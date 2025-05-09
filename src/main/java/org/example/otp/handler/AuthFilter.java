package org.example.otp.handler;

import org.example.otp.util.TokenUtil;
import org.example.otp.dao.UserDao;
import org.example.otp.model.User;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.logging.Logger;

import static org.example.otp.util.HttpUtils.sendError;

public class AuthFilter implements HttpHandler {
    private static final Logger logger = Logger.getLogger(AuthFilter.class.getName());

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
            logger.warning(String.format("Доступ запрещен к %s %s: отсутствует или недействителен токен", method, path));
            sendError(exchange, "Отсутствует или недействителен токен", 401);
            return;
        }

        String token = authHeader.substring(7); // Убираем "Bearer "
        TokenUtil tokenUtil = new TokenUtil();
        StringBuilder usernameBuilder = new StringBuilder();

        if (!tokenUtil.validateToken(token, usernameBuilder)) {
            logger.warning(String.format("Доступ запрещен к %s %s: недействительный или просроченный токен", method, path));
            sendError(exchange, "Недействительный или просроченный токен", 401);
            return;
        }

        String username = usernameBuilder.toString();
        User user = new UserDao().findByLogin(username).orElse(null);

        if (user == null) {
            logger.warning(String.format("Пользователь не найден для токена: %s", username));
            sendError(exchange, "Пользователь не найден", 401);
            return;
        }

        // Проверяем роль
        if (!user.getRole().equals(requiredRole) && !user.getRole().equals("ADMIN")) {
            logger.warning(String.format("Доступ запрещен для %s %s: пользователь не %s", method, path, requiredRole));
            sendError(exchange, "Доступ запрещен", 403);
            return;
        }

        logger.info(String.format("Пользователь '%s' получил доступ к %s %s", username, method, path));

        exchange.getHttpContext().getAttributes().put("username", username);

        // Передаём управление следующему обработчику
        try {
            next.handle(exchange);
        } catch (Exception e) {
            logger.severe("Ошибка в обработчике: " + e.getMessage());
            sendError(exchange, "Внутренняя ошибка сервера", 500);
        }
    }
}