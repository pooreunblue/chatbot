package com.example.archat.infrastructure.repository;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.repository.ChatRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupabaseChatRepository implements ChatRepository {
    private static final String DEFAULT_TABLE = "chats";
    private static final SupabaseChatRepository instance = new SupabaseChatRepository();

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String tableName;

    private SupabaseChatRepository() {
        String projectUrl = getRequiredEnv("SUPABASE_URL");
        this.jdbcUrl = getRequiredEnv("SUPABASE_JDBC_URL");
        this.username = getEnvOrDefault("SUPABASE_DB_USER", "postgres");
        this.password = getRequiredEnv("SUPABASE_DB_PASSWORD");
        this.tableName = getEnvOrDefault("SUPABASE_CHAT_TABLE", DEFAULT_TABLE);

        if (!projectUrl.startsWith("https://")) {
            throw new IllegalStateException("SUPABASE_URL must be a Supabase project URL");
        }
    }

    public static SupabaseChatRepository getInstance() {
        return instance;
    }

    @Override
    public void save(Chat chat) {
        String sql = "insert into " + tableName + " (message, owner, user_id, model, timestamp) values (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chat.message());
            statement.setString(2, chat.owner());
            statement.setString(3, chat.userId());
            statement.setString(4, chat.model());
            statement.setString(5, chat.timestamp());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save chat to Supabase", e);
        }
    }

    @Override
    public List<Chat> findAllByUserId(String userId) {
        String sql = "select message, owner, user_id, model, timestamp from " + tableName + " where user_id = ? order by timestamp asc";
        List<Chat> chats = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    chats.add(new Chat(
                            resultSet.getString("message"),
                            resultSet.getString("owner"),
                            resultSet.getString("user_id"),
                            resultSet.getString("model"),
                            resultSet.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to read chats from Supabase", e);
        }
        return chats;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private static String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
