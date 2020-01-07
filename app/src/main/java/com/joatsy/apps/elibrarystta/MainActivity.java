package com.joatsy.apps.elibrarystta;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    static String SERVER_ADDRS = "http://172.168.0.1/elibrary/api/v1/";
    public static String user_agent="Apps Nandra 20.19 (E-Library STTA)";
    Intent intent;
    CardView btn_caribuku, btn_pinjaman, btn_syarat, btn_pengaturan;
    ImageView ic_syarat, ic_pengaturan, ic_cari;
    TextView tx_syarat, tx_pengaturan, tx_cari;
    public static String root_data="";
    public static String master_dir="";
    public static String log_dir="";
    public static String session_user_nim="";
    public static String session_user_id="";
    public static String session_user_name="";
    public static String session_user_hp="";
    public static String session_user_addr="";
    public static String session_user_id_temp="";
    public static  boolean mode_offline=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_caribuku = findViewById(R.id.btn_home_cari);
        btn_pinjaman = findViewById(R.id.btn_home_pinjaman);
        btn_syarat = findViewById(R.id.btn_home_syarat);
        btn_pengaturan = findViewById(R.id.btn_home_pengaturan);

        ic_syarat = findViewById(R.id.btn_home_syarat_proc);
        ic_pengaturan = findViewById(R.id.btn_home_pengaturan_proc);
        ic_cari = findViewById(R.id.btn_home_cari_proc);
        tx_syarat = findViewById(R.id.lb_home_syarat);
        tx_pengaturan = findViewById(R.id.lb_home_pengaturan);
        tx_cari = findViewById(R.id.lb_home_cari);
        PackageManager m = getPackageManager();
        String s = getPackageName();
        intent = getIntent();
        try
        {
            PackageInfo p = m.getPackageInfo(s, 0);
            root_data = p.applicationInfo.dataDir;
            Log.e("joatsy ---> ", "root dir :  " + root_data );
            master_dir = root_data + "/master/";
            log_dir  = root_data + "/log/";
            final File newFile = new File(master_dir);
            newFile.mkdir();
            session_user_id_temp = file_read(master_dir,"account.dat");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("yourtag", "Error Package name not found ", e);
        }
        clear_chace();
        if (intent.hasExtra("session_user_id")) {
            session_user_id = getIntent().getStringExtra("session_user_id");
            session_user_nim = getIntent().getStringExtra("session_user_nim");
            session_user_name = getIntent().getStringExtra("session_user_name");
            session_user_hp = getIntent().getStringExtra("session_user_hp");
            session_user_addr = getIntent().getStringExtra("session_user_addr");
        }


       if (session_user_id.equals(""))
        {
            if (checkInet(0)==true)
            {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity (intent);
                finish();
            }
            else{
                mode_offline=true;
                session_user_id=session_user_id_temp;
                tx_syarat.setTextColor(Color.parseColor("#444444"));
                ic_syarat.setImageResource(R.drawable.ic_security_gray_24dp);
                tx_pengaturan.setTextColor(Color.parseColor("#444444"));
                ic_pengaturan.setImageResource(R.drawable.ic_settings_gray_24dp);
                tx_cari.setTextColor(Color.parseColor("#444444"));
                ic_cari.setImageResource(R.drawable.ic_find_in_page_gray_24dp);
            }

        }
        else
        {
            if (!session_user_id_temp.equals(""))
            {
                if (!session_user_id_temp.contains(session_user_id))
                {
                    clear_data();
                    file_write(true,session_user_id,master_dir,"account.dat");

                }
            }
        }
        btn_caribuku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode_offline==true)
                {

                }
                else
                {
                    Intent intent = new Intent(MainActivity.this, FindBookActivity.class);
                    startActivity (intent);
                }
                //Toast.makeText(getBaseContext(), "Tombol Cari Buku Ditekan" , Toast.LENGTH_LONG).show();

//                Intent intent = new Intent(MainActivity.this, ViewerActivity.class);
//                intent.putExtra("id_buku", "1");
//                intent.putExtra("judul_buku", "Contoh Dari DB");
//                startActivity (intent);

            }
        });

        btn_pinjaman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getBaseContext(), "Tombol Pinjaman Ditekan" , Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, MybookActivity.class);
                startActivity (intent);
            }
        });

        btn_syarat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode_offline==true)
                {

                }
                else
                {
                    Intent intent = new Intent(MainActivity.this, TermActivity.class);
                    startActivity (intent);
                }
                //Toast.makeText(getBaseContext(), "Tombol Syarat Ditekan" , Toast.LENGTH_LONG).show();

            }
        });

        btn_pengaturan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode_offline==true)
                {

                }
                else
                {
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    startActivity (intent);
                }
                //Toast.makeText(getBaseContext(), "Tombol Pengaturan Ditekan" , Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin ingin keluar aplikasi?")
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
                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(MainActivity.this).create();
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

    public class GetDataResult
    {
        public String value;
        public String command;
        public String url;
    }

    void clear_chace()
    {
        File dir_cache = new File(getBaseContext().getCacheDir(),"");
        File[] files = dir_cache.listFiles();
        for (File file:files)
        {
            if (file.exists())
            {
                file.delete();
            }
        }
    }

    void clear_data()
    {
        File dir_cache = new File(master_dir,"");
        File[] files = dir_cache.listFiles();
        for (File file:files)
        {
            if (file.exists())
            {
                file.delete();
            }
        }
    }
}
