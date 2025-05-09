package org.example.otp.scheduler;

import org.example.otp.dao.OtpCodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class OtpExpiryScheduler {
    private static final Logger logger = LoggerFactory.getLogger(OtpExpiryScheduler.class);
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
                    logger.error("Ошибка при проверке срока действия одноразового пароля: {}", e.getMessage());
                }
            }
        }, INITIAL_DELAY, PERIOD);
        logger.info("Планировщик срока действия OTP запущен. Проверка каждые {} секунд.", (PERIOD / 1000));
    }

    public void stop() {
        timer.cancel();
        logger.info("Планировщик истечения срока действия OTP остановлен.");
    }
}
