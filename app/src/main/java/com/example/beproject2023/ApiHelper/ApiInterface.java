package com.example.beproject2023.ApiHelper;

import android.app.appsearch.SearchResult;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiInterface {

    String BASE_URL_PREDICTOR= "http://4.240.49.41:5000/";
//    @Multipart
//    @POST("food-predictor")
//    Call<FoodPredictorResult> sendImage(@Part MultipartBody.Part image);
//
//    @GET("similar-recommendation")
//    Call<FoodRecommendationResult> getSimilarFoodItems(@Query("item-name") String itemName);
//
//    @GET("user-recommendation")
//    Call<FoodRecommendationResult> getUserRecommendation(@Query("user-uid") String userUid);

//    @Multipart
//    @POST("identify")
//    Call<IdentifyResult> sendImage(@Part MultipartBody.Part image);

    @Multipart
    @POST("detect")
    Call<BarcodeResult> sendBarcodeImage(@Part MultipartBody.Part image);

    @Multipart
    @POST("fit")
    Call<VTRResult> sendVTRImage(@Part MultipartBody.Part img1 , @Part MultipartBody.Part img2 );

    @Multipart
    @POST("recommend")
    Call<RecommendationResult> sendRecImage(@Part MultipartBody.Part image );

    @GET("mail")
    Call<Billing> sendMail(@Query("recipient")  String recipient, @Query("item")  String item, @Query("qty")  String qty, @Query("cost") String cost);
}


