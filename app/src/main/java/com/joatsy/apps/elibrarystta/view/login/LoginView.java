package com.joatsy.apps.elibrarystta.view.login;

import com.joatsy.apps.elibrarystta.base.BaseInterface;
import com.joatsy.apps.elibrarystta.base.BaseView;

import io.reactivex.disposables.Disposable;

abstract class LoginView extends BaseView {

    abstract Disposable login(String nama, String password, String maccadress);
    abstract Disposable getprofil(String nim);

    interface view extends BaseInterface {
        void success(String s);
        void profil(String s);
    }
}