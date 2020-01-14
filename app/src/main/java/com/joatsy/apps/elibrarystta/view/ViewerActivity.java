package com.joatsy.apps.elibrarystta.view;

import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.joatsy.apps.elibrarystta.R;
import com.joatsy.apps.elibrarystta.base.BaseActivity;
import com.joatsy.apps.elibrarystta.utils.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.joatsy.apps.elibrarystta.view.MainActivity.SERVER_ADDRS;
import static com.joatsy.apps.elibrarystta.view.MainActivity.master_dir;
import static com.joatsy.apps.elibrarystta.view.MainActivity.session_user_id;
import static com.joatsy.apps.elibrarystta.view.MainActivity.session_user_nim;
import static com.joatsy.apps.elibrarystta.view.MainActivity.user_agent;

public class ViewerActivity extends BaseActivity {
    PDFView pdfviewer;
    Button btn_pinjam;
    Intent intent;
    String id_buku;
    String judul_buku;
    String filename;
    String duration;
    String file_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        pdfviewer = findViewById(R.id.pdfviewer);
        btn_pinjam = findViewById(R.id.btn_pinjam_proc);

        intent = getIntent();
        if (intent.hasExtra("id_buku") && intent.hasExtra("judul_buku")) {
            id_buku = getIntent().getStringExtra("id_buku");
            judul_buku = getIntent().getStringExtra("judul_buku");
            file_location = getIntent().getStringExtra("file_location");
            //Toast.makeText(getBaseContext(), id_buku , Toast.LENGTH_LONG).show();
            if (!id_buku.equals("") && !judul_buku.equals("") && !file_location.equals("")) {
                if (check_in_mybook(id_buku) == false) {
                    loading_dialog = new ProgressDialog(ViewerActivity.this);
                    loading_dialog.setMessage("Mencoba mendapatkan file buku. Tunggu sebentar...");
                    loading_dialog.setIndeterminate(true);
                    loading_dialog.show();
//                    String parameters = "id_member=" + session_user_id + "&id_buku=" + id_buku;
                    String parameters = "{\n" +
                            "\t\"id_member\": " + session_user_id + ",\n" +
                            "\t\"id_buku\": " + id_buku + "\n" +

                            "}";
                    Log.e("test", session_user_id);

                    //"id_member:"+session_user_id,"id_buku":+ id_buku;
                    //String parameters = "id_member:" + session_user_id + ",id_buku:" + id_buku;
                    //String parameters = "{\"id_member\":" + session_user_id + ",\"id_buku\":" + id_buku + "}";
                    //Toast.makeText(getBaseContext(), "BACA FILE : " + "membaca,membaca," + parameters, Toast.LENGTH_LONG).show();
                    new PostData().execute(SERVER_ADDRS + "membaca", "baca", parameters);
                } else {
                    Toast.makeText(getBaseContext(), "Buku ini sudah ada dalam daftar pinjaman anda!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ViewerActivity.this, ReadBookActivity.class);
                    intent.putExtra("id_buku", id_buku);
                    startActivity(intent);
                    finish();
                }

            } else {
                finish();
            }
        } else {
            finish();
        }

        btn_pinjam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(ViewerActivity.this, RegisterActivity.class);
//                startActivity (intent);
                if (checkInet(1) == true) {
                    show_form_load();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(ViewerActivity.this)
                .setCancelable(false)
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin tidak jadi meminjam?")
                .setNegativeButton("Tidak", (arg0, arg1) -> {

                })
                .setPositiveButton("Ya", (arg0, arg1) -> {
                    new PostData().execute(SERVER_ADDRS + "membaca/back", "back", "nim=" + session_user_nim + "&id_buku=" + id_buku);
                    File file = new File(getBaseContext().getCacheDir(), filename);
                    //Toast.makeText(getBaseContext(), "DELETE FILE : " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    if (file.exists()) {
                        //pdfviewer.removeAllViewsInLayout();
                        file.delete();
                        finish();
                    }
                })
                .create().show();
    }

    public class GetDataResult {
        public String value;
        public String command;
        public String url;
    }

    public class GetDownloadResult {
        public String file;
        public String url;
    }

    public class PostDataResult {
        public String value;
        public String command;
        public String url;
        public String parameter;
    }

//    private static void downloadFile(String url, File outputFile) {
//        try {
//            URL u = new URL(url);
//            URLConnection conn = u.openConnection();
//            int contentLength = conn.getContentLength();
//
//            DataInputStream stream = new DataInputStream(u.openStream());
//
//            byte[] buffer = new byte[contentLength];
//            stream.readFully(buffer);
//            stream.close();
//
//            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
//            fos.write(buffer);
//            fos.flush();
//            fos.close();
//        } catch(FileNotFoundException e) {
//            return; // swallow a 404
//        } catch (IOException e) {
//            return; // swallow a 404
//        }
//    }

