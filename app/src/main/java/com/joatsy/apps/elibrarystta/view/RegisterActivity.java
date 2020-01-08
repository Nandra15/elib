package com.joatsy.apps.elibrarystta.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.joatsy.apps.elibrarystta.R;
import com.joatsy.apps.elibrarystta.view.login.LoginActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.joatsy.apps.elibrarystta.view.MainActivity.SERVER_ADDRS;
import static com.joatsy.apps.elibrarystta.view.MainActivity.user_agent;

public class RegisterActivity extends AppCompatActivity {
    Button btn_register, btn_login;
    EditText tx_user_id, tx_pass, tx_hp, tx_nama;
    private ProgressDialog loading_dialog;
    String id_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btn_login = (Button) findViewById(R.id.btn_register_login);
        btn_register = (Button) findViewById(R.id.btn_register_proc);
        tx_user_id = (EditText) findViewById(R.id.tx_register_email);
        tx_pass = (EditText) findViewById(R.id.tx_register_password);
        tx_hp = (EditText) findViewById(R.id.tx_register_hp);
        tx_nama = (EditText) findViewById(R.id.tx_register_name);

        if (checkInet(1)==true)
        {
            btn_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getBaseContext(), "Tombol Login Ditekan" , Toast.LENGTH_LONG).show();
                    if (checkInet(1)==true)
                    {
                        finish();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity (intent);
                    }


                }
            });

            btn_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getBaseContext(), "Tombol Register Ditekan" , Toast.LENGTH_LONG).show();
                    if (checkInet(1)==true)
                    {
                        id_user= tx_user_id.getText().toString();
                        String pass = tx_pass.getText().toString();
                        String hp = tx_hp.getText().toString();
                        String nama = tx_nama.getText().toString().replace(" ","%20");
                        String md5_pass = convert_md5(pass);
                        loading_dialog = new ProgressDialog(RegisterActivity.this);
                        loading_dialog.setMessage("Mencoba mendaftarkan akun peminjam (" + id_user + "). Tunggu sebentar...");
                        loading_dialog.setIndeterminate(true);
                        loading_dialog.show();
                        new GetData().execute(SERVER_ADDRS + "register.php?user_id=" + id_user + "&name=" + nama + "&hp=" + hp + "&password=" + md5_pass, "register");
                    }
                }
            });
        }
        else
        {
            System.exit(0);
        }
    }

    String convert_md5(String s) {
        String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
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

    public boolean checkInet(int msg)
    {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else {
            connected = false;
            if (msg==1)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
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

    public class GetDataResult
    {
        public String value;
        public String command;
        public String url;
    }

    public class GetData extends AsyncTask<String, String, GetDataResult> {

        @Override
        protected GetDataResult doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String result="";
            GetDataResult rslt = new GetDataResult();
            rslt.url=params[0];
            rslt.value="";
            rslt.command=params[1];
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Accept-Encoding", "");
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("User-Agent", user_agent);
                urlConnection.setConnectTimeout(30000);
                urlConnection.setReadTimeout(30000);

                int code = urlConnection.getResponseCode();

                if(code==200){
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    if (in != null) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        String line = "";

                        while ((line = bufferedReader.readLine()) != null)
                            result += line;
                    }
                    in.close();
                }
                rslt.value=result;
                rslt.url=params[0];
                rslt.command=params[1];
                return rslt;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            finally {
                urlConnection.disconnect();
            }
            return rslt;

        }

        protected void onPostExecute(GetDataResult result) {
            if (result.command.equals("register")) {
                loading_dialog.dismiss();
                Log.i("URL login", result.url);
                Log.i("VALUE login", result.value);
                if (result.value.equals("OK"))
                {
                    Toast.makeText(getBaseContext(), "Silahkan tunggu konfirmasi admin untuk aktifasi akun anda!" , Toast.LENGTH_LONG).show();
                    finish();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("user_id", id_user);
                    startActivity(intent);
                }
                else
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
                    alertDialog.setTitle("Kesalahan");
                    alertDialog.setMessage("Akun atau nomor hp anda sudah terdaftar");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        }


    }
}
