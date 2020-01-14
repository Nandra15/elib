package com.joatsy.apps.elibrarystta.base;

import retrofit2.HttpException;

public class BaseView {
    protected boolean isOutOfNetwork(Throwable throwable) {
        return throwable instanceof HttpException &&
                ((HttpException) throwable).code() <= 400 &&
                ((HttpException) throwable).code() >= 500;
    }

    protected String getErrorBody(Throwable throwable) {
        if (throwable instanceof HttpException)
        return ((HttpException)throwable).response().body()+"";
        else return "NO_ERROR_BODY";
    }
}