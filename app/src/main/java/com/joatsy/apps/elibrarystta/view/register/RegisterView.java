package com.joatsy.apps.elibrarystta.view.register;

import com.joatsy.apps.elibrarystta.base.BaseInterface;
import com.joatsy.apps.elibrarystta.base.BaseView;

import io.reactivex.disposables.Disposable;

abstract class RegisterView extends BaseView {

    abstract Disposable register(String nama,
                                 String nim,
                                 String maccadress,
                                 String email,
                                 String pass,
                                 String telp);

    interface view extends BaseInterface {
        void success(String s);
        void fail(String s);
    }
}