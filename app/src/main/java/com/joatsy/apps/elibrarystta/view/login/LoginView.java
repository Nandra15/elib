package com.joatsy.apps.elibrarystta.view.login;

import com.joatsy.apps.elibrarystta.base.BaseInterface;
import com.joatsy.apps.elibrarystta.base.BaseView;

import io.reactivex.disposables.Disposable;

abstract class LoginView extends BaseView {

    abstract Disposable login(String nama, String password);

    interface view extends BaseInterface {
        void success(String s);
    }
}