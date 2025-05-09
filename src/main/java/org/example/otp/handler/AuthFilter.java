package org.example.otp.handler;

import org.example.otp.util.TokenUtil;
import org.example.otp.dao.UserDao;
import org.example.otp.model.User;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.example.otp.util.HttpUtils.sendError;

public class AuthFilter implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    private final HttpHandler next;
    private final String requiredRole;

    public AuthFilter(HttpHandler next, String requiredRole) {
        this.next = next;
        this.requiredRole = requiredRole;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Логируем попытку доступа
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Извлекаем токен
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Доступ запрещен к {} {}: отсутствует или недействителен токен", method, path);
            sendError(exchange, "Отсутствует или недействителен токен", 401);
            return;
        }

        String token = authHeader.substring(7); // Убираем "Bearer "
        TokenUtil tokenUtil = new TokenUtil();
        StringBuilder usernameBuilder = new StringBuilder();

        if (!tokenUtil.validateToken(token, usernameBuilder)) {
            logger.warn("Доступ запрещен к {} {}: недействительный или просроченный токен", method, path);
            sendError(exchange, "Недействительный или просроченный токен", 401);
            return;
        }

        String username = usernameBuilder.toString();
        User user = new UserDao().findByLogin(username).orElse(null);

        if (user == null) {
            logger.warn("Пользователь не найден для токена: {}", username);
            sendError(exchange, "Пользователь не найден", 401);
            return;
        }

        // Проверяем роль
        if (!user.getRole().equals(requiredRole) && !user.getRole().equals("ADMIN")) {
            logger.warn("Доступ запрещен для {} {}: пользователь не {}", method, path, requiredRole);
            sendError(exchange, "Доступ запрещен", 403);
            return;
        }

        logger.info("Пользователь '{}' получил доступ к {} {}", username, method, path);

        exchange.getHttpContext().getAttributes().put("username", username);

        // Передаём управление следующему обработчику
        try {
            next.handle(exchange);
        } catch (Exception e) {
            logger.error("Ошибка в обработчике: {}", e.getMessage());
            sendError(exchange, "Внутренняя ошибка сервера", 500);
        }
    }
}