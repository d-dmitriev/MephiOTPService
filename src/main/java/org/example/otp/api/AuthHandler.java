package org.example.otp.api;

import org.example.otp.service.AuthService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

import static org.example.otp.service.AuthService.ADMIN_EXISTS;
import static org.example.otp.service.AuthService.USER_EXISTS;
import static org.example.otp.util.HttpUtils.*;

public class AuthHandler implements HttpHandler {
    private static final Logger logger = Logger.getLogger(AuthHandler.class.getName());
    private static final String ADMIN_ALREADY_EXISTS = "Администратор уже существует";
    private static final String USER_ALREADY_EXISTS = "Пользователь уже существует";
    private static final String INVALID_CREDENTIALS = "Недействительные учетные данные";
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!POST.equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        Map<String, String> params = parseRequestBody(sb.toString());

        String login = params.get("login");
        String password = params.get("password");
        String role = params.get("role");

        String token;
        int responseCode;

        if ("/register".equals(exchange.getRequestURI().getPath())) {
            String registerResult = authService.registerUser(login, password, role);
            if (ADMIN_EXISTS.equals(registerResult)) {
                sendError(exchange, ADMIN_ALREADY_EXISTS, 400);
                logger.severe(ADMIN_ALREADY_EXISTS);
                return;
            } else if (USER_EXISTS.equals(registerResult)) {
                sendError(exchange, USER_ALREADY_EXISTS, 400);
                logger.severe(USER_ALREADY_EXISTS);
                return;
            }
            token = registerResult;
            responseCode = 201;
        } else if ("/login".equals(exchange.getRequestURI().getPath())) {
            token = authService.authenticateUser(login, password);
            if (token == null) {
                sendError(exchange, INVALID_CREDENTIALS, 401);
                logger.severe(INVALID_CREDENTIALS);
                return;
            }
            responseCode = 200;
        } else {
            exchange.sendResponseHeaders(404, -1); // Not Found
            return;
        }

        String response = String.format("{\"token\": \"%s\"}", token);
        sendResponse(exchange, response, responseCode);
    }
}
