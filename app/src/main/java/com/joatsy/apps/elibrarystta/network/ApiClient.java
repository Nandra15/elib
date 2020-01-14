package com.joatsy.apps.elibrarystta.network;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String BASE_URL = "http://192.168.43.180:8000/elibrary/api/v1/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        CookieManager cookieManager = new CookieManager();
//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);


        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request request =
                                chain.request().newBuilder().addHeader("Authorization", "Bearer " + "").build();
                        return chain.proceed(request);
                    })
//                    .addInterceptor(interceptor)
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .addNetworkInterceptor(chain -> {
                        Request request = chain.request();
                        Request builder = request.newBuilder()
                                .addHeader("Cookie", "KEY-VALUE").build();
                        return chain.proceed(builder);
                    })
                    .protocols(Arrays.asList(Protocol.HTTP_1_1))

                    .build();

            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }
}