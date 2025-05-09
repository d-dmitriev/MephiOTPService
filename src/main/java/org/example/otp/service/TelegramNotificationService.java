package org.example.otp.service;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TelegramNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final String telegramApiUrl;
    private final String botToken;

    public TelegramNotificationService() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("telegram.properties")) {
            props.load(input);
            this.botToken = props.getProperty("telegram.bot.token");
            this.telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить конфигурацию Telegram", e);
        }
    }

    public void sendCode(String destination, String code) {
        String message = String.format("Ваш код подтверждения: %s", code);
        String url = telegramApiUrl + "?chat_id=" + destination + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            client.execute(request);
        } catch (IOException e) {
            logger.error("Ошибка отправки сообщения через Telegram: {}", e.getMessage());
        }
    }
}
