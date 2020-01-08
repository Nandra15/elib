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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.joatsy.apps.elibrarystta.R;
import com.joatsy.apps.elibrarystta.view.login.LoginActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.joatsy.apps.elibrarystta.view.MainActivity.SERVER_ADDRS;
import static com.joatsy.apps.elibrarystta.view.MainActivity.master_dir;
import static com.joatsy.apps.elibrarystta.view.MainActivity.mode_offline;
import static com.joatsy.apps.elibrarystta.view.MainActivity.session_user_id;
import static com.joatsy.apps.elibrarystta.view.MainActivity.user_agent;

public class ReadBookActivity  extends AppCompatActivity {
    PDFView pdfviewer;
    Button btn_kembalikan;
    Intent intent;
    String id_buku;
    String filename;
    String duration;
    String file_location;
    private ProgressDialog loading_dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readbook);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        pdfviewer = (PDFView) findViewById(R.id.pdfviewer2);
        btn_kembalikan = (Button) findViewById(R.id.btn_readbook_proc);
        //pdfviewer.fromAsset("contoh.pdf").load();
        intent = getIntent();

        if (intent.hasExtra("id_buku")) {
            id_buku = getIntent().getStringExtra("id_buku");
            //Toast.makeText(getBaseContext(), id_buku , Toast.LENGTH_LONG).show();
            if (!id_buku.equals(""))
            {
                if (check_in_mybook(id_buku)==true)
                {
                    if (get_book_expired(id_buku)==false)
                    {
                        loading_dialog = new ProgressDialog(ReadBookActivity.this);
                        loading_dialog.setMessage("Mencoba mendapatkan file buku. Tunggu sebentar...");
                        loading_dialog.setIndeterminate(true);
                        loading_dialog.show();
                        //new GetData().execute(SERVER_ADDRS + "file.php?id=" + id_buku, "file");
                        filename = get_filename_from_id(id_buku);
                        if(!filename.equals(""))
                        {
                            File file = new File(master_dir, filename);
                            Log.d("FILE PDF : ",file.getAbsolutePath());
                            if (file.exists())
                            {
                                showpdf(file);
                                loading_dialog.dismiss();
                                if (checkInet(0)==false)
                                {
                                    btn_kembalikan.setVisibility(View.GONE);
                                }
                                else
                                {
                                    btn_kembalikan.setVisibility(View.VISIBLE);
                                }

                                Toast.makeText(getBaseContext(), "Buku ini kadaluarsa tanggal : " + get_book_expired_date(id_buku), Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Toast.makeText(getBaseContext(), "Buku ini tidak ada dalam memori!", Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "Buku ini tidak ada dalam memori!", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        filename = get_filename_from_id(id_buku);
                        if(!filename.equals(""))
                        {
                            File file = new File(master_dir, filename);
                            Log.d("FILE PDF : ",file.getAbsolutePath());
                            if (file.exists())
                            {
                                file.delete();
                            }
                            delete_file_expired(id_buku);
                        }
                        if (mode_offline==true)
                        {
                            new AlertDialog.Builder(ReadBookActivity.this)
                                    .setCancelable(false)
                                    .setTitle("Informasi")
                                    .setMessage("Buku ini sudah kadaluarsa dalam masa peminjamanan anda. Silahkan terhubung ke internet dan pinjam lagi untuk melanjutkan baca.")
                                    .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            finish();
                                        }
                                    })
                                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            if (checkInet(1)==true)
                                            {
                                                Intent intent = new Intent(ReadBookActivity.this, LoginActivity.class);
                                                startActivity (intent);
                                                finish();
                                            }
                                            else
                                            {

                                            }
                                        }
                                    })
                                    .create().show();

                        }
                        else
                        {
                            new AlertDialog.Builder(ReadBookActivity.this)
                                    .setCancelable(false)
                                    .setTitle("Informasi")
                                    .setMessage("Buku ini sudah kadaluarsa dalam masa peminjamanan anda. Silahkan pinjam lagi untuk melanjutkan baca.")
                                    .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            finish();
                                        }
                                    })
                                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            Intent intent = new Intent(ReadBookActivity.this, ViewerActivity.class);
                                            intent.putExtra("id_buku", id_buku);
                                            startActivity (intent);
                                            finish();
                                        }
                                    })
                                    .create().show();

                        }


                    }

                }
                else
                {
                    Toast.makeText(getBaseContext(), "Buku ini belum ada dalam daftar pinjaman anda!", Toast.LENGTH_LONG).show();
                }

            }
            else
            {
                finish();
            }
        }
        else
        {
            finish();
        }

        btn_kembalikan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_file_expired(id_buku);
                loading_dialog = new ProgressDialog(ReadBookActivity.this);
                loading_dialog.setMessage("Mencoba mengembalikan buku. Tunggu sebentar...");
                loading_dialog.setIndeterminate(true);
                loading_dialog.show();
                new GetData().execute(SERVER_ADDRS + "kembalikan.php?user_id=" + session_user_id + "&id_buku=" + id_buku, "back");
