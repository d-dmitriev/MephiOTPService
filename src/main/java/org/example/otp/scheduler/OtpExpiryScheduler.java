package org.example.otp.scheduler;

import org.example.otp.dao.OtpCodeDao;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class OtpExpiryScheduler {
    private static final Logger logger = Logger.getLogger(OtpExpiryScheduler.class.getName());
    private final Timer timer = new Timer();
    private static final long INITIAL_DELAY = 0;        // Начать сразу
    private static final long PERIOD = 60_000;         // Каждую минуту

    private final OtpCodeDao otpCodeDao = new OtpCodeDao();

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info("Запуск запланированной задачи по удалению старых одноразовых кодов...");
                    otpCodeDao.markAllExpired();
                } catch (Exception e) {
                    logger.severe("Ошибка при проверке срока действия одноразового пароля: " + e.getMessage());
                }
            }
        }, INITIAL_DELAY, PERIOD);
        logger.info("Планировщик срока действия OTP запущен. Проверка каждые " + (PERIOD / 1000) + " секунд.");
    }

    public void stop() {
        timer.cancel();
        logger.info("Планировщик истечения срока действия OTP остановлен.");
    }
}
