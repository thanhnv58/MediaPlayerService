package com.thanhnv.servicemedia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thanh on 7/29/2016.
 */
public class ListSongAdapter extends BaseAdapter {

    private List<Song> listSong;
    private LayoutInflater inflater;


    public ListSongAdapter(Context context, List<Song> list) {
        listSong = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listSong.size();
    }

    @Override
    public Object getItem(int position) {
        return listSong.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.listview_item_song, parent, false);
            viewHolder.llItem = (LinearLayout) view.findViewById(R.id.ll_item);
            viewHolder.txtSongName = (TextView) view.findViewById(R.id.txt_name_song);
            viewHolder.txtArtist = (TextView) view.findViewById(R.id.txt_artist);
            viewHolder.txtTime = (TextView) view.findViewById(R.id.txt_duration);
            viewHolder.imgPlayOrPause = (ImageView) view.findViewById(R.id.img_ic_song);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Song song = listSong.get(position);
        viewHolder.txtSongName.setText(song.getName());
        viewHolder.txtArtist.setText(song.getArtist());
        viewHolder.txtTime.setText(song.getTime());

        if (song.isRunning()){
            viewHolder.imgPlayOrPause.setImageLevel(1);
        } else {
            viewHolder.imgPlayOrPause.setImageLevel(0);
        }

        view.setTag(viewHolder);
        return view;
    }

    private class ViewHolder {
        public LinearLayout llItem;
        public TextView txtSongName, txtArtist, txtTime;
        public ImageView imgPlayOrPause;
    }
}