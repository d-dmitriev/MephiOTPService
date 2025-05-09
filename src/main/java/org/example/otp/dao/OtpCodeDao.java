package org.example.otp.dao;

import org.example.otp.model.OtpCode;
import org.example.otp.util.DbConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class OtpCodeDao {
    private static final Logger logger = LoggerFactory.getLogger(OtpCodeDao.class);

    public void save(OtpCode code) {
        String sql = "INSERT INTO otp_codes(user_id, operation_id, code, status, expires_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, code.getUserId());
            pstmt.setString(2, code.getOperationId());
            pstmt.setString(3, code.getCode());
            pstmt.setString(4, code.getStatus());
            pstmt.setTimestamp(5, Timestamp.valueOf(code.getExpiresAt()));
            pstmt.executeUpdate();
            logger.info("Код OTP сохранен для идентификатора пользователя: {}", code.getUserId());
        } catch (SQLException e) {
            logger.error("Ошибка сохранения OTP-кода: {}", e.getMessage());
        }
    }

    public Optional<OtpCode> findByOperationIdAndCode(String operationId, String code) {
        String sql = "SELECT * FROM otp_codes WHERE operation_id = ? AND code = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, operationId);
            pstmt.setString(2, code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                OtpCode otpCode = new OtpCode();
                otpCode.setId(rs.getInt("id"));
                otpCode.setUserId(rs.getInt("user_id"));
                otpCode.setOperationId(rs.getString("operation_id"));
                otpCode.setCode(rs.getString("code"));
                otpCode.setStatus(rs.getString("status"));
                otpCode.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                otpCode.setExpiresAt(rs.getObject("expires_at", LocalDateTime.class));
                return Optional.of(otpCode);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске OTP-кода: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public void markAsUsed(String operationId, String code) {
        String sql = "UPDATE otp_codes SET status = 'USED' WHERE operation_id = ? AND code = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, operationId);
            pstmt.setString(2, code);
            pstmt.executeUpdate();
            logger.info("Код OTP отмечен как использованный");
        } catch (SQLException e) {
            logger.error("Ошибка маркировки кода OTP как использованного: {}", e.getMessage());
        }
    }

    public void markAllExpired() {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' WHERE expires_at < CURRENT_TIMESTAMP AND status = 'ACTIVE'";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            int count = stmt.executeUpdate(sql);
            logger.info("{} OTP-кода истекли.", count);
        } catch (SQLException e) {
            logger.error("Ошибка истечения срока действия одноразовых паролей: {}", e.getMessage());
        }
    }

    public void deleteByUserId(int userId) {
        String sql = "DELETE FROM otp_codes WHERE user_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            logger.info("Удалены одноразовые пароли для идентификатора пользователя: {}", userId);
        } catch (SQLException e) {
            logger.error("Ошибка удаления OTP-кодов по идентификатору пользователя: {}", e.getMessage());
        }
    }
}
