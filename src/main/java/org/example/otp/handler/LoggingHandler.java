package org.example.otp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.logging.Logger;

public class LoggingHandler implements HttpHandler {
    private static final Logger logger = Logger.getLogger(LoggingHandler.class.getName());
    private final HttpHandler next;

    public LoggingHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Instant start = Instant.now();

        // Логируем детали запроса
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        String method = exchange.getRequestMethod();
        String uri = exchange.getRequestURI().toString();

        logger.info(String.format("Входящий запрос: %s %s | IP: %s", method, uri, clientIp));

        // Выполняем реальный обработчик
        try {
            next.handle(exchange);
        } catch (Exception e) {
            logger.severe("Ошибка обработки запроса " + method + " " + uri + ": " + e.getMessage());
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
            exchange.getResponseBody().write("Внутренняя ошибка сервера".getBytes(StandardCharsets.UTF_8));
            exchange.getResponseBody().close();
        }

        // Логируем ответ
        long duration = System.currentTimeMillis() - start.toEpochMilli();
        int responseCode = getResponseCode(exchange);
        logger.info(String.format("Запрос выполнен: %s %s | Код ответа: %d | Время: %d мс",
                method, uri, responseCode, duration));
    }

    private int getResponseCode(HttpExchange exchange) {
        try {
            return exchange.getResponseCode();
        } catch (Exception e) {
            return -1;
        }
    }
}