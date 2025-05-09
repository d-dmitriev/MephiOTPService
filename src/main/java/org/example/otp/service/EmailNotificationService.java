package org.example.otp.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class EmailNotificationService {
    private static final Logger logger = Logger.getLogger(EmailNotificationService.class.getName());

    private final Properties emailProps = new Properties();
    private Session session;
    private String fromEmail;

    public EmailNotificationService() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            emailProps.load(input);
            this.fromEmail = emailProps.getProperty("email.from");

            session = Session.getInstance(emailProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            emailProps.getProperty("email.username"),
                            emailProps.getProperty("email.password"));
                }
            });
        } catch (Exception e) {
            logger.severe("Не удалось загрузить конфигурацию электронной почты: " + e.getMessage());
        }
    }

    public void sendCode(String toEmail, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Ваш OTP код");
            message.setText("Ваш код подтверждения: " + code);

            Transport.send(message);
            logger.info("Код OTP отправлен по электронной почте: " + toEmail);
        } catch (MessagingException | RuntimeException e) {
            logger.severe("Не удалось отправить email: " + e.getMessage());
        }
    }
}
