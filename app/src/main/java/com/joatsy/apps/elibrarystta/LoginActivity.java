package com.joatsy.apps.elibrarystta;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

import static com.joatsy.apps.elibrarystta.MainActivity.SERVER_ADDRS;
import static com.joatsy.apps.elibrarystta.MainActivity.session_user_addr;
import static com.joatsy.apps.elibrarystta.MainActivity.session_user_hp;
import static com.joatsy.apps.elibrarystta.MainActivity.session_user_id;
import static com.joatsy.apps.elibrarystta.MainActivity.session_user_name;
import static com.joatsy.apps.elibrarystta.MainActivity.session_user_nim;
import static com.joatsy.apps.elibrarystta.MainActivity.user_agent;

public class LoginActivity extends AppCompatActivity {
    EditText txUserId;
    EditText txPassword;
    Button bLogin;
    Button bRegister;
    String id_user;
    String nim;
    private ProgressDialog loading_dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txUserId = (EditText) findViewById(R.id.tx_login_email);
        txPassword = (EditText) findViewById(R.id.tx_login_password);
        bLogin = (Button) findViewById(R.id.btn_login_proc);
        bRegister = (Button) findViewById(R.id.btn_login_register);

        if (checkInet(1)==true)
        {
            bLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getBaseContext(), "Tombol Login Ditekan" , Toast.LENGTH_LONG).show();
                    if (checkInet(1)==true)
                    {
                        nim = txUserId.getText().toString();
                        String pass = txPassword.getText().toString();
                        String md5_pass = convert_md5(pass);
                        String parameters = "nim=" + nim + "&password=" + pass + "&mac_addr=123456789";
                        loading_dialog = new ProgressDialog(LoginActivity.this);
                        loading_dialog.setMessage("Mencoba login akun peminjam (" + nim + "). Tunggu sebentar...");
                        loading_dialog.setIndeterminate(true);
                        loading_dialog.show();
                        //new GetData().execute(SERVER_ADDRS + "login.php?user_id=" + id_user + "&password=" + md5_pass, "login");
                        new PostData().execute(SERVER_ADDRS + "auth", "login",parameters);
                    }

                }
            });

            bRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getBaseContext(), "Tombol Register Ditekan" , Toast.LENGTH_LONG).show();
                    if (checkInet(1)==true) {
                        finish();
                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
        else
        {
            System.exit(0);
        }

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(LoginActivity.this)
                .setCancelable(false)
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin tidak jadi login?")
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                })
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        System.exit(0);
                    }
                })
                .create().show();
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
                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
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

    public class PostDataResult
    {
        public String value;
        public String command;
        public String url;
        public String parameter;
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
            if (result.command.equals("account")) {
                loading_dialog.dismiss();
                Log.i("URL account", result.url);
                Log.i("VALUE account", result.value);
                String nama = "";
                String email="";
                String nimx="";
                String no_telp="";
                String user_id="";
                int login_success=0;
                try {

                    JSONObject json = new JSONObject(result.value);
                    JSONArray userArray = json.getJSONArray("data");
                    for (int i = 0; i < userArray.length(); i++) {
                        // create a JSONObject for fetching single user data
                        JSONObject userDetail = userArray.getJSONObject(i);
                        /*
                        {"status":true,"data":[{"id":"3","nama":"Tri Wahyuningsih","nim":"15030038","email":"triwhy@gmail.com","no_telp":"085643587657","password":"$2y$10$d3yf5QypEki38zmI2hsmJecxkIuv.0A.gOGdICrXcmIrPG7VNCKaW","status":"1","mac_address":"ashdkhsjdhja","created_at":"2019-08-17 08:33:52","updated_at":"2019-08-17 08:33:52"}]}
                         */
                        // fetch email and name and store it in arraylist
                        user_id =userDetail.getString("id");
                        nama = userDetail.getString("nama");
                        nimx = userDetail.getString("nim");
                        no_telp = userDetail.getString("no_telp");
                        email = userDetail.getString("email");
                        if (nimx.equals(nim)){
                            login_success=1;
                            session_user_id=user_id;
                            session_user_nim=nim;
                            session_user_name=nama;
                            session_user_hp=no_telp;
                            session_user_addr=email;
                            break;
                        }
                    }
                    if (login_success==1){
                        Toast.makeText(getBaseContext(), "Berhasil login sebagai " + session_user_name + "( " + session_user_addr +  " )" , Toast.LENGTH_LONG).show();
                        finish();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("session_user_id", session_user_id);
                        intent.putExtra("session_user_nim", session_user_nim);
                        intent.putExtra("session_user_name", session_user_name);
                        intent.putExtra("session_user_hp", session_user_hp);
                        intent.putExtra("session_user_addr", session_user_addr);
                        startActivity(intent);
                    } else {

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    public class PostData extends AsyncTask<String, String, PostDataResult> {

        @Override
        protected PostDataResult doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            PostDataResult rslt = new PostDataResult();
            Log.e("param", params[0]);
            Log.e("param", params[1]);
            Log.e("param", params[2]);
            rslt.url=params[0];
            rslt.value="";
            rslt.parameter=params[2];
            rslt.command=params[1];
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setReadTimeout(20000);
                urlConnection.setConnectTimeout(20000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
                writer.write(rslt.parameter);
                writer.flush();
                writer.close();
                os.close();

                int responseCode=urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader( new InputStreamReader(urlConnection.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";
                    while((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    in.close();
                    rslt.value=sb.toString();
                    rslt.url=params[0];
                    rslt.command=params[1];
                    return rslt;
                }
                return null;
            } catch (MalformedURLException e) {
                Log.e("login malf", e.getMessage());
            } catch (IOException e) {
                Log.e("login io", e.getMessage());
            }

            finally {
                urlConnection.disconnect();
            }
            return rslt;

        }

        protected void onPostExecute(PostDataResult result) {
            if (result.command.equals("login")) {
                loading_dialog.dismiss();
                if (result.value.equals("{\"status\":true,\"message\":\"Login succeed.\"}"))
                {
                    loading_dialog = new ProgressDialog(LoginActivity.this);
                    loading_dialog.setMessage("Mencoba mendapatkan data peminjam (" + nim + "). Tunggu sebentar...");
                    loading_dialog.setIndeterminate(true);
                    loading_dialog.show();
                    new GetData().execute(SERVER_ADDRS + "auth" , "account");

                }
                else
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                    alertDialog.setTitle("Kesalahan");
                    alertDialog.setMessage("Akun atau password anda salah");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
            /*else if (result.command.equals("account")) {
                loading_dialog.dismiss();
                Log.i("URL account", result.url);
                Log.i("VALUE account", result.value);
                String nama = "";
                String email="";
                String nim="";
                String no_telp="";
                int login_success=0;
                try {

                    JSONObject json = new JSONObject(result.value);
                    JSONArray userArray = json.getJSONArray("data");
                    for (int i = 0; i < userArray.length(); i++) {
                        JSONObject userDetail = userArray.getJSONObject(i);
                        nama = userDetail.getString("nama");
                        nim = userDetail.getString("nim");
                        no_telp = userDetail.getString("no_telp");
                        email = userDetail.getString("email");
                        if (nim.equals(id_user)){
                            login_success=1;
                            session_user_name=nama;
                            session_user_hp=no_telp;
                            session_user_addr=email;
                            break;
                        }
                    }
                    if (login_success==1){
                        Toast.makeText(getBaseContext(), "Berhasil login!" , Toast.LENGTH_LONG).show();
                        finish();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("session_user_id", id_user);
                        intent.putExtra("session_user_name", session_user_name);
                        intent.putExtra("session_user_hp", session_user_hp);
                        intent.putExtra("session_user_addr", session_user_addr);
                        startActivity(intent);
                    } else {

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            */
        }


    }
}