    public class DownloadFilePdf extends AsyncTask<String, String, GetDownloadResult> {
        @Override
        protected GetDownloadResult doInBackground(String... params) {
            GetDownloadResult rslt = new GetDownloadResult();
            rslt.url = params[0];
            rslt.file = params[1];
            try {
                URL u = new URL(rslt.url);
                URLConnection conn = u.openConnection();
                int contentLength = conn.getContentLength();

                DataInputStream stream = new DataInputStream(u.openStream());

                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();
                File file = new File(getBaseContext().getCacheDir(), rslt.file);
                DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
                fos.write(buffer);
                fos.flush();
                fos.close();
                return rslt;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return rslt;
        }

        protected void onPostExecute(GetDownloadResult result) {
hideLoading();
filename = result.url.substring(result.url.lastIndexOf('/') + 1);
            //Toast.makeText(getBaseContext(), "SIMPAN FILE : " +filename , Toast.LENGTH_LONG).show();
            //file_write_to_chace(result.value,filename);
            File file = new File(getBaseContext().getCacheDir(), filename);
            if (file.exists()) {
                //downloadFile(result.url,file);
                //Toast.makeText(getBaseContext(), "BUKA FILE : " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                //pdfviewer.fromFile(file);
                showpdf(file);
            }
            //

        }

    }

    public class GetData extends AsyncTask<String, String, GetDataResult> {

        @Override
        protected GetDataResult doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String result = "";
            GetDataResult rslt = new GetDataResult();
            rslt.url = params[0];
            rslt.value = "";
            rslt.command = params[1];
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Accept-Encoding", "");
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("User-Agent", user_agent);
                urlConnection.setConnectTimeout(30000);
                urlConnection.setReadTimeout(30000);

                int code = urlConnection.getResponseCode();

                if (code == 200) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    if (in != null) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        String line = "";

