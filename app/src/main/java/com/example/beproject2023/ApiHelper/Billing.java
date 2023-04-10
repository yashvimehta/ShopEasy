package com.example.beproject2023.ApiHelper;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class Billing {
    final private boolean success;
    final private String error;

    public Billing(boolean success, String error ) {
        this.success=success;
        this.error=error;
    }

    public boolean getGeneralSuccess() {
        return success;
    }
    public String getBillingError() {
        return error;
    }
}