//                Intent intent = new Intent(ViewerActivity.this, RegisterActivity.class);
//                startActivity (intent);
                //show_form_load();
            }
        });
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
                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(ReadBookActivity.this).create();
                alertDialog.setTitle("Kesalahan");
                alertDialog.setMessage("Tidak ditemukan koneksi internet");
                alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, "OK",
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

    boolean file_write(Boolean newFile, String data,String location_file, String name_file) {
        try {
            new File(location_file  ).mkdir();
            File file = new File(location_file+ name_file);
            if (!file.exists()) {
                file.createNewFile();
            }
            else
            {
                if (newFile==true)
                {
                    file.delete();
                    file.createNewFile();
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());

            return true;
        }  catch(FileNotFoundException ex) {
            Log.d("Write Error", ex.getMessage());
        }  catch(IOException ex) {
            Log.d("Write Error", ex.getMessage());
        }
        return  false;
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

    void delete_file_expired(String id_book)
    {
        //id:1;title:nananana;file:sn74hc595.pdf;duration:7;from:2019-06-01;end:2019-06-08
        boolean found_list=false;
        String new_list="";
        int new_item=0;
        String my_book_list = file_read(master_dir,"list.dat");
        if (my_book_list.contains("id:" + id_book + ";")) {
            String[] data_list = my_book_list.split("\r\n");
            for (int i = 0; i < data_list.length; i++) {
                if (!data_list[i].equals(""))
                {
                    Log.e("data_list [" + i + "] :",data_list[i]);
                    String[] data_list_item = data_list[i].split(";");
                    for (int a = 0; a < data_list_item.length; a++) {
                        if (!data_list_item[a].equals(""))
                        {
                            Log.e("data_list_item [" + a + "] :",data_list_item[a]);
                            if (a==0)
                            {
                                if (data_list_item[a].equals("id:" + id_book))
                                {
                                    found_list=true;
                                }
                            }
                            else if (a==2)
                            {
                                String filenamex = data_list_item[a].replace("file:","");
                                File file = new File(master_dir, filenamex);
                                if (file.exists())
                                {
                                    file.delete();
                                }
                            }
                        }
                    }
                    if (new_list.equals(""))
                    {
                        if (found_list==false)
                        {
                            new_item++;
                            new_list = data_list[i];
                        }
                    }
                    else
                    {
                        if (found_list==false)
                        {
                            new_item++;
                            new_list = new_list + "\r\n" + data_list[i];
                        }
                    }

                }
            }
        }
        if (new_item>0)
        {
            file_write(true,new_list,master_dir,"list.dat");
        }else{
            File file = new File(master_dir, "list.dat");
            if (file.exists())
            {
                file.delete();
            }
        }
    }

    String get_filename_from_id(String id_book)
    {
        //id:1;title:nananana;file:sn74hc595.pdf;duration:7;from:2019-06-01;end:2019-06-08
        boolean found_list=false;
        String rslt="";
        String my_book_list = file_read(master_dir,"list.dat");
        if (my_book_list.contains("id:" + id_book + ";")) {
            String[] data_list = my_book_list.split("&&&&");
            for (int i = 0; i < data_list.length; i++) {
                if (!data_list[i].equals(""))
                {
                    Log.d("data_list [" + i + "] :",data_list[i]);
                    String[] data_list_item = data_list[i].split(";");
                    for (int a = 0; a < data_list_item.length; a++) {
                        if (!data_list_item[a].equals(""))
                        {
                            Log.d("data_list_item [" + a + "] :",data_list_item[a]);
                            if (a==0)
                            {
                                if (data_list_item[a].equals("id:" + id_book))
                                {
//                                    rslt = id_book;
                                    found_list=true;
                                }
                            }
                            else if (a==2 && found_list==true)
                            {
                                return  data_list_item[a].replace("file:","");
                            }
                        }
                    }
                }
            }
        }
        return  rslt;
    }

    String get_book_expired_date(String id_book)
    {
        //id:1;title:nananana;file:sn74hc595.pdf;duration:7;from:2019-06-01;end:2019-06-08
        String rslt="";
        boolean found_list=false;
        String my_book_list = file_read(master_dir,"list.dat");
        if (my_book_list.contains("id:" + id_book + ";")) {
            String[] data_list = my_book_list.split("\r\n");
            for (int i = 0; i < data_list.length; i++) {
                if (!data_list[i].equals(""))
                {
                    Log.d("data_list [" + i + "] :",data_list[i]);
                    String[] data_list_item = data_list[i].split(";");
                    for (int a = 0; a < data_list_item.length; a++) {
                        if (!data_list_item[a].equals(""))
                        {
                            Log.d("data_list_item [" + a + "] :",data_list_item[a]);
                            if (a==0)
                            {
                                if (data_list_item[a].equals("id:" + id_book))
                                {
                                    found_list=true;
                                }
                            }
                            else if (a==5 && found_list==true)
                            {
                                String enddate = data_list_item[a].replace("end:","");
                                return  enddate;
                            }
                        }
                    }
                }
            }
        }
        return  rslt;
    }

    boolean get_book_expired(String id_book)
    {
        //id:1;title:nananana;file:sn74hc595.pdf;duration:7;from:2019-06-01;end:2019-06-08
        boolean rslt=false;
        boolean found_list=false;
        String my_book_list = file_read(master_dir,"list.dat");
        if (my_book_list.contains("id:" + id_book + ";")) {
            String[] data_list = my_book_list.split("\r\n");
            for (int i = 0; i < data_list.length; i++) {
                if (!data_list[i].equals(""))
                {
                    Log.d("data_list [" + i + "] :",data_list[i]);
                    String[] data_list_item = data_list[i].split(";");
                    for (int a = 0; a < data_list_item.length; a++) {
                        if (!data_list_item[a].equals(""))
                        {
                            Log.d("data_list_item [" + a + "] :",data_list_item[a]);
                            if (a==0)
                            {
                                if (data_list_item[a].equals("id:" + id_book))
                                {
                                    found_list=true;
                                }
                            }
                            else if (a==5 && found_list==true)
                            {
                                //return  data_list_item[a].replace("file:","");
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                String enddate = data_list_item[a].replace("end:","");
                                try
                                {
                                    Date sDate = sdf.parse(enddate);
                                    if (new Date().after(sDate))
                                    {
                                        return true;
                                    }
                                    else
                                    {
                                        return false;
                                    }
                                }
                                catch (ParseException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
        return rslt;
    }

    boolean check_in_mybook(String id_book)
    {
        String my_book_list = file_read(master_dir,"list.dat");
        Log.e("dat", my_book_list + " vs " + "id:" + id_book + ";");
        if (my_book_list.contains("id:" + id_book + ";")) {
            return true;
        }
        else
            return false;
    }
    void showpdf(File file)
    {
        pdfviewer.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(true)
                .password(null)
                .scrollHandle(null)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        pdfviewer.setMinZoom(1f);
                        pdfviewer.setMidZoom(5f);
                        pdfviewer.setMaxZoom(10f);
                        pdfviewer.zoomTo(1f);
                        //pdfviewer.scrollTo(100,0);
                        //pdfviewer.moveTo(0f,0f);
                    }
                })
                .load();
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
            if (result.command.equals("back")) {
                loading_dialog.dismiss();
                Log.i("URL login", result.url);
                Log.i("VALUE login", result.value);
                if (result.value.equals("OK"))
                {
                    Toast.makeText(getBaseContext(), "Berhasil mengembalikan buku!" , Toast.LENGTH_LONG).show();
                    finish();
                }
                else
                {
                    Toast.makeText(getBaseContext(), "Tidak berhasil mengembalikan buku!" , Toast.LENGTH_LONG).show();
                }
            }
        }


    }
}
