package com.joatsy.apps.elibrarystta.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joatsy.apps.elibrarystta.Data.DataBook;
import com.joatsy.apps.elibrarystta.R;

import java.util.ArrayList;

public class AdapterRecyclerBook extends RecyclerView.Adapter<AdapterRecyclerBook.MyViewHolder> {
    private LayoutInflater inflater;
    public static ArrayList<DataBook> editModelArrayList;

    public AdapterRecyclerBook(Context ctx, ArrayList<DataBook> editModelArrayList){
        inflater = LayoutInflater.from(ctx);
        this.editModelArrayList = editModelArrayList;
    }

    @Override
    public AdapterRecyclerBook.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.recycler_book, parent, false);
        AdapterRecyclerBook.MyViewHolder holder = new AdapterRecyclerBook.MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final AdapterRecyclerBook.MyViewHolder holder, final int position) {
        holder.viewText.setText(editModelArrayList.get(position).getValue());
        holder.imageView.setImageResource(Integer.valueOf(editModelArrayList.get(position).getIdRoot()));
        if (editModelArrayList.get(position).getStatus()==0)
            holder.relativeLyt.setBackgroundColor(Color.parseColor("#ffffff"));
        else
            holder.relativeLyt.setBackgroundColor(Color.parseColor("#cccccc"));

    }

    @Override
    public int getItemCount() {
        return editModelArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        protected RelativeLayout relativeLyt;
        protected TextView viewText;
        protected ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            relativeLyt = (RelativeLayout) itemView.findViewById(R.id.lyt_book);
            viewText = (TextView) itemView.findViewById(R.id.tx_book_name);
            imageView= (ImageView) itemView.findViewById(R.id.img_book_icon);
        }

    }
}