                        while ((line = bufferedReader.readLine()) != null)
                            result += line;
                    }
                    in.close();
                }
                rslt.value = result;
                rslt.url = params[0];
                rslt.command = params[1];
                return rslt;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return rslt;

        }

        protected void onPostExecute(GetDataResult result) {
            if (result.command.equals("file")) {
                hideLoading();
                Log.i("URL file", result.url);
                Log.i("VALUE file", result.value);
                if (!result.value.equals("FAIL")) {
                    //Toast.makeText(getBaseContext(), "URL FILE : " + result.value , Toast.LENGTH_LONG).show();

                    //new GetData().execute( result.value, "download");
                    filename = result.value.substring(result.value.lastIndexOf('/') + 1);
                    File file = new File(getBaseContext().getCacheDir(), filename);
                    if (!file.exists()) {
                        loading_dialog = new ProgressDialog(ViewerActivity.this);
                        loading_dialog.setMessage("Mencoba menampilkan file buku. Tunggu sebentar...");
                        loading_dialog.setIndeterminate(true);
                        loading_dialog.show();
                        new DownloadFilePdf().execute(result.value, filename);
                    } else {
//                        Log.i("BUKA FILE : ", file.getAbsolutePath());
//                        Toast.makeText(getBaseContext(), "BUKA FILE : " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        showpdf(file);
                    }

                }
            }
            if (result.command.equals("loan")) {
                hideLoading();
                Log.i("URL file", result.url);
                Log.i("VALUE file", result.value);
                if (!result.value.equals("OK")) {
                    Toast.makeText(getBaseContext(), result.value, Toast.LENGTH_LONG).show();
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String thisdate = sdf.format(new Date());
                    Calendar c = Calendar.getInstance();
                    try {
                        c.setTime(sdf.parse(thisdate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    c.add(Calendar.DATE, Integer.valueOf(duration));
                    String enddate = sdf.format(c.getTime());
                    String my_book_list = file_read(master_dir, "list.dat");
                    if (my_book_list.equals("")) {
                        my_book_list = "id:" + id_buku + ";" + "title:" + judul_buku + ";" + "file:" + filename + ";" + "duration:" + duration + ";from:" + thisdate + ";end:" + enddate;
                    } else {
                        if (!my_book_list.contains("id:" + id_buku + ";")) {
                            my_book_list = my_book_list + "id:" + id_buku + ";" + "title:" + judul_buku + ";" + "file:" + filename + ";" + "duration:" + duration + ";from:" + thisdate + ";end:" + enddate;
                        }
                    }
                    file_write(true, my_book_list, master_dir, "list.dat");
                    File file_source = new File(getBaseContext().getCacheDir(), filename);
                    File file_destination = new File(master_dir, filename);
                    copyfile(file_source, file_destination);

                    Toast.makeText(getBaseContext(), "Buku ini berhasil ditambahkan dalam daftar pinjaman anda selama " + duration + " hari!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ViewerActivity.this, ReadBookActivity.class);
                    intent.putExtra("id_buku", id_buku);
                    startActivity(intent);
                    finish();
                }

            }
        }


    }

    public class PostData extends AsyncTask<String, String, PostDataResult> {

        @Override
        protected PostDataResult doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            PostDataResult rslt = new PostDataResult();
            rslt.url = params[0];
            rslt.value = "";
            rslt.parameter = params[2];
            rslt.command = params[1];
            Log.i("url PostData", rslt.url);
            Log.i("command PostData", rslt.command);
            Log.i("parameter PostData", rslt.parameter);
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
            if (result.command.equals("baca")) {
                hideLoading();
                Log.i("URL baca", result.url);
                Log.i("VALUE baca", result.value);
                if (result.value.equals("{\"status\":true,\"message\":\"Berhasil\"}")) {
                    filename = file_location.substring(file_location.lastIndexOf('/') + 1);
                    File file = new File(getBaseContext().getCacheDir(), filename);
                    if (!file.exists()) {
                        hideLoading();
                        showDialog("Mencoba menampilkan file buku. Tunggu sebentar...");
                        new DownloadFilePdf().execute(file_location, filename);
                    } else {
                        showpdf(file);
                    }

                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(ViewerActivity.this).create();
                    alertDialog.setTitle("Kesalahan");
                    alertDialog.setMessage("Proses membaca gagal");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            } else if (result.command.equals("back")) {
                loading_dialog.dismiss();
                Log.i("URL back", result.url);
                Log.i("VALUE back", result.value);
                if (result.value.equals("{\"status\":true,\"data\":\"Berhasil\"}")) {
                    finish();

                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(ViewerActivity.this).create();
                    alertDialog.setTitle("Kesalahan");
                    alertDialog.setMessage("Proses pengembalian gagal");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            } else if (result.command.equals("loan")) {
                loading_dialog.dismiss();
                Log.i("URL loan", result.url);
                Log.i("VALUE loan", result.value);
                if (result.value.contains("berhasil")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String thisdate = sdf.format(new Date());
                    Calendar c = Calendar.getInstance();
                    try {
                        c.setTime(sdf.parse(thisdate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    c.add(Calendar.DATE, Integer.valueOf(duration));
                    String enddate = sdf.format(c.getTime());
                    String my_book_list = file_read(master_dir, "list.dat");
                    if (my_book_list.equals("")) {
                        my_book_list = "id:" + id_buku + ";" + "title:" + judul_buku + ";" + "file:" + filename + ";" + "file_location:" + file_location + ";duration:" + duration + ";from:" + thisdate + ";end:" + enddate;
                    } else {
                        if (!my_book_list.contains("id:" + id_buku + ";")) {
                            my_book_list = my_book_list + "&&&&id:" + id_buku + ";" + "title:" + judul_buku + ";" + "file:" + filename + ";" + "file_location:" + file_location + ";duration:" + duration + ";from:" + thisdate + ";end:" + enddate;
                        }
                    }
                    file_write(true, my_book_list, master_dir, "list.dat");
                    File file_source = new File(getBaseContext().getCacheDir(), filename);
                    File file_destination = new File(master_dir, filename);
                    copyfile(file_source, file_destination);

                    Toast.makeText(getBaseContext(), "Buku ini berhasil ditambahkan dalam daftar pinjaman anda selama " + duration + " hari!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ViewerActivity.this, ReadBookActivity.class);
                    intent.putExtra("id_buku", id_buku);
                    startActivity(intent);
                    finish();

                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(ViewerActivity.this).create();
                    alertDialog.setTitle("Kesalahan");
                    alertDialog.setMessage("Proses peminjaman gagal");
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


    void showpdf(File file) {
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

    boolean check_in_mybook(String id_book) {
        String my_book_list = file_read(master_dir, "list.dat");
        return my_book_list.contains("id:" + id_book + ";");
    }

    void show_form_load() {
        ArrayList<String> arraylist = new ArrayList<String>();
        final Dialog form = new Dialog(ViewerActivity.this);
        form.setContentView(R.layout.activity_loan);
        form.setCancelable(false);
        //form.create();
        Button btn_loan = form.findViewById(R.id.btn_loan_proc);
        Spinner sp_loan = form.findViewById(R.id.sp_loan_duration);
        for (int a = 1; a <= 3; a++) {
            arraylist.add(String.valueOf(a));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewerActivity.this, android.R.layout.simple_spinner_item, arraylist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sp_loan.setAdapter(adapter);
        sp_loan.setOnItemSelectedListener(new myOnItemSelectedListener());
        duration = "1";
        form.show();
        btn_loan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!duration.equals("")) {
                    form.cancel();
                    if (checkInet(1) == true) {
                        showDialog("Mencoba meminjam file buku. Tunggu sebentar...");
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Silahkan pilih durasi pinjaman", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    class myOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
            String text = parent.getItemAtPosition(pos).toString();
            //Toast.makeText(getBaseContext(), "List " + text + " Ditekan", Toast.LENGTH_LONG).show();
            duration = text;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }

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

    void copyfile(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            try {
                OutputStream out = new FileOutputStream(dst);
                try {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    out.close();
                    Log.e("out close", "true");
                }
            } finally {
                in.close();
                Log.e("in close", "true");
            }
        } catch (FileNotFoundException ex) {
            Log.e("Write Error", ex.getMessage());
        } catch (IOException ex) {
            Log.e("Write Error", ex.getMessage());
        }
    }

}
