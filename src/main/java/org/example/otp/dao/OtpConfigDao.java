package org.example.otp.dao;

import org.example.otp.model.OtpConfig;
import org.example.otp.util.DbConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class OtpConfigDao {
    private static final Logger logger = LoggerFactory.getLogger(OtpConfigDao.class);

    public OtpConfig getConfig() {
        String sql = "SELECT * FROM otp_config LIMIT 1";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                OtpConfig config = new OtpConfig();
                config.setCodeLength(rs.getInt("code_length"));
                config.setExpirationTime(rs.getInt("expiration_time"));
                return config;
            }
        } catch (SQLException e) {
            logger.error("Ошибка получения конфигурации OTP: {}", e.getMessage());
        }
        // Default values
        return new OtpConfig();
    }

    public void updateConfig(OtpConfig config) {
        String sql = "UPDATE otp_config SET code_length = ?, expiration_time = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, config.getCodeLength());
            pstmt.setInt(2, config.getExpirationTime());
            pstmt.executeUpdate();
            logger.info("Конфигурация OTP обновлена");
        } catch (SQLException e) {
            logger.error("Ошибка обновления конфигурации OTP: {}", e.getMessage());
        }
    }
}
