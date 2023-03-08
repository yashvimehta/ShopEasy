package com.example.beproject2023.ApiHelper;

public class BarcodeResult {
    final private boolean success;
    final private String ans;
    final private String error;

    public BarcodeResult(boolean success,String ans, String error) {
        this.success=success;
        this.ans=ans;
        this.error=error;
    }

    public boolean getGeneralSuccess() {
        return success;
    }
    public String getBarcodeText() {
        return ans;
    }
    public String getBarcodeError() {
        return error;
    }
}
