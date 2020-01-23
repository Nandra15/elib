package com.joatsy.apps.elibrarystta.view;

import android.app.ProgressDialog;
import android.content.Intent;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.joatsy.apps.elibrarystta.view.MainActivity.master_dir;

public class MybookActivity extends AppCompatActivity {
    Intent intent;
    private RecyclerView recycler_book;
    public static AdapterRecyclerBook recycler_adapter;
    public static ArrayList<DataBook> data_book;
    ArrayList<String> arraylist = new ArrayList<String>();
    private ProgressDialog loading_dialog;
    private SharedPrefs pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mybook);
        recycler_book = (RecyclerView) findViewById(R.id.rc_mybook);
        pref = new SharedPrefs(this);
        data_book = populateList();
        if (data_book.size()<=0)
        {
            Toast.makeText(getBaseContext(), "Anda tidak mempunyai daftar buku pinjaman!" , Toast.LENGTH_LONG).show();
            finish();
        }
        intent = getIntent();
        recycler_adapter = new AdapterRecyclerBook(this,data_book);
        recycler_book.setAdapter(recycler_adapter);
        recycler_book.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        recycler_book.addOnItemTouchListener(
                new RecyclerItemClickListener(getBaseContext(), recycler_book ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String nama_buku = data_book.get(position).getValue();
                        String id_buku = data_book.get(position).getId();
                        String file_location = data_book.get(position).getLocation();
                        String idPeminjaman= data_book.get(position).getId_peminjaman();
                        //Toast.makeText(getBaseContext(), "List (" + id_buku + ")" + nama_buku + " Ditekan" , Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MybookActivity.this, ReadBookActivity.class);
                        intent.putExtra("id_buku", id_buku);
                        intent.putExtra("judul_buku", nama_buku);
                        intent.putExtra("file_location", file_location);
                        intent.putExtra("id_peminjaman", idPeminjaman);
                        startActivity (intent);
                        finish();
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
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

    private ArrayList<DataBook> populateList(){
        ArrayList<DataBook>  list = new ArrayList<>();
        String my_book_list = file_read(master_dir,"list.dat");
//        String my_book_list = pref.getListData();
        String[] data_list = my_book_list.split("&&&&");
        Log.d("list_abc", my_book_list.toString() + " x");
        for (int i = 0; i < data_list.length; i++) {
            if (!data_list[i].equals(""))
            {
                Log.d("data_list [" + i + "] :",data_list[i]);
                String[] data_list_item = data_list[i].split(";");
                String id_book="";
                String id_peminjaman="";
                String name_book="";
                String file_location="";
                for (int a = 0; a < data_list_item.length; a++) {
                    if (!data_list_item[a].equals(""))
                    {

                    if (a==0)
                        {
                           id_book = data_list_item[a].replace("id:","");
                        }
                        else if (a==1)
                        {
                            name_book = data_list_item[a].replace("title:","");
                        }
                        else if (a==2)
                        {
                            file_location = data_list_item[a].replace("location:","");
                        }
                        else if (a==7)
                        {
                            id_peminjaman = data_list_item[a].replace("id_peminjaman:","");
                        }
                    }
                }
                if (!id_book.equals("") && !name_book.equals("")) {
                    DataBook editModel = new DataBook(String.valueOf(R.drawable.ic_library_books_purple_24dp), id_book, name_book, 0,file_location, id_peminjaman);
                    list.add(editModel);
                }
            }
        }
        return list;
    }
}
