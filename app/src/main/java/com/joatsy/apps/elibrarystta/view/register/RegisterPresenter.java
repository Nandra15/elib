package com.joatsy.apps.elibrarystta.view.register;

import android.util.Log;

import com.google.gson.Gson;
import com.joatsy.apps.elibrarystta.R;
import com.joatsy.apps.elibrarystta.network.ApiInterface;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RegisterPresenter extends RegisterView {
    private ApiInterface apiInterface;
    private RegisterView.view view;

    public RegisterPresenter(ApiInterface apiInterface, RegisterView.view view) {
        this.apiInterface = apiInterface;
        this.view = view;
    }

    @Override
    Disposable register(String nama, String nim, String maccadress, String email, String pass, String telp) {
        view.loading();
        String md5_pass = convert_md5(pass);
        return apiInterface.register(nim, nama, email, telp, pass, maccadress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(loginResponse -> {
                            if (loginResponse.isStatus())
                                view.success(loginResponse.getMessage());
                            else view.fail(loginResponse.getMessage());
                        },
                        throwable -> {
                            if (isOutOfNetwork(throwable))
                                view.error(R.string.out_of_network);
                            else
                                view.error(R.string.general_error);
                            Log.e("error login", throwable.getMessage() + ":" + getErrorBody(throwable));
                        });
    }

    private String convert_md5(String s) {
        String MD5 = "MD5";
        try {
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
