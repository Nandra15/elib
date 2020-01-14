package com.joatsy.apps.elibrarystta.view.login;

import android.util.Log;

import com.joatsy.apps.elibrarystta.R;
import com.joatsy.apps.elibrarystta.network.ApiInterface;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LoginPresenter extends LoginView {
    private ApiInterface apiInterface;
    private LoginView.view view;

    public LoginPresenter(ApiInterface apiInterface, LoginView.view view) {
        this.apiInterface = apiInterface;
        this.view = view;
    }

    @Override
    Disposable login(String nim, String password) {
        view.loading();
        String md5_pass = convert_md5(password);
        return apiInterface.JJJ(nim, md5_pass, "123")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(loginResponse -> {
                            view.success(loginResponse.toString());
                        },
                        throwable -> {
                            if (isOutOfNetwork(throwable))
                                view.error(R.string.out_of_network);
                            else
                                view.error(R.string.general_error);
                            Log.e("error", throwable.getMessage() + ":" + getErrorBody(throwable));
                        });
    }


    private String convert_md5(String s) {
        String MD5 = "MD5";
        try {
            MessageDigest digest = java.security.MessageDigest
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
