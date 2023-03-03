package com.example.antilight;

public class IpAddress {
    private String ipAddress;
    private boolean isSuccessful;

    public IpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        this.isSuccessful = false;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }
}
