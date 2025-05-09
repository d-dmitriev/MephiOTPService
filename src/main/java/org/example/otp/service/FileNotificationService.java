package org.example.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;

public class FileNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);
    private static final String FILE_PATH = "otp_codes.log";

    public void saveToFile(String operationId, String code) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(String.format("[%s] Operation ID: %s, Code: %s%n",
                    LocalDateTime.now(), operationId, code));
            logger.info("Код OTP сохранен в файле: {}", operationId);
        } catch (Exception e) {
            logger.error("Не удалось записать OTP-код в файл: {}", e.getMessage());
        }
    }
}
