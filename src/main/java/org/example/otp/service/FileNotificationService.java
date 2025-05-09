package org.example.otp.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class FileNotificationService {
    private static final Logger logger = Logger.getLogger(FileNotificationService.class.getName());
    private static final String FILE_PATH = "otp_codes.log";

    public void saveToFile(String operationId, String code) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(String.format("[%s] Operation ID: %s, Code: %s%n",
                    LocalDateTime.now(), operationId, code));
            logger.info("Код OTP сохранен в файле: " + operationId);
        } catch (Exception e) {
            logger.severe("Не удалось записать OTP-код в файл: " + e.getMessage());
        }
    }
}
