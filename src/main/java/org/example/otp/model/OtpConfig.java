package org.example.otp.model;

public class OtpConfig {
    private int codeLength;
    private int expirationTime; // in seconds

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    public int getCodeLength() {
        return this.codeLength;
    }

    public int getExpirationTime() {
        return this.expirationTime;
    }
}