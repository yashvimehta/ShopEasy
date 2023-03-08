package com.example.beproject2023.ApiHelper;

public class VTRResult {
    final private boolean success;
    final private String img;
    final private String error;

    public VTRResult(boolean success, String img, String error) {
        this.success=success;
        this.img=img;
        this.error=error;
    }

    public boolean getGeneralSuccess() {
        return success;
    }
    public String getVTRText() {
        return img;
    }
    public String getVTRError() {
        return error;
    }
}
