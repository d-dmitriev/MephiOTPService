package org.example.otp.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

public class PasswordHasher {
    private static final Logger logger = Logger.getLogger(PasswordHasher.class.getName());

    // Настройки PBKDF2
    private static final int ITERATIONS = 600000;       // Кол-во итераций
    private static final int KEY_LENGTH = 512;          // Длина ключа
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

    /**
     * Хэширует пароль с использованием PBKDF2 и случайной соли.
     */
    public String hash(String password) {
        char[] chars = password.toCharArray();
        byte[] salt = generateSalt();

        byte[] hash = hashWithSalt(chars, salt);
        return toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Проверяет, совпадает ли введённый пароль с сохранённым хэшем.
     */
    public boolean check(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            logger.warning("Сохраненный хэш недействителен (отсутствует соль или хэш)");
            return false;
        }

        byte[] salt = fromHex(parts[0]);
        byte[] storedPasswordHash = fromHex(parts[1]);

        byte[] testHash = hashWithSalt(password.toCharArray(), salt);

        return slowEquals(storedPasswordHash, testHash);
    }

    /**
     * Генерирует случайную соль.
     */
    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Вычисляет хэш пароля с указанной солью.
     */
    private byte[] hashWithSalt(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Ошибка хеширования пароля", e);
        }
    }

    /**
     * Сравнивает два массива байтов в постоянное время, чтобы избежать side-channel атак.
     */
    private boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    /**
     * Преобразует байты в шестнадцатеричную строку.
     */
    private String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }
        return hex;
    }

    /**
     * Преобразует шестнадцатеричную строку обратно в байты.
     */
    private byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
