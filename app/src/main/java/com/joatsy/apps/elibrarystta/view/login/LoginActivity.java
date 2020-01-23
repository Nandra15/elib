package com.joatsy.apps.elibrarystta.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.joatsy.apps.elibrarystta.R;
import com.joatsy.apps.elibrarystta.base.BaseActivity;
import com.joatsy.apps.elibrarystta.network.ApiClient;
import com.joatsy.apps.elibrarystta.network.ApiInterface;
import com.joatsy.apps.elibrarystta.utils.SharedPrefs;
import com.joatsy.apps.elibrarystta.view.MainActivity;
import com.joatsy.apps.elibrarystta.view.RegisterActivity;

import static com.joatsy.apps.elibrarystta.utils.SharedPrefs.NIM;
import static com.joatsy.apps.elibrarystta.utils.SharedPrefs.PROFIL;

public class LoginActivity extends BaseActivity implements LoginView.view {
    private EditText txUserId, txPassword;
    private Button bLogin, bRegister;
    private String nim;
    private LoginPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txUserId = findViewById(R.id.tx_login_email);
        txPassword = findViewById(R.id.tx_login_password);
        bLogin = findViewById(R.id.btn_login_proc);
        bRegister = findViewById(R.id.btn_login_register);



        presenter = new LoginPresenter(ApiClient.getClient().create(ApiInterface.class), this);

        if (checkInet(1)) {
            bLogin.setOnClickListener(view -> {
                nim = txUserId.getText().toString();
                String pass = txPassword.getText().toString();
                presenter.login(nim, pass);

            });

            bRegister.setOnClickListener(view -> {
                if (checkInet(1)) {
                    finish();
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
        } else System.exit(0);

    }

    private void toHome() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }


    @Override
    public void success(String s) {
        Log.e("success", s + " ");
        hideLoading();
        presenter.getprofil(nim);
    }

    @Override
    public void profil(String s) {

        Log.e("tes", s);
        SharedPrefs prefs = new SharedPrefs(this);
        prefs.saveSPBoolean(SharedPrefs.IS_LOGED_IN, true);
        prefs.saveSPInt(NIM, Integer.parseInt(nim));
        prefs.saveSPString(PROFIL, s);
        toHome();
    }

    @Override
    public void loading() {
        showDialog("Mencoba login. Tunggu sebentar...");
    }

    @Override
    public void error(int message) {
        hideLoading();
        Toast.makeText(this, this.getString(message), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(LoginActivity.this)
                .setCancelable(false)
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin tidak jadi login?")
                .setNegativeButton("Tidak", (arg0, arg1) -> {

                })
                .setPositiveButton("Ya", (arg0, arg1) -> System.exit(0))
                .create().show();
    }

}
