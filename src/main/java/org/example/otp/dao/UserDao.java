package org.example.otp.dao;

import org.example.otp.model.User;
import org.example.otp.util.DbConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    public Optional<User> findByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setLogin(rs.getString("login"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            logger.error("Ошибка поиска пользователя по логину: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public void save(User user) {
        String sql = "INSERT INTO users(login, password_hash, role) VALUES(?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getLogin());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getRole());
            pstmt.executeUpdate();
            logger.info("Пользователь сохранен: {}", user.getLogin());
        } catch (SQLException e) {
            logger.error("Ошибка сохранения пользователя: {}", e.getMessage());
        }
    }

    public List<User> findAllUsersExceptAdmin() {
        String sql = "SELECT * FROM users WHERE role != 'ADMIN'";
        List<User> users = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setLogin(rs.getString("login"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));
                users.add(user);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при извлечении пользователей: {}", e.getMessage());
        }
        return users;
    }

    public boolean existsAdmin() {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE role = 'ADMIN')";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            logger.error("Ошибка проверки существования администратора: {}", e.getMessage());
        }
        return false;
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Удален пользователь с идентификатором: {}", id);
        } catch (SQLException e) {
            logger.error("Ошибка удаления пользователя: {}", e.getMessage());
        }
    }
}