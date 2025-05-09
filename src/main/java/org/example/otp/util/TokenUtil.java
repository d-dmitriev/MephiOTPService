package org.example.otp.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Logger;

public class TokenUtil {
    private static final Logger logger = Logger.getLogger(TokenUtil.class.getName());

    // Секретный ключ для подписи токена
    private static final String SECRET_KEY = "my_very_secret_key_for_signing_tokens";
    // Время жизни токена в секундах (например, 30 минут)
    private static final long EXPIRATION_TIME = 1800; // 30 минут

    /**
     * Генерирует токен для указанного пользователя
     */
    public String generateToken(String username) {
        long expirationTimestamp = Instant.now().getEpochSecond() + EXPIRATION_TIME;
        String tokenContent = username + ":" + expirationTimestamp;
        String signature = calculateHmac(tokenContent);

        String token = Base64.getEncoder().encodeToString(tokenContent.getBytes(StandardCharsets.UTF_8)) +
                "." +
                Base64.getEncoder().encodeToString(signature.getBytes(StandardCharsets.UTF_8));

        logger.info("Токен, сгенерированный для пользователя: " + username);
        return token;
    }

    /**
     * Проверяет токен и возвращает имя пользователя, если токен валиден
     */
    public boolean validateToken(String token, StringBuilder username) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                logger.warning("Неверный формат токена");
                return false;
            }

            String decodedContent = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] contentParts = decodedContent.split(":");
            if (contentParts.length != 2) {
                logger.warning("Недействительное содержимое токена");
                return false;
            }

            String user = contentParts[0];
            long expirationTime = Long.parseLong(contentParts[1]);

            if (Instant.now().getEpochSecond() > expirationTime) {
                logger.warning("Срок действия токена для пользователя истек: " + user);
                return false;
            }

            String expectedSignature = calculateHmac(decodedContent);
            String providedSignature = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            if (!expectedSignature.equals(providedSignature)) {
                logger.warning("Несоответствие подписи токена для пользователя: " + user);
                return false;
            }

            username.append(user);
            logger.info("Токен успешно проверен для пользователя: " + user);
            return true;
        } catch (Exception e) {
            logger.severe("Проверка токена не удалась: " + e.getMessage());
            return false;
        }
    }

    /**
     * Вычисляет HMAC-SHA256 подпись для строки
     */
    private String calculateHmac(String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Ошибка расчета HMAC", e);
        }
    }
}