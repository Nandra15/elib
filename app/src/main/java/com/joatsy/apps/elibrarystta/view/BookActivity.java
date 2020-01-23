package com.joatsy.apps.elibrarystta.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.joatsy.apps.elibrarystta.Adapter.AdapterRecyclerBook;
import com.joatsy.apps.elibrarystta.Data.DataBook;
import com.joatsy.apps.elibrarystta.Event.RecyclerItemClickListener;
import com.joatsy.apps.elibrarystta.R;
import com.joatsy.apps.elibrarystta.utils.SharedPrefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.joatsy.apps.elibrarystta.view.MainActivity.SERVER_ADDRS;
import static com.joatsy.apps.elibrarystta.view.MainActivity.master_dir;
import static com.joatsy.apps.elibrarystta.view.MainActivity.user_agent;

public class BookActivity extends AppCompatActivity {
    Intent intent;
    private RecyclerView recycler_book;
    public static AdapterRecyclerBook recycler_adapter;
    public static ArrayList<DataBook> data_book;
    ArrayList<String> arraylist = new ArrayList<String>();
    private ProgressDialog loading_dialog;
    private SharedPrefs pref;
    private String kategori, nama;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        kategori= "";
        nama = "";
        intent = getIntent();
        if (intent.hasExtra("kategori")) {
            kategori = getIntent().getStringExtra("kategori");
        }
        if (intent.hasExtra("nama")) {
            nama = getIntent().getStringExtra("nama");
        }
        recycler_book = (RecyclerView) findViewById(R.id.rc_book);
        data_book = populateList();
        pref = new SharedPrefs(this);
        recycler_adapter = new AdapterRecyclerBook(this,data_book);
        recycler_book.setAdapter(recycler_adapter);
        recycler_book.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        recycler_book.addOnItemTouchListener(
                new RecyclerItemClickListener(getBaseContext(), recycler_book ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String nama_buku = data_book.get(position).getValue();
                        String id_buku = data_book.get(position).getId();
                        String file_location = data_book.get(position).getLocation();
                        //Toast.makeText(getBaseContext(), "List (" + id_buku + ")" + nama_buku + " Ditekan" , Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(BookActivity.this, ViewerActivity.class);
                        intent.putExtra("id_buku", id_buku);
                        intent.putExtra("judul_buku", nama_buku);
                        intent.putExtra("file_location",file_location);
//                        intent.putExtra("nama", tx_name.getText().toString());
                        startActivity (intent);

                        if (check_in_mybook(id_buku))
                        {
                            finish();
                        }
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        loading_dialog = new ProgressDialog(BookActivity.this);
        loading_dialog.setMessage("Mencoba mengambil data kategori. Tunggu sebentar...");
        loading_dialog.setIndeterminate(true);
        loading_dialog.show();

        //new GetData().execute(SERVER_ADDRS + "buku.php?kategori=" + kategori + "&s=" + nama, "book");
        new GetData().execute(SERVER_ADDRS + "catalog", "book");
    }

    private ArrayList<DataBook> populateList(){
        ArrayList<DataBook>  list = new ArrayList<>();

        return list;
    }

    boolean check_in_mybook(String id_book)
    {
        String my_book_list = file_read(master_dir,"list.dat");
//        String my_book_list = pref.getListData();
        if (my_book_list.contains("id:" + id_book + ";")) {
            return true;
        }
        else
            return false;
    }

    private String file_read(String location_file, String name_file) {

        String line = "";

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(location_file + name_file));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.d("Read error", ex.getMessage());
        }
        catch(IOException ex) {
            Log.d("Read error", ex.getMessage());
        }
        return line;
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
            if (result.command.equals("book")) {
                int count_data=0;
                loading_dialog.dismiss();
                Log.i("URL login", result.url);
                Log.i("VALUE login", result.value);
                try {
                    JSONObject json = new JSONObject(result.value);
                    JSONArray userArray = json.getJSONArray("data");
                    for (int i = 0; i < userArray.length(); i++) {
                        count_data++;
                        JSONObject userDetail = userArray.getJSONObject(i);
                        String id_buku = userDetail.getString("id");
                        String judul_buku = userDetail.getString("judul");
                        String file_location = userDetail.getString("pdf_path");
                        if (judul_buku.toLowerCase().contains(nama.toLowerCase())){
                            if (count_data==1)
                            {
                                data_book.clear();
                            }
                            DataBook editModel = new DataBook(String.valueOf(R.drawable.ic_library_books_purple_24dp), id_buku,judul_buku,0,file_location,"");
                            data_book.add(editModel);
                        }

                    }
                    if (count_data>0)
                    {
                        recycler_adapter.notifyDataSetChanged();
                    }  else
                    {
                        Toast.makeText(getBaseContext(), "Tidak ditemukan buku yang sesuai" , Toast.LENGTH_LONG).show();
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (count_data<=0)
                {
                    Toast.makeText(getBaseContext(), "Tidak ditemukan buku yang sesuai" , Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }


    }
}
