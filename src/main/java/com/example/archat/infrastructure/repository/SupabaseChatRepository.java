package com.example.archat.infrastructure.repository;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;
import com.example.archat.domain.model.ConversationSummary;
import com.example.archat.domain.repository.ChatRepository;
import com.example.archat.infrastructure.config.EnvironmentSettings;
import com.example.archat.infrastructure.supabase.SupabaseRestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SupabaseChatRepository implements ChatRepository {
    private static final SupabaseChatRepository instance = new SupabaseChatRepository();

    private final SupabaseRestClient supabaseRestClient = SupabaseRestClient.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String storageBucket = EnvironmentSettings.getOrDefault("SUPABASE_STORAGE_BUCKET", "chat-attachments");

    private SupabaseChatRepository() {
    }

    public static SupabaseChatRepository getInstance() {
        return instance;
    }

    @Override
    public Long createConversation(String userId, String title) {
        try {
            String response = supabaseRestClient.post(
                    "conversations?select=conversation_id",
                    objectMapper.writeValueAsString(new ConversationInsertPayload(Long.parseLong(userId), title)),
                    true
            );
            JsonNode jsonArray = objectMapper.readTree(response);
            if (!jsonArray.isArray() || jsonArray.isEmpty()) {
                throw new IllegalStateException("Conversation insert did not return an id");
            }
            return jsonArray.get(0).get("conversation_id").asLong();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create conversation in Supabase: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Chat chat, List<ChatAttachment> attachments) {
        try {
            String messageResponse = supabaseRestClient.post(
                    "messages?select=message_id",
                    objectMapper.writeValueAsString(new MessageInsertPayload(
                            chat.conversationId(),
                            toDatabaseRole(chat.owner()),
                            chat.message(),
                            chat.model(),
                            chat.timestamp()
                    )),
                    true
            );
            JsonNode messageArray = objectMapper.readTree(messageResponse);
            if (!messageArray.isArray() || messageArray.isEmpty()) {
                throw new IllegalStateException("Message insert did not return an id");
            }

            long messageId = messageArray.get(0).get("message_id").asLong();
            if (attachments != null && !attachments.isEmpty()) {
                List<AttachmentInsertPayload> payloads = attachments.stream()
                        .map(attachment -> new AttachmentInsertPayload(
                                messageId,
                                chat.conversationId(),
                                attachment.fileName(),
                                attachment.filePath(),
                                attachment.mimeType(),
                                attachment.fileSize()
                        ))
                        .toList();
                supabaseRestClient.post("attachments", objectMapper.writeValueAsString(payloads), false);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save chat to Supabase: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Chat> findAllByConversationId(String userId, Long conversationId) {
        try {
            String ownershipCheck = supabaseRestClient.get(
                    "conversations?conversation_id=eq." + conversationId
                            + "&user_id=eq." + userId
                            + "&select=conversation_id&limit=1"
            );
            JsonNode ownershipArray = objectMapper.readTree(ownershipCheck);
            if (!ownershipArray.isArray() || ownershipArray.isEmpty()) {
                return List.of();
            }

            String messageResponse = supabaseRestClient.get(
                    "messages?conversation_id=eq." + conversationId
                            + "&select=message_id,conversation_id,content,role,model_name,created_at"
                            + "&order=created_at.asc,message_id.asc"
            );
            JsonNode messageArray = objectMapper.readTree(messageResponse);

            String attachmentResponse = supabaseRestClient.get(
                    "attachments?conversation_id=eq." + conversationId
                            + "&select=attachment_id,message_id,file_name,file_path,mime_type,file_size"
                            + "&order=created_at.asc,attachment_id.asc"
            );
            JsonNode attachmentArray = objectMapper.readTree(attachmentResponse);

            Map<Long, List<ChatAttachment>> attachmentsByMessageId = new LinkedHashMap<>();
            for (JsonNode node : attachmentArray) {
                long messageId = node.path("message_id").asLong();
                attachmentsByMessageId.computeIfAbsent(messageId, key -> new ArrayList<>())
                        .add(new ChatAttachment(
                                node.path("attachment_id").asLong(),
                                node.path("file_name").asText(""),
                                node.path("file_path").asText(""),
                                node.path("mime_type").asText(""),
                                node.path("file_size").asLong(0L),
                                isImageMimeType(node.path("mime_type").asText(""))
                        ));
            }

            List<Chat> chats = new ArrayList<>();
            for (JsonNode node : messageArray) {
                long messageId = node.path("message_id").asLong();
                chats.add(new Chat(
                        messageId,
                        node.path("conversation_id").asLong(),
                        node.path("content").asText(""),
                        toOwner(node.path("role").asText("assistant")),
                        userId,
                        node.path("model_name").asText(""),
                        node.path("created_at").asText(""),
                        attachmentsByMessageId.getOrDefault(messageId, List.of())
                ));
            }
            return chats;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load conversation messages from Supabase: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ConversationSummary> findConversationsByUserId(String userId) {
        try {
            String response = supabaseRestClient.get(
                    "conversations?user_id=eq." + userId
                            + "&select=conversation_id,title,updated_at"
                            + "&order=updated_at.desc,conversation_id.desc"
            );
            JsonNode jsonArray = objectMapper.readTree(response);
            List<ConversationSummary> conversations = new ArrayList<>();
            for (JsonNode node : jsonArray) {
                conversations.add(new ConversationSummary(
                        node.get("conversation_id").asLong(),
                        node.path("title").asText("New chat"),
                        node.path("updated_at").asText("")
                ));
            }
            return conversations;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load conversations from Supabase: " + e.getMessage(), e);
        }
    }

    @Override
    public Long findLatestConversationId(String userId) {
        List<ConversationSummary> conversations = findConversationsByUserId(userId);
        return conversations.isEmpty() ? null : conversations.get(0).conversationId();
    }

    @Override
    public void touchConversation(Long conversationId) {
        try {
            String existing = supabaseRestClient.get(
                    "conversations?conversation_id=eq." + conversationId + "&select=title&limit=1"
            );
            JsonNode array = objectMapper.readTree(existing);
            if (!array.isArray() || array.isEmpty()) {
                return;
            }
            String currentTitle = array.get(0).path("title").asText("New chat");
            supabaseRestClient.patch(
                    "conversations?conversation_id=eq." + conversationId,
                    objectMapper.writeValueAsString(new ConversationTouchPayload(currentTitle, OffsetDateTime.now().toString())),
                    false
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update conversation timestamp: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateConversationTitle(String userId, Long conversationId, String title) {
        try {
            supabaseRestClient.patch(
                    "conversations?conversation_id=eq." + conversationId + "&user_id=eq." + userId,
                    objectMapper.writeValueAsString(new ConversationTitlePayload(title)),
                    false
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update conversation title: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteConversation(String userId, Long conversationId) {
        try {
            supabaseRestClient.delete(
                    "conversations?conversation_id=eq." + conversationId + "&user_id=eq." + userId
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete conversation: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatAttachment storeAttachment(String userId, Long conversationId, String originalFileName, String mimeType, byte[] fileBytes) {
        try {
            String safeFileName = sanitizeFileName(originalFileName);
            String storagePath = "users/%s/conversations/%d/%s-%s".formatted(
                    userId,
                    conversationId,
                    UUID.nameUUIDFromBytes((safeFileName + System.nanoTime()).getBytes()),
                    safeFileName
            );

            supabaseRestClient.uploadBinary(
                    "/storage/v1/object/" + storageBucket + "/" + supabaseRestClient.encodePath(storagePath),
                    fileBytes,
                    mimeType == null || mimeType.isBlank() ? "application/octet-stream" : mimeType
            );

            String publicUrl = supabaseRestClient.getPublicStorageUrl(storageBucket, storagePath);
            return new ChatAttachment(
                    null,
                    safeFileName,
                    publicUrl,
                    mimeType,
                    fileBytes.length,
                    isImageMimeType(mimeType)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store attachment in Supabase Storage: " + e.getMessage(), e);
        }
    }

    private String sanitizeFileName(String fileName) {
        String normalized = fileName == null ? "attachment" : fileName.trim();
        if (normalized.isBlank()) {
            return "attachment";
        }
        return Path.of(normalized).getFileName().toString();
    }

    private boolean isImageMimeType(String mimeType) {
        return mimeType != null && mimeType.toLowerCase().startsWith("image/");
    }

    private String toDatabaseRole(String owner) {
        return "USER".equalsIgnoreCase(owner) ? "user" : "assistant";
    }

    private String toOwner(String role) {
        return "user".equalsIgnoreCase(role) ? "USER" : "AI";
    }

    private record ConversationInsertPayload(long user_id, String title) {}
    private record MessageInsertPayload(long conversation_id, String role, String content, String model_name, String created_at) {}
    private record AttachmentInsertPayload(long message_id, long conversation_id, String file_name, String file_path, String mime_type, long file_size) {}
    private record ConversationTouchPayload(String title, String updated_at) {}
    private record ConversationTitlePayload(String title) {}
}
