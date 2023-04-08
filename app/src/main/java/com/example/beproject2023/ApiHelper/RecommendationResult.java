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
    final private String img1_name;
    final private String img2_name;
    final private String img3_name;
    final private String img4_name;
    final private String img5_name;
    final private String error;

    public RecommendationResult(boolean success, String img1, String img1_name , String img2, String img2_name , String img3, String img3_name , String img4, String img4_name, String img5 , String img5_name, String error ) {
        this.success=success;
        this.img1=img1;
        this.img2=img2;
        this.img3=img3;
        this.img4=img4;
        this.img5=img5;
        this.img1_name=img1_name;
        this.img2_name=img2_name;
        this.img3_name=img3_name;
        this.img4_name=img4_name;
        this.img5_name=img5_name;
        this.error=error;
    }

    public boolean getGeneralSuccess() {
        return success;
    }
    public List<String> getRecommendationText() {
        List<String> vtrtext = new ArrayList<String>();
        vtrtext.add(img1);
        vtrtext.add(img2);
        vtrtext.add(img3);
        vtrtext.add(img4);
        vtrtext.add(img5);
        vtrtext.add(img1_name);
        vtrtext.add(img2_name);
        vtrtext.add(img3_name);
        vtrtext.add(img4_name);
        vtrtext.add(img5_name);
        return vtrtext;
    }
    public String getRecommendationError() {
        return error;
    }
}
