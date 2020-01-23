package com.joatsy.apps.elibrarystta.view;

import android.annotation.SuppressLint;
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
import com.joatsy.apps.elibrarystta.base.BaseActivity;
import com.joatsy.apps.elibrarystta.utils.Constants;
import com.joatsy.apps.elibrarystta.utils.SharedPrefs;
import com.joatsy.apps.elibrarystta.view.login.LoginActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

public class ReadBookActivity extends BaseActivity {
    PDFView pdfviewer;
    Button btn_kembalikan;
    Intent intent;
    String id_buku;
    String id_peminjaman;
    String filename;
    String duration;
    String file_location;
    private SharedPrefs pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readbook);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        pdfviewer = (PDFView) findViewById(R.id.pdfviewer2);
        btn_kembalikan = (Button) findViewById(R.id.btn_readbook_proc);
        //pdfviewer.fromAsset("contoh.pdf").load();

        pref = new SharedPrefs(this);
        intent = getIntent();

        if (intent.hasExtra("id_buku")) {
            id_buku = getIntent().getStringExtra("id_buku");
            id_peminjaman = getIntent().getStringExtra("id_peminjaman");
            //Toast.makeText(getBaseContext(), id_buku , Toast.LENGTH_LONG).show();
            if (!id_buku.equals("")) {
                if (check_in_mybook(id_buku)) {
                    if (!get_book_expired(id_buku)) {
                        showDialog("Mencoba mendapatkan file buku. Tunggu sebentar...");
                        //new GetData().execute(SERVER_ADDRS + "file.php?id=" + id_buku, "file");
                        filename = get_filename_from_id(id_buku);
                        if (!filename.equals("")) {
                            File file = new File(master_dir, filename);
                            Log.d("FILE PDF : ", file.getAbsolutePath());
                            if (file.exists()) {
                                showpdf(file);
                                hideLoading();
                                if (!checkInet(0)) {
                                    btn_kembalikan.setVisibility(View.GONE);
                                } else {
                                    btn_kembalikan.setVisibility(View.VISIBLE);
                                }

                                Toast.makeText(getBaseContext(), "Buku ini kadaluarsa tanggal : " + get_book_expired_date(id_buku), Toast.LENGTH_LONG).show();
                            } else {
                                hideLoading();
                                delete_file_expired(id_buku);
                                Toast.makeText(getBaseContext(), "Buku ini tidak ada dalam memori!", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            hideLoading();
                            delete_file_expired(id_buku);
                            Toast.makeText(getBaseContext(), "Buku ini tidak ada dalam memori!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        filename = get_filename_from_id(id_buku);
                        if (!filename.equals("")) {
                            File file = new File(master_dir, filename);
                            Log.d("FILE PDF : ", file.getAbsolutePath());
                            if (file.exists()) {
                                file.delete();
                            }
                            delete_file_expired(id_buku);
                        }
                        if (mode_offline) {
                            new AlertDialog.Builder(ReadBookActivity.this)
                                    .setCancelable(false)
                                    .setTitle("Informasi")
                                    .setMessage("Buku ini sudah kadaluarsa dalam masa peminjamanan anda. Silahkan terhubung ke internet dan pinjam lagi untuk melanjutkan baca.")
                                    .setNegativeButton("Tidak", (arg0, arg1) -> finish())
                                    .setPositiveButton("Ya", (arg0, arg1) -> {
                                        if (checkInet(1)) {
                                            Intent intent = new Intent(ReadBookActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {

                                        }
                                    })
                                    .create().show();

                        } else {
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
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .create().show();

                        }


                    }

                } else {
                    hideLoading();
                    delete_file_expired(id_buku);
                    Toast.makeText(getBaseContext(), "Buku ini belum ada dalam daftar pinjaman anda!", Toast.LENGTH_LONG).show();
                finish();
                }

            } else {
                finish();
            }
        } else {
            finish();
        }

        btn_kembalikan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog("Mencoba mengembalikan buku. Tunggu sebentar...");
                new PostData().execute(SERVER_ADDRS + "peminjaman/back", "back","id_peminjaman="+id_peminjaman);
//                Intent intent = new Intent(ViewerActivity.this, RegisterActivity.class);
//                startActivity (intent);
                //show_form_load();
            }
        });
    }

    boolean file_write(Boolean newFile, String data, String location_file, String name_file) {
        try {
            new File(location_file).mkdir();
            File file = new File(location_file + name_file);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                if (newFile == true) {
                    file.delete();
                    file.createNewFile();
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());

            return true;
        } catch (FileNotFoundException ex) {
            Log.d("Write Error", ex.getMessage());
        } catch (IOException ex) {
            Log.d("Write Error", ex.getMessage());
        }
        return false;
    }

    private String file_read(String location_file, String name_file) {

        String line = "";

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(location_file + name_file));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            Log.d("Read error", ex.getMessage());
        } catch (IOException ex) {
            Log.d("Read error", ex.getMessage());
        }
        return line;
    }

    void delete_file_expired(String id_book) {
        //id:1;title:nananana;file:sn74hc595.pdf;duration:7;from:2019-06-01;end:2019-06-08
        boolean found_list = false;
        String new_list = "";
        int new_item = 0;

        String my_book_list = file_read(master_dir, "list.dat");
//        String my_book_list = pref.getListData();

        if (my_book_list.contains("id:" + id_book + ";")) {
            String[] data_list = my_book_list.split("&&&&");
            for (int i = 0; i < data_list.length; i++) {
                found_list = false;
                if (!data_list[i].equals("")) {
                    Log.e("data_list [" + i + "] :", data_list[i]);
                    String[] data_list_item = data_list[i].split(";");
                    for (int a = 0; a < data_list_item.length; a++) {
                        if (!data_list_item[a].equals("")) {
                            Log.e("data_list_item [" + a + "] :", data_list_item[a]);
                            if (a == 0) {
                                if (data_list_item[a].equals("id:" + id_book)) {
                                    found_list = true;
                                }
                            } else if (a == 2 && found_list) {
                                String filenamex = data_list_item[a].replace("file:", "");
                                File file = new File(master_dir, filenamex);
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                        }
                    }
                    if (new_list.equals("")) {
                        if (found_list == false) {
                            new_item++;
                            new_list = data_list[i];
                        }
                    } else {
                        if (found_list == false) {
                            new_item++;
                            new_list = new_list + "&&&&" + data_list[i];
                        }
                    }

                }
            }
        }
        if (new_item > 0) {
            file_write(true, new_list, master_dir, "list.dat");
//            pref.saveListData(new_list);
        } else {
//            pref.saveListData("");
            File file = new File(master_dir, "list.dat");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    String get_filename_from_id(String id_book) {
        //id:1;title:nananana;file:sn74hc595.pdf;duration:7;from:2019-06-01;end:2019-06-08
        boolean found_list = false;
        String rslt = "";

        String my_book_list = file_read(master_dir, "list.dat");
//        String my_book_list = pref.getListData();
        if (my_book_list.contains("id:" + id_book + ";")) {
            String[] data_list = my_book_list.split("&&&&");
            for (int i = 0; i < data_list.length; i++) {
                if (!data_list[i].equals("")) {
                    Log.d("data_list [" + i + "] :", data_list[i]);
                    String[] data_list_item = data_list[i].split(";");
                    for (int a = 0; a < data_list_item.length; a++) {
                        if (!data_list_item[a].equals("")) {
                            Log.d("data_list_item [" + a + "] :", data_list_item[a]);
                            if (a == 0) {
                                if (data_list_item[a].equals("id:" + id_book)) {
//                                    rslt = id_book;
                                    found_list = true;
                                }
                            } else if (a == 2 && found_list == true) {
                                return data_list_item[a].replace("file:", "");
                            }
                        }
                    }
                }
            }
        }
        return rslt;
    }

    private String get_book_expired_date(String id_book) {
        //id:1;title:nananana;file:sn74hc595.pdf;duration:7;from:2019-06-01;end:2019-06-08
        String rslt = "";
        boolean found_list = false;

        String my_book_list = file_read(master_dir, "list.dat");
//        String my_book_list = pref.getListData();

        if (my_book_list.contains("id:" + id_book + ";")) {
            String[] data_list = my_book_list.split("&&&&");
            for (int i = 0; i < data_list.length; i++) {
                if (!data_list[i].equals("")) {
                    Log.d("data_list [" + i + "] :", data_list[i]);
                    String[] data_list_item = data_list[i].split(";");
                    for (int a = 0; a < data_list_item.length; a++) {
                        if (!data_list_item[a].equals("")) {
                            Log.d("data_list_item [" + a + "] :", data_list_item[a]);
                            if (a == 0) {
                                if (data_list_item[a].equals("id:" + id_book)) {
                                    found_list = true;
                                }
                            } else if (a == 6 && found_list == true) {
                                String enddate = data_list_item[a].replace("end:", "");
                                return enddate;
                            }
                        }
                    }
                }
            }
        }
        return rslt;
    }

    boolean get_book_expired(String id_book) {
        //id:1;title:nananana;file:sn74hc595.pdf;duration:7;from:2019-06-01;end:2019-06-08
        boolean rslt = false;
        boolean found_list = false;
        String my_book_list = file_read(master_dir, "list.dat");
//        String my_book_list = pref.getListData();
        if (my_book_list.contains("id:" + id_book + ";")) {
            String[] data_list = my_book_list.split("\r\n");
            for (int i = 0; i < data_list.length; i++) {
                if (!data_list[i].equals("")) {
                    Log.d("data_list [" + i + "] :", data_list[i]);
                    String[] data_list_item = data_list[i].split(";");
                    for (int a = 0; a < data_list_item.length; a++) {
                        if (!data_list_item[a].equals("")) {
                            Log.d("data_list_item [" + a + "] :", data_list_item[a]);
                            if (a == 0) {
                                if (data_list_item[a].equals("id:" + id_book)) {
                                    found_list = true;
                                }
                            } else if (a == 5 && found_list == true) {
                                //return  data_list_item[a].replace("file:","");
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                String enddate = data_list_item[a].replace("end:", "");
                                try {
                                    Date sDate = sdf.parse(enddate);
                                    if (new Date().after(sDate)) {
                                        return true;
                                    } else {
                                        return false;
                                    }
                                } catch (ParseException e) {
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

    boolean check_in_mybook(String id_book) {
        String my_book_list = file_read(master_dir, "list.dat");
//        String my_book_list = pref.getListData();
        Log.e("dat", my_book_list + " vs " + "id:" + id_book + ";");
        if (my_book_list.contains("id:" + id_book + ";")) {
            return true;
        } else
            return false;
    }

    private void showpdf(File file) {
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

    public class GetDataResult {
        public String value;
        public String command;
        public String url;
    }

    @SuppressLint("StaticFieldLeak")
    private class PostData extends AsyncTask<String, String, PostDataResult> {

        @Override
        protected PostDataResult doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            PostDataResult rslt = new PostDataResult();
            rslt.url = params[0];
            rslt.value = "";
            rslt.parameter = params[2];
            rslt.command = params[1];
             try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setReadTimeout(20000);
                urlConnection.setConnectTimeout(20000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Constants._UTF8));
                writer.write(rslt.parameter);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                Log.d("responseCode PostData", "responseCode : " + responseCode);
                if (responseCode == 201 || responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    in.close();
                    Log.i("sb PostData", sb.toString());
                    rslt.value = sb.toString();
                    rslt.url = params[0];
                    rslt.command = params[1];
                    return rslt;
                }
            } catch (MalformedURLException e) {
                Log.e("view malf", e.getMessage());
            } catch (IOException e) {
                Log.e("view io", e.getMessage());
            } finally {
                urlConnection.disconnect();
            }
            return rslt;

        }

        protected void onPostExecute(PostDataResult result) {
            Log.e("URL baca", result.url);
            Log.e("VALUE baca", result.value);
            if (result.command.equals("back")) {
                loading_dialog.dismiss();
                if (result.value.contains("true")) {
                    delete_file_expired(id_buku);
                    showToast("Buku Berhasil Dikembalikan");
                    finish();

                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(ReadBookActivity.this).create();
                    alertDialog.setTitle("Kesalahan");
                    alertDialog.setMessage("Proses pengembalian gagal");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> {
                                //dialog.dismiss();
                            });
                    alertDialog.show();
                }
            }

        }


    }

    public class PostDataResult {
        public String value;
        public String command;
        public String url;
        public String parameter;
    }
}
