package com.joatsy.apps.elibrarystta.view;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import static com.joatsy.apps.elibrarystta.view.MainActivity.session_user_addr;
import static com.joatsy.apps.elibrarystta.view.MainActivity.session_user_hp;
import static com.joatsy.apps.elibrarystta.view.MainActivity.session_user_id;
import static com.joatsy.apps.elibrarystta.view.MainActivity.user_agent;

public class SettingActivity extends AppCompatActivity {
    EditText tx_alamat;
    EditText tx_hp;
    EditText tx_pass1;
    EditText tx_pass2;
    Button btn_simpan;
    private ProgressDialog loading_dialog;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        tx_alamat = (EditText) findViewById(R.id.tx_setting_address);
        tx_hp = (EditText) findViewById(R.id.tx_setting_hp);
        tx_pass1 = (EditText) findViewById(R.id.tx_setting_password);
        tx_pass2 = (EditText) findViewById(R.id.tx_setting_password2);
        btn_simpan = (Button) findViewById(R.id.btn_setting_proc);
        tx_hp.setText(session_user_hp);
        tx_alamat.setText(session_user_addr);
        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getBaseContext(), "Tombol Login Ditekan" , Toast.LENGTH_LONG).show();
                String id_user = session_user_id;
                String pass1 = tx_pass1.getText().toString();
                String pass2 = tx_pass2.getText().toString();
                String alamat = tx_alamat.getText().toString();
                String hp = tx_hp.getText().toString();
                if (pass1.length()>0)
                {
                    //ganti password juga
                    if (pass1.length()>=8)
                    {
                        if (pass1.equals(pass2))
                        {
                            String md5_pass = convert_md5(pass1);
                            loading_dialog = new ProgressDialog(SettingActivity.this);
                            loading_dialog.setMessage("Mencoba mengubah data akun peminjam (" + id_user + "). Tunggu sebentar...");
                            loading_dialog.setIndeterminate(true);
                            loading_dialog.show();
                            new GetData().execute(SERVER_ADDRS + "akun.php?user_id=" + id_user + "&hp=" + hp + "&alamat=" + alamat + "&password=" + md5_pass, "akun");
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "Password tidak sama" , Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else
                {
                    loading_dialog = new ProgressDialog(SettingActivity.this);
                    loading_dialog.setMessage("Mencoba mengubah data akun peminjam (" + id_user + "). Tunggu sebentar...");
                    loading_dialog.setIndeterminate(true);
                    loading_dialog.show();
                    new GetData().execute(SERVER_ADDRS + "akun.php?user_id=" + id_user + "&hp=" + hp + "&alamat=" + alamat , "akun");
                }
            }
        });
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
            if (result.command.equals("akun")) {
                loading_dialog.dismiss();
                Log.i("URL login", result.url);
                Log.i("VALUE login", result.value);
                if (result.value.equals("OK"))
                {
                    Toast.makeText(getBaseContext(), "Berhasil mengubah data!" , Toast.LENGTH_LONG).show();
                    finish();
                }
                else
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(SettingActivity.this).create();
                    alertDialog.setTitle("Kesalahan");
                    alertDialog.setMessage("Data input anda salah");
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
