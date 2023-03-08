package com.example.beproject2023.ApiHelper;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class RecommendationResult {
    final private boolean success;
    final private String img1;
    final private String img2;
    final private String img3;
    final private String img4;
    final private String img5;
    final private String error;

    public RecommendationResult(boolean success, String img1, String img2,String img3,String img4, String img5 ,String error ) {
        this.success=success;
        this.img1=img1;
        this.img2=img2;
        this.img3=img3;
        this.img4=img4;
        this.img5=img5;
        this.error=error;
    }

    public boolean getGeneralSuccess() {
        return success;
    }
    public List<String> getVTRText() {
        List<String> vtrtext = new ArrayList<String>();
        vtrtext.add(img1);
        vtrtext.add(img2);
        vtrtext.add(img3);
        vtrtext.add(img4);
        vtrtext.add(img5);
        return vtrtext;
    }
    public String getVTRError() {
        return error;
    }
}
