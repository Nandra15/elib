package com.joatsy.apps.elibrarystta.network;

import com.joatsy.apps.elibrarystta.Data.LoginResponse;
import com.joatsy.apps.elibrarystta.Data.ProfilResponse;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {
    @FormUrlEncoded
    @POST("auth")
    Observable<LoginResponse> JJJ(@Field("nim") String nim,
                                  @Field("password") String pass,
                                  @Field("mac_addr") String mac);

 @FormUrlEncoded
    @POST("auth/register")
    Observable<LoginResponse> register(@Field("nim") String nim,
                                  @Field("nama") String nama,
                                  @Field("email") String email,
                                  @Field("no_telp") String telp,
                                  @Field("password") String pass,
                                  @Field("mac_addr") String mac);

    @GET("auth")
    Single<ProfilResponse> getProfil(@Query("id") int nim);

    //{
    //    "status": false,
    //    "message": "Nim sudah terdaftar."
    //}

    //{
    //    "status": true,
    //    "message": "Registration succeed."
    //}


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