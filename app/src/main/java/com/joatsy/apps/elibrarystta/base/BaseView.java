package com.joatsy.apps.elibrarystta.base;

import retrofit2.HttpException;

public class BaseView {
    protected boolean isOutOfNetwork(Throwable throwable) {
        return throwable instanceof HttpException &&
                ((HttpException) throwable).code() <= 400 &&
                ((HttpException) throwable).code() >= 500;
    }
}