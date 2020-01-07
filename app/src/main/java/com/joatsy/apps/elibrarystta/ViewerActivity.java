package com.joatsy.apps.elibrarystta;

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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.joatsy.apps.elibrarystta.Data.ItemsItem;
import com.joatsy.apps.elibrarystta.Data.ResponseRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.joatsy.apps.elibrarystta.MainActivity.SERVER_ADDRS;
import static com.joatsy.apps.elibrarystta.MainActivity.master_dir;
import static com.joatsy.apps.elibrarystta.MainActivity.session_user_id;
import static com.joatsy.apps.elibrarystta.MainActivity.session_user_nim;
import static com.joatsy.apps.elibrarystta.MainActivity.user_agent;

public class ViewerActivity extends AppCompatActivity {
    PDFView pdfviewer;
    Button btn_pinjam;
    Intent intent;
    String id_buku;
    String judul_buku;
    String filename;
    String duration;
    String file_location;
    private ProgressDialog loading_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        pdfviewer = (PDFView) findViewById(R.id.pdfviewer);
        btn_pinjam = (Button) findViewById(R.id.btn_pinjam_proc);
        //pdfviewer.fromAsset("contoh.pdf").load();
        AndroidNetworking.initialize(this);
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
                    //new PostData().execute(SERVER_ADDRS + "membaca", "membaca","id_member=" + session_user_id + "&id_buku=" + id_buku );
                    /*
                    filename = file_location.substring(file_location.lastIndexOf('/') + 1);
                    File file = new File(getBaseContext().getCacheDir(), filename);
                    if (!file.exists())
                    {
                        loading_dialog.dismiss();
                        loading_dialog = new ProgressDialog(ViewerActivity.this);
                        loading_dialog.setMessage("Mencoba menampilkan file buku. Tunggu sebentar...");
                        loading_dialog.setIndeterminate(true);
                        loading_dialog.show();
                        new DownloadFilePdf().execute(file_location, filename);
                    }
                    else {
//                        Log.i("BUKA FILE : ", file.getAbsolutePath());
//                        Toast.makeText(getBaseContext(), "BUKA FILE : " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        showpdf(file);
                    }
                     */


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
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                })
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        new PostData().execute(SERVER_ADDRS + "membaca/back", "back", "nim=" + session_user_nim + "&id_buku=" + id_buku);
                        File file = new File(getBaseContext().getCacheDir(), filename);
                        //Toast.makeText(getBaseContext(), "DELETE FILE : " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        if (file.exists()) {
                            //pdfviewer.removeAllViewsInLayout();
                            file.delete();
                            finish();
                        }
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
            loading_dialog.dismiss();
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
                loading_dialog.dismiss();
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
                loading_dialog.dismiss();
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
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(rslt.parameter);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                Log.d("responseCode PostData", "responseCode : " + responseCode);
                if (responseCode == 201 || responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
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
                loading_dialog.dismiss();
                Log.i("URL baca", result.url);
                Log.i("VALUE baca", result.value);
                if (result.value.equals("{\"status\":true,\"message\":\"Berhasil\"}")) {
                    filename = file_location.substring(file_location.lastIndexOf('/') + 1);
                    File file = new File(getBaseContext().getCacheDir(), filename);
                    if (!file.exists()) {
                        loading_dialog.dismiss();
                        loading_dialog = new ProgressDialog(ViewerActivity.this);
                        loading_dialog.setMessage("Mencoba menampilkan file buku. Tunggu sebentar...");
                        loading_dialog.setIndeterminate(true);
                        loading_dialog.show();
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
                    //id:4;title:Aircraft Operations Volume I Flight Procedures;file:Air_operation.pdf;file_location:http://172.168.0.1/elibrary/uploads/pdf/Air_operation.pdfduration:1;from:2019-12-27;end:2019-12-28
                    //
                    //
                    //
                    //id:11;title:Military Avionics Systems;file:Wiley_-_Military_Avionics_Systems.pdf;file_location:http://172.168.0.1/elibrary/uploads/pdf/Wiley_-_Military_Avionics_Systems.pdf duration:1;from:2019-12-27;end:2019-12-28
                    //id:20;title:Flight-crew human factors handbook  CAP 737;file:Flight-crew_human_factors_handbook.pdf;file_location:http://172.168.0.1/elibrary/uploads/pdf/Flight-crew_human_factors_handbook.pdf duration:1;from:2019-12-27;end:2019-12-28
                    //id:17;title:Stress in Aircraft and Shell Structure Volume 2;file:stress_in_aircraft_by_kuhn_Vol_2.pdf;file_location:http://172.168.0.1/elibrary/uploads/pdf/stress_in_aircraft_by_kuhn_Vol_2.pdf duration:1;from:2019-12-27;end:2019-12-28

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

    public void PostDataJSON(String url, String command, ResponseRequest responseRequest) {
//        String json = new Gson().toJson(responseRequest);
        JSONObject jsonObject = new JSONObject();
        JSONObject item= new JSONObject();
        JSONArray items= new JSONArray();
        try {
            item.put("id_buku",responseRequest.getItems().get(0).getIdBuku());
            item.put("qty",responseRequest.getItems().get(0).getQty());
            items.put(item);

            jsonObject.put("id_member", responseRequest.getIdMember());
            jsonObject.put("tanggal", responseRequest.getTanggal());
            jsonObject.put("durasi", responseRequest.getDurasi());
            jsonObject.put("items", items);

            Log.e("body", jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    AndroidNetworking.post(url)
            .addBodyParameter(jsonObject)
//                .addHeaders("Content-Type","application/json")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("ResulFAN", response.toString());
                }

                @Override
                public void onError(ANError anError) {
                    Log.e("errorFan", anError.getErrorBody()+" :::"+ anError.getMessage());
                }
            });
}


    public boolean checkInet(int msg) {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else {
            connected = false;
            if (msg == 1) {
                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(ViewerActivity.this).create();
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
        if (my_book_list.contains("id:" + id_book + ";")) {
            return true;
        } else
            return false;
    }

    void show_form_load() {
        ArrayList<String> arraylist = new ArrayList<String>();
        final Dialog form = new Dialog(ViewerActivity.this);
        form.setContentView(R.layout.activity_loan);
        form.setCancelable(false);
        //form.create();
        Button btn_loan = (Button) form.findViewById(R.id.btn_loan_proc);
        Spinner sp_loan = (Spinner) form.findViewById(R.id.sp_loan_duration);
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
                        loading_dialog = new ProgressDialog(ViewerActivity.this);
                        loading_dialog.setMessage("Mencoba meminjam file buku. Tunggu sebentar...");
                        loading_dialog.setIndeterminate(true);
                        loading_dialog.show();
                        //new GetData().execute(SERVER_ADDRS + "pinjam.php?user_id=" + session_user_id + "&id_buku=" + id_buku + "&duration=" + duration, "loan");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                        String thisdate = sdf.format(new Date());
                        List<ItemsItem> items = new ArrayList<>();
                        items.add(new ItemsItem(1, Integer.parseInt(id_buku)));
                        ResponseRequest responseRequest = new ResponseRequest(15030001, thisdate, Integer.parseInt(duration), items);
//                        String parameters = "{\n" +
//                                "\t\"id_member\":" + session_user_id + ",\n" +
//                                "\t\"tanggal\": " + thisdate + ",\n" +
//                                "\t\"durasi\": " + duration + ",\n" +
//                                "\t\"items\": [\n" +
//                                "\t\t{\n" +
//                                "\t\t\t\"id_buku\": " + id_buku + ",\n" +
//                                "\t\t\t\"qty\": 1\n" +
//                                "\t\t}\n" +
//                                "\t]\n" +
//                                "}";
                         PostDataJSON2(SERVER_ADDRS + "peminjaman", "loan",responseRequest);

                    }
                } else {
                    Toast.makeText(getBaseContext(), "Silahkan pilih durasi pinjaman", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void PostDataJSON2(String s, String loan, ResponseRequest responseRequest) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"id_member\":15030001,\"tanggal\":\"2020-01-07 15-05-16\",\"durasi\":1,\"items\":[{\"id_buku\":11,\"qty\":1}]}");
        Request request = new Request.Builder()
                .url("http://172.168.0.1/elibrary/api/v1/peminjaman")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.e("response", response.toString());
        } catch (IOException e) {
            Log.e("okhtpp",e.getMessage());
        }
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
