package org.example.otp;

import org.example.otp.handler.AuthFilter;
import org.example.otp.handler.LoggingHandler;
import org.example.otp.scheduler.OtpExpiryScheduler;
import org.example.otp.api.AdminApi;
import org.example.otp.api.AuthHandler;
import org.example.otp.api.UserApi;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            // Загрузить конфигурацию логгирования из logging.properties
            String configPath = "src/main/resources/logging.properties";
            if (Files.exists(Paths.get(configPath))) {
                LogManager.getLogManager().readConfiguration(
                        Files.newInputStream(Paths.get(configPath))
                );
                logger.info("Логгирование инициализировано");
            } else {
                System.err.println("Файл logging.properties не найден. Используется стандартное логгирование.");
            }

            // Создаем директорию для логов, если её нет
            Files.createDirectories(Paths.get("logs"));

            // Запуск HTTP сервера
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            logger.info("HTTP-сервер запущен на порту " + PORT);

            // Регистрация обработчиков
            server.createContext("/register", new LoggingHandler(new AuthHandler()));
            server.createContext("/login", new LoggingHandler(new AuthHandler()));
            server.createContext("/admin/", new LoggingHandler(new AuthFilter(new AdminApi(), "ADMIN")));
            server.createContext("/user/", new LoggingHandler(new AuthFilter(new UserApi(), "USER")));

            // Настройки сервера
            server.setExecutor(null); // использовать стандартный executor

            // Запуск сервера
            server.start();
            logger.info("Сервер работает...");

            // Запуск планировщика для проверки истекших кодов (раз в минуту)
            OtpExpiryScheduler scheduler = new OtpExpiryScheduler();
            scheduler.start();

            // Добавляем shutdown hook для корректного завершения
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Выключение HTTP-сервера...");
                server.stop(5); // Ожидание 5 секунд перед остановкой
                scheduler.stop();
                logger.info("Сервер остановлен.");
            }));

        } catch (IOException e) {
            logger.severe("Не удалось запустить HTTP-сервер: " + e.getMessage());
            System.exit(1);
        }
    }
}