package com.joatsy.apps.elibrarystta.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {
    protected ProgressDialog loading_dialog;

    protected boolean checkInet(int msg) {
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else {
            connected = false;
            if (msg == 1) {
                AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
                alertDialog.setTitle("Kesalahan");
                alertDialog.setMessage("Tidak ditemukan koneksi internet");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }

        }
        return connected;
    }

    protected void showDialog(String s) {
        loading_dialog = new ProgressDialog(this);
        loading_dialog.setMessage(s);
        loading_dialog.setIndeterminate(true);
        if (!loading_dialog.isShowing())
        loading_dialog.show();
    }

    protected void hideLoading() {
        loading_dialog.dismiss();
    }

    protected void showToast(String mess){
        Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
    }


}
