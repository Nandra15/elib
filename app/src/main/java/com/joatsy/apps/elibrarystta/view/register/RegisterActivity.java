package com.joatsy.apps.elibrarystta.view.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.joatsy.apps.elibrarystta.R;
import com.joatsy.apps.elibrarystta.base.BaseActivity;
import com.joatsy.apps.elibrarystta.network.ApiClient;
import com.joatsy.apps.elibrarystta.network.ApiInterface;
import com.joatsy.apps.elibrarystta.view.login.LoginActivity;

public class RegisterActivity extends BaseActivity implements RegisterView.view {
    Button btn_register, btn_login;
    EditText tx_email, tx_pass, tx_hp, tx_nama, tx_nim;

    String id_user;
    private RegisterPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        presenter = new RegisterPresenter(ApiClient.getClient().create(ApiInterface.class), this);
        btn_login = (Button) findViewById(R.id.btn_register_login);
        btn_register = (Button) findViewById(R.id.btn_register_proc);
        tx_email = (EditText) findViewById(R.id.tx_register_email);
        tx_pass = (EditText) findViewById(R.id.tx_register_password);
        tx_hp = (EditText) findViewById(R.id.tx_register_hp);
        tx_nama = (EditText) findViewById(R.id.tx_register_name);
        tx_nim = (EditText) findViewById(R.id.txt_nim);

        if (checkInet(1) == true) {
            btn_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getBaseContext(), "Tombol Login Ditekan" , Toast.LENGTH_LONG).show();
                    if (checkInet(1) == true) {
                        finish();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }


                }
            });

            btn_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getBaseContext(), "Tombol Register Ditekan" , Toast.LENGTH_LONG).show();
                    if (checkInet(1) == true) {
                        String nim = tx_nim.getText().toString();
                        String pass = tx_pass.getText().toString();
                        String hp = tx_hp.getText().toString();
                        String nama = tx_nama.getText().toString();
                        String email = tx_email.getText().toString();

                        if (nim.isEmpty() || nama.isEmpty() || email.isEmpty() || hp.isEmpty() || pass.isEmpty() ) {
                            showToast("Harap Lengkapi Data Diatas");
                        } else {
                            presenter.register(nama, nim, getMacAddr(), email, pass, hp);

                        }
                    }


                }
            });
        } else {
            System.exit(0);
        }
    }

    @Override
    public void success(String s) {
        hideLoading();
        finish();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        showToast(s);
    }

    @Override
    public void fail(String s) {
        hideLoading();
        showLongToast(s);
    }

    @Override
    public void loading() {
        showDialog("Mencoba mendaftarkan akun peminjam (" + id_user + "). Tunggu sebentar...");
    }

    @Override
    public void error(int message) {
        hideLoading();
        showLongToast(getString(message));
    }
}
