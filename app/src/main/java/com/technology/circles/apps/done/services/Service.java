package com.technology.circles.apps.done.services;


import com.technology.circles.apps.done.models.AppDataModel;
import com.technology.circles.apps.done.models.MyAlertModel;
import com.technology.circles.apps.done.models.SingleAlertModel;
import com.technology.circles.apps.done.models.UserModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Service {
    @GET("api/settings")
    Call<AppDataModel> getSetting(@Header("lang") String lang);

    @FormUrlEncoded
    @POST("api/user-register")
    Call<UserModel> signUpWithoutImage(@Field("name") String name,
                                       @Field("phone") String phone,
                                       @Field("phone_code") String phone_code,
                                       @Field("email") String email,
                                       @Field("soft_type") String soft_type
    );


    @Multipart
    @POST("api/user-register")
    Call<UserModel> signUpWithImage(@Part("name") RequestBody name,
                                    @Part("phone") RequestBody phone,
                                    @Part("phone_code") RequestBody phone_code,
                                    @Part("email") RequestBody email,
                                    @Part("soft_type") RequestBody soft_type,
                                    @Part MultipartBody.Part image
    );

    @FormUrlEncoded
    @POST("api/login")
    Call<UserModel> login(@Field("phone") String phone,
                          @Field("phone_code") String phone_code
    );

    @Multipart
    @POST("api/alert/make")
    Call<ResponseBody> makeAlertWithSound(@Header("Authorization") String token,
                                          @Part("local_id") RequestBody local_id,
                                          @Part("time_int") RequestBody time_int,
                                          @Part("date_int") RequestBody date_int,
                                          @Part("alert_type") RequestBody alert_type,
                                          @Part("is_alert") RequestBody is_alert,
                                          @Part("is_inner_call") RequestBody is_inner_call,
                                          @Part("is_outer_call") RequestBody is_outer_call,
                                          @Part("is_sound") RequestBody is_sound,
                                          @Part("details") RequestBody details,
                                          @Part("state_alert") RequestBody state_alert,
                                          @Part("date_str") RequestBody date_str,
                                          @Part MultipartBody.Part sound
    );


    @Multipart
    @POST("api/alert/make")
    Call<ResponseBody> makeAlertWithoutSound(@Header("Authorization") String token,
                                             @Part("local_id") RequestBody local_id,
                                             @Part("time_int") RequestBody time_int,
                                             @Part("date_int") RequestBody date_int,
                                             @Part("alert_type") RequestBody alert_type,
                                             @Part("is_alert") RequestBody is_alert,
                                             @Part("is_inner_call") RequestBody is_inner_call,
                                             @Part("is_outer_call") RequestBody is_outer_call,
                                             @Part("is_sound") RequestBody is_sound,
                                             @Part("details") RequestBody details,
                                             @Part("state_alert") RequestBody state_alert,
                                             @Part("date_str") RequestBody date_str
    );


    @GET("api/my-alerts")
    Call<MyAlertModel> getMyAlert(@Header("Authorization") String user_token);

    @GET("api/alert/one")
    Call<SingleAlertModel> getSingleAlert(@Header("Authorization") String user_token,
                                          @Query("local_id") String local_id
    );

}


