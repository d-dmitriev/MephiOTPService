package org.example.otp.service;

import org.example.otp.dao.UserDao;
import org.example.otp.model.User;
import org.example.otp.util.PasswordHasher;
import org.example.otp.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    public static final String ADMIN_EXISTS = "ADMIN_EXISTS";
    public static final String USER_EXISTS = "USER_EXISTS";

    private final UserDao userDao = new UserDao();
    private final PasswordHasher passwordHasher = new PasswordHasher();
    private final TokenUtil tokenUtil = new TokenUtil();

    /**
     * Регистрация нового пользователя.
     * Если роль ADMIN, проверяет, что администратор ещё не существует.
     */
    public String registerUser(String login, String password, String role) {
        if ("ADMIN".equals(role) && userDao.existsAdmin()) {
            logger.warn("Регистрация отклонена: Администратор уже существует");
            return ADMIN_EXISTS;
        }

        if (userDao.findByLogin(login).isPresent()) {
            logger.warn("Пользователь с таким логином уже существует: {}", login);
            return USER_EXISTS;
        }

        String hashedPassword = passwordHasher.hash(password);

        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(hashedPassword);
        user.setRole(role);
        userDao.save(user);

        logger.info("Новый пользователь зарегистрирован: " + login);
        return tokenUtil.generateToken(login);
    }

    /**
     * Аутентификация пользователя по логину и паролю.
     */
    public String authenticateUser(String login, String password) {
        var optionalUser = userDao.findByLogin(login);
        if (optionalUser.isEmpty()) {
            logger.warn("Пользователь не найден: {}", login);
            return null;
        }

        User user = optionalUser.get();
        if (!passwordHasher.check(password, user.getPasswordHash())) {
            logger.warn("Неверный пароль для пользователя: {}", login);
            return null;
        }

        logger.info("Пользователь успешно вошёл: " + login);
        return tokenUtil.generateToken(login);
    }

    /**
     * Получает ID пользователя по его логину.
     */
    public int getUserId(String username) {
        return userDao.findByLogin(username)
                .map(User::getId)
                .orElse(-1);
    }

    /**
     * Получает список всех пользователей кроме администратора.
     */
    public List<User> getAllUsersExceptAdmin() {
        return userDao.findAllUsersExceptAdmin();
    }

    /**
     * Удаляет пользователя по ID.
     */
    public void deleteUser(int userId) {
        userDao.deleteById(userId);
        logger.info("Пользователь удалён по ID: " + userId);
    }
}
