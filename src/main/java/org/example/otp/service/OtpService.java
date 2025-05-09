package org.example.otp.service;

import org.example.otp.dao.OtpCodeDao;
import org.example.otp.dao.OtpConfigDao;
import org.example.otp.model.OtpCode;
import org.example.otp.model.OtpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpCodeDao otpCodeDao = new OtpCodeDao();
    private final OtpConfigDao otpConfigDao = new OtpConfigDao();

    /**
     * Генерирует новый OTP-код и сохраняет его в БД.
     */
    public String generateOtp(int userId, String operationId) {
        OtpConfig config = otpConfigDao.getConfig();
        int codeLength = config.getCodeLength();
        int expirationSeconds = config.getExpirationTime();

        String code = generateRandomCode(codeLength);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationSeconds);

        OtpCode otpCode = new OtpCode();
        otpCode.setUserId(userId);
        otpCode.setOperationId(operationId);
        otpCode.setCode(code);
        otpCode.setStatus("ACTIVE");
        otpCode.setExpiresAt(expiresAt);

        otpCodeDao.save(otpCode);
        logger.info("Сгенерированный OTP-код для идентификатора операции: {}", operationId);
        return code;
    }

    /**
     * Проверяет, существует ли указанный код для операции и активен ли он.
     */
    public boolean validateOtp(String operationId, String code) {
        Optional<OtpCode> optionalOtp = otpCodeDao.findByOperationIdAndCode(operationId, code);
        if (optionalOtp.isEmpty()) {
            logger.warn("Неверный код OTP для операции: {}", operationId);
            return false;
        }

        OtpCode otpCode = optionalOtp.get();
        if (!"ACTIVE".equals(otpCode.getStatus())) {
            logger.warn("OTP просрочен или используется для операции: {}", operationId);
            return false;
        }

        if (LocalDateTime.now().isAfter(otpCode.getExpiresAt())) {
            logger.warn("Срок действия OTP истек: {}", operationId);
            return false;
        }

        otpCodeDao.markAsUsed(operationId, code);
        logger.info("OTP успешно прошел проверку на работоспособность: {}", operationId);
        return true;
    }

    /**
     * Генерирует случайный числовой код заданной длины.
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Обновляет глобальные настройки OTP: длина кода и время жизни.
     */
    public void updateOtpConfig(int codeLength, int expirationTime) {
        OtpConfig config = new OtpConfig();
        config.setCodeLength(codeLength);
        config.setExpirationTime(expirationTime);
        otpConfigDao.updateConfig(config);
        logger.info("Конфигурация OTP обновлена");
    }

    /**
     * Удаляет все OTP-коды, привязанные к пользователю.
     */
    public void deleteCodesByUserId(int userId) {
        otpCodeDao.deleteByUserId(userId);
        logger.info("Удалены OTP для идентификатора пользователя: {}", userId);
    }

    /**
     * Отмечает все истёкшие коды как EXPIRED.
     */
    public void expireOldCodes() {
        otpCodeDao.markAllExpired();
        logger.info("OTP-коды с истекшим сроком действия");
    }

    public OtpConfig getOtpConfig() {
        OtpConfig config = otpConfigDao.getConfig();
        if (config == null) {
            // Если конфиг не найден, используем значения по умолчанию
            logger.warn("Конфигурация OTP не найдена в БД. Используются значения по умолчанию.");
            config = new OtpConfig();
            config.setCodeLength(6);         // 6 цифр
            config.setExpirationTime(300);   // 5 минут
        }
        return config;
    }
}