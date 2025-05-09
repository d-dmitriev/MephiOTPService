package org.example.otp.api;

import org.example.otp.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

import static org.example.otp.util.HttpUtils.*;

public class UserApi implements HttpHandler {
    private static final Logger logger = Logger.getLogger(UserApi.class.getName());
    private static final String INVALID_OR_EXPIRED_CODE = "Недействительный или просроченный код";
    private static final String AUTHENTICATION_REQUIRED = "Требуется аутентификация";
    public static final String INVALID_CHANNEL = "Неверный канал";
    private final AuthService authService = new AuthService();
    private final OtpService otpService = new OtpService();
    private final EmailNotificationService emailNotifier = new EmailNotificationService();
    private final SmsNotificationService smsNotifier = new SmsNotificationService();
    private final TelegramNotificationService telegramNotifier = new TelegramNotificationService();
    private final FileNotificationService fileNotifier = new FileNotificationService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Object userAttr = exchange.getHttpContext().getAttributes().get("username");
        if (userAttr == null) {
            sendError(exchange, AUTHENTICATION_REQUIRED, 401);
            logger.severe("Атрибут username не существует в http-контексте");
            return;
        }
        String username = userAttr.toString();

        int userId = authService.getUserId(username);

        if (POST.equals(method) && "/user/generate".equals(path)) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            Map<String, String> params = parseRequestBody(sb.toString());
            String operationId = params.get("operationId");
            String channel = params.get("channel"); // "email", "sms", "telegram", "file"
            String destination = params.get("destination");

            if (operationId == null || channel == null || destination == null && !channel.equals("file")) {
                sendError(exchange, "Отсутствуют необходимые параметры", 400);
                return;
            }

            String code = otpService.generateOtp(userId, operationId);
            switch (channel.toLowerCase()) {
                case "email":
                    emailNotifier.sendCode(destination, code);
                    break;
                case "sms":
                    smsNotifier.sendCode(destination, code);
                    break;
                case "telegram":
                    telegramNotifier.sendCode(destination, code);
                    break;
                case "file":
                    fileNotifier.saveToFile(operationId, code);
                    break;
                default:
                    sendError(exchange, INVALID_CHANNEL, 400);
                    logger.severe(INVALID_CHANNEL);
                    return;
            }

            sendResponse(exchange, "{\"message\": \"Код отправлен\"}", 200);

        } else if (POST.equals(method) && "/user/validate".equals(path)) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            Map<String, String> params = parseRequestBody(sb.toString());
            String operationId = params.get("operationId");
            String code = params.get("code");

            boolean isValid = otpService.validateOtp(operationId, code);
            if (isValid) {
                sendResponse(exchange, "{\"valid\": true}", 200);
            } else {
                sendError(exchange, INVALID_OR_EXPIRED_CODE, 400);
                logger.severe(INVALID_OR_EXPIRED_CODE);
            }

        } else {
            sendError(exchange, NOT_FOUND, 404);
            logger.severe(NOT_FOUND);
        }
    }
}
