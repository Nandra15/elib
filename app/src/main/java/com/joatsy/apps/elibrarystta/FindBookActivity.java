package com.joatsy.apps.elibrarystta;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static com.joatsy.apps.elibrarystta.MainActivity.SERVER_ADDRS;
import static com.joatsy.apps.elibrarystta.MainActivity.user_agent;

public class FindBookActivity  extends AppCompatActivity  {
    Spinner sp_category;
    Button btn_find;
    EditText tx_name;
    String kategori;
    ArrayList<String> arraylist = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    private ProgressDialog loading_dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findbook);
        sp_category = (Spinner) findViewById(R.id.sp_find_category);
        btn_find = (Button) findViewById(R.id.btn_find_proc);
        tx_name = (EditText) findViewById(R.id.tx_find_name);
        //ArrayAdapter<String> adapter = ArrayAdapter.createFromResource(this,R.array.numbers, android.R.layout.simple_spinner_item );
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arraylist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arraylist.add("");
        sp_category.setAdapter(adapter);
        sp_category.setOnItemSelectedListener(new myOnItemSelectedListener());
        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getBaseContext(), "Tombol Login Ditekan" , Toast.LENGTH_LONG).show();
                finish();
                Intent intent = new Intent(FindBookActivity.this, BookActivity.class);
                intent.putExtra("kategori", kategori);
                intent.putExtra("nama", tx_name.getText().toString());
                startActivity (intent);
            }
        });
        loading_dialog = new ProgressDialog(FindBookActivity.this);
        loading_dialog.setMessage("Mencoba mengambil data kategori. Tunggu sebentar...");
        loading_dialog.setIndeterminate(true);
        loading_dialog.show();
        new GetData().execute(SERVER_ADDRS + "categories", "category");
    }

    public class myOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View arg1, int pos,long arg3) {
            String text = parent.getItemAtPosition(pos).toString();
            //Toast.makeText(getBaseContext(), "List " + text + " Ditekan", Toast.LENGTH_LONG).show();
            kategori = text;
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {        }
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
            if (result.command.equals("category")) {
                loading_dialog.dismiss();
                Log.i("URL login", result.url);
                Log.i("VALUE login", result.value);
                try {
                    int count_data=0;
                    JSONObject json = new JSONObject(result.value);
                    JSONArray userArray = json.getJSONArray("data");
                    for (int i = 0; i < userArray.length(); i++) {
                        count_data++;
                        JSONObject userDetail = userArray.getJSONObject(i);
                        String kategori_item = userDetail.getString("name");
                        if (count_data==1)
                        {
                            kategori="";
                            arraylist.clear();
                            arraylist.add("");
                        }
                        arraylist.add(kategori_item);
                    }
                    if (count_data>0)
                    {
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }


    }
}
