package com.joatsy.apps.elibrarystta.network;

import com.joatsy.apps.elibrarystta.Data.LoginResponse;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {
    @FormUrlEncoded
    @POST("auth")
    Observable<LoginResponse> JJJ(@Field("nim") String nim,
                                  @Field("password") String pass,
                                  @Field("mac_addr") String mac);


//    @FormUrlEncoded
//    @POST("kontak")
//    Call<PostPutDelKontak> FINDBOOK(@Field("nama") String nama,
//                                      @Field("nomor") String nomor);
//
//    @FormUrlEncoded
//    @PUT("kontak")
//    Call<PostPutDelKontak> putKontak(@Field("id") String id,
//                                     @Field("nama") String nama,
//                                     @Field("nomor") String nomor);
//
//    @FormUrlEncoded
//    @HTTP(method = "DELETE", path = "kontak", hasBody = true)
//    Call<PostPutDelKontak> deleteKontak(@Field("id") String id);
}