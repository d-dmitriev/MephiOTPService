package org.example.otp.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpUtils {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";
    public static final String NOT_FOUND = "Not found";
    /**
     * Отправляет JSON-ответ клиенту
     */
    public static void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Отправляет JSON-ошибку клиенту
     */
    public static void sendError(HttpExchange exchange, String message, int code) throws IOException {
        String errorJson = String.format("{\"error\": \"%s\"}", message);
        sendResponse(exchange, errorJson, code);
    }

    /**
     * Парсит тело запроса в Map<String, String>
     */
    public static Map<String, String> parseRequestBody(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isBlank()) return map;

        // Убираем лишние символы
        json = json.replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\"", "");

        for (String pair : json.split(",")) {
            String[] entry = pair.trim().split(":");
            if (entry.length == 2) {
                map.put(entry[0].trim(), entry[1].trim());
            }
        }
        return map;
    }
}
