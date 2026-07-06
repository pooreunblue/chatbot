package com.example.archat.infrastructure.repository;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;
import com.example.archat.domain.model.ConversationSummary;
import com.example.archat.domain.repository.ChatRepository;
import com.example.archat.infrastructure.supabase.SupabaseRestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SupabaseChatRepository implements ChatRepository {
    private static final SupabaseChatRepository instance = new SupabaseChatRepository();

    private final SupabaseRestClient supabaseRestClient = SupabaseRestClient.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            if (!attachments.isEmpty()) {
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

            String response = supabaseRestClient.get(
                    "messages?conversation_id=eq." + conversationId
                            + "&select=conversation_id,content,role,model_name,created_at"
                            + "&order=created_at.asc,message_id.asc"
            );
            JsonNode jsonArray = objectMapper.readTree(response);
            List<Chat> chats = new ArrayList<>();
            for (JsonNode node : jsonArray) {
                chats.add(new Chat(
                        node.get("conversation_id").asLong(),
                        node.path("content").asText(""),
                        toOwner(node.path("role").asText("assistant")),
                        userId,
                        node.path("model_name").asText(""),
                        node.path("created_at").asText("")
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
