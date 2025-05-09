package org.example.otp.api;

import org.example.otp.model.OtpConfig;
import org.example.otp.model.User;
import org.example.otp.service.AuthService;
import org.example.otp.service.OtpService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

import static org.example.otp.util.HttpUtils.*;

public class AdminApi implements HttpHandler {
    private static final Logger logger = Logger.getLogger(AdminApi.class.getName());
    private final OtpService otpService = new OtpService();
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (GET.equals(method) && "/admin/config".equals(path)) {
            OtpConfig config = otpService.getOtpConfig();
            String response = String.format("{\"codeLength\": %d, \"expirationTime\": %d}",
                    config.getCodeLength(), config.getExpirationTime());
            sendResponse(exchange, response, 200);

        } else if (POST.equals(method) && "/admin/config".equals(path)) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            Map<String, String> params = parseRequestBody(sb.toString());
            int codeLength = Integer.parseInt(params.get("codeLength"));
            int expirationTime = Integer.parseInt(params.get("expirationTime"));

            otpService.updateOtpConfig(codeLength, expirationTime);
            sendResponse(exchange, "{\"message\": \"Конфигурация обновлена\"}", 200);

        } else if (GET.equals(method) && "/admin/users".equals(path)) {
            List<User> users = authService.getAllUsersExceptAdmin();
            StringBuilder usersJson = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                usersJson.append(String.format(
                        "{\"id\":%d, \"login\":\"%s\", \"role\":\"%s\"}",
                        user.getId(), user.getLogin(), user.getRole()
                ));
                if (i < users.size() - 1) usersJson.append(",");
            }
            usersJson.append("]");
            sendResponse(exchange, usersJson.toString(), 200);

        } else if (DELETE.equals(method) && "/admin/users/".equals(path.substring(0, 13))) {
            int userId = Integer.parseInt(path.substring(13));
            authService.deleteUser(userId);
            sendResponse(exchange, "{\"message\": \"Пользователь удален\"}", 200);

        } else {
            sendError(exchange, NOT_FOUND, 404);
            logger.severe(NOT_FOUND);
        }
    }
}