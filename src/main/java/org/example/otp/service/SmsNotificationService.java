package org.example.otp.service;

import org.smpp.*;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.PDUException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class SmsNotificationService extends SmppObject {
    private static final Logger logger = Logger.getLogger(SmsNotificationService.class.getName());

    private String host;
    private int port;
    private String systemId;
    private String password;
    private String systemType;
    private String sourceAddress;

    public SmsNotificationService() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("sms.properties")) {
            Properties props = new Properties();
            props.load(input);

            this.host = props.getProperty("smpp.host");
            this.port = Integer.parseInt(props.getProperty("smpp.port"));
            this.systemId = props.getProperty("smpp.system_id");
            this.password = props.getProperty("smpp.password");
            this.systemType = props.getProperty("smpp.system_type");
            this.sourceAddress = props.getProperty("smpp.source_addr");
        } catch (Exception e) {
            logger.severe("Не удалось загрузить конфигурацию SMS: " + e.getMessage());
        }
    }

    public void sendCode(String destination, String code) {
        TCPIPConnection connection = new TCPIPConnection(host, port);
        Session session = new Session(connection);

        try {
            BindTransmitter bindRequest = new BindTransmitter();
            bindRequest.setSystemId(systemId);
            bindRequest.setPassword(password);
            bindRequest.setSystemType(systemType);
            bindRequest.setInterfaceVersion((byte) 0x34); // SMPP v3.4
            bindRequest.setAddressRange(sourceAddress);

            BindResponse bindResponse = session.bind(bindRequest);
            if (bindResponse.getCommandStatus() != 0) {
                throw new Exception("Связка не удалась: " + bindResponse.getCommandStatus());
            }

            SubmitSM submitSM = new SubmitSM();
            submitSM.setSourceAddr(sourceAddress);
            submitSM.setDestAddr(destination);
            submitSM.setShortMessage("Your OTP code: " + code);

            session.submit(submitSM);
            logger.info("Код OTP отправлен по SMS на номер: " + destination);
        } catch (Exception e) {
            logger.severe("Не удалось отправить SMS: " + e.getMessage());
        } finally {
            try {
                session.unbind();
                connection.close();
            } catch (PDUException | IOException | TimeoutException | WrongSessionStateException e) {
                logger.severe("Ошибка закрытия сеанса SMPP: " + e.getMessage());
            }
        }
    }
}