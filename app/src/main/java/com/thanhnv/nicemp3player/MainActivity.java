package com.thanhnv.nicemp3player;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by thanh on 7/29/2016.
 */
public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SeekBar.OnSeekBarChangeListener, MediaService.MediaServiceInterface, MediaPlayer.OnCompletionListener, MediaService.OnStartSongService {

    private static final int PLAY = 1, PAUSE = 0;

    private ListSongAdapter adapter;
    private ImageView imgPlayPause, imgPrevious, imgNext, imgStop;
    private TextView txtDuration, txtCurentTime;
    private TextView txtCurentSongName, txtCurentArtist, txtCurentPosition;
    private ListView listViewSong;
    private SeekBar seekBarTime;
    private boolean isOff;
    private List<Song> listSong;

    private AsynTaskTime asynTaskTime;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    private boolean isConnectedMediaService = false;
    private MediaService mMediaService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaService.MyBinder mBinder = (MediaService.MyBinder) service;
            mMediaService = mBinder.getMediaService();
            mMediaService.setMediaServiceInterface(MainActivity.this);
            mMediaService.setOnCompleListener(MainActivity.this);
            mMediaService.setOnStartSong(MainActivity.this);
            mMediaService.getListSong(MainActivity.this);

            isOff = false;
            asynTaskTime = new AsynTaskTime();
            asynTaskTime.execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        connectService();

    }

    private void connectService() {
        Intent mediaServiceIntent = new Intent();
        mediaServiceIntent.setClass(this, MediaService.class);
        isConnectedMediaService = bindService(mediaServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void doneLoadSongFromDevice(List<Song> listSong) {
        this.listSong = listSong;
        adapter = new ListSongAdapter(this, this.listSong);
        listViewSong.setAdapter(adapter);

        Song song = listSong.get(mMediaService.getPosition());
        String strPosition = (mMediaService.getPosition() + 1) + "/" + listSong.size();
        txtCurentSongName.setText(song.getName());
        txtCurentArtist.setText(song.getArtist());
        txtCurentPosition.setText(strPosition);
    }




    private void initView() {

        listViewSong = (ListView) findViewById(R.id.listview_song);
        imgPlayPause = (ImageView) findViewById(R.id.img_play_pause);
        imgStop = (ImageView) findViewById(R.id.img_stop);
        imgPrevious = (ImageView) findViewById(R.id.img_previous);
        imgNext = (ImageView) findViewById(R.id.img_next);

        seekBarTime = (SeekBar) findViewById(R.id.seek_bar_time);
        seekBarTime.setOnSeekBarChangeListener(this);

        txtDuration = (TextView) findViewById(R.id.txt_total_duration);
        txtCurentTime = (TextView) findViewById(R.id.txt_current_time);

        txtCurentSongName = (TextView) findViewById(R.id.txt_main_title_song);
        txtCurentArtist = (TextView) findViewById(R.id.txt_main_artist);
        txtCurentPosition = (TextView) findViewById(R.id.txt_main_position);


//        listSong = mSoundMaster.getListSong();


        imgPlayPause.setOnClickListener(this);
        imgStop.setOnClickListener(this);
        imgPrevious.setOnClickListener(this);
        imgNext.setOnClickListener(this);

        listViewSong.setOnItemClickListener(this);
        listViewSong.setOnItemLongClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (!isConnectedMediaService){
            return;
        }
        switch (v.getId()){
            case R.id.img_play_pause:
                playSong();
                break;
            case R.id.img_stop:
                listSong.get(mMediaService.getPosition()).setRunning(false);
                adapter.notifyDataSetChanged();
                mMediaService.stop();
                imgPlayPause.setImageLevel(PAUSE);

                break;
            case R.id.img_previous:
                listSong.get(mMediaService.getPosition()).setRunning(false);


                mMediaService.previous();
                listSong.get(mMediaService.getPosition()).setRunning(true);
                imgPlayPause.setImageLevel(PLAY);
                adapter.notifyDataSetChanged();
                break;
            case R.id.img_next:
                nextSong();
                break;
            default:
                break;
        }
    }

    private void playSong() {
        if (mMediaService.isPlaying()){
            listSong.get(mMediaService.getPosition()).setRunning(false);
            mMediaService.pause();
            imgPlayPause.setImageLevel(PAUSE);
            adapter.notifyDataSetChanged();
        } else {
            mMediaService.play();
            imgPlayPause.setImageLevel(PLAY);
            listSong.get(mMediaService.getPosition()).setRunning(true);
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listSong.get(mMediaService.getPosition()).setRunning(false);
        mMediaService.setPosition(position);
        mMediaService.play();
        imgPlayPause.setImageLevel(PLAY);
        listSong.get(mMediaService.getPosition()).setRunning(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_detail_song);

        TextView txtNameSong, txtArtist, txtPath, txtDuration, txtAlbum;
        txtNameSong = (TextView) dialog.findViewById(R.id.txt_dialog_name_song);
        txtAlbum = (TextView) dialog.findViewById(R.id.txt_dialog_album_song);
        txtArtist = (TextView) dialog.findViewById(R.id.txt_dialog_artist_song);
        txtPath = (TextView) dialog.findViewById(R.id.txt_dialog_path_song);
        txtDuration = (TextView) dialog.findViewById(R.id.txt_dialog_duration_song);
        Song song = listSong.get(position);
        txtNameSong.setText(song.getName());
        txtAlbum.setText("Album : " + song.getAlbum());
        txtArtist.setText("Artist : " + song.getArtist());
        txtDuration.setText("Duration : " + song.getTime());
        txtPath.setText("Path : " + song.getPath());


        dialog.show();
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        mMediaService.setCurentTime(progress * 1000);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextSong();
    }

    private void nextSong() {
        listSong.get(mMediaService.getPosition()).setRunning(false);


        mMediaService.next();
        listSong.get(mMediaService.getPosition()).setRunning(true);
        imgPlayPause.setImageLevel(PLAY);
        adapter.notifyDataSetChanged();
    }



    private class AsynTaskTime extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            while (!isOff){
                if (!mMediaService.isPlaying()){
                    continue;
                }
                try {
                    Thread.sleep(800);
                    publishProgress(mMediaService.getCurentTime());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            txtCurentTime.setText(simpleDateFormat.format(values[0]));
            seekBarTime.setProgress(values[0] / 1000);
        }
    }

    @Override
    protected void onDestroy() {
        isOff = true;
        super.onDestroy();
    }

    @Override
    public void onStartSong(int duration, String nameSong, String artist, String curentPosition){
        seekBarTime.setMax(duration / 1000);
        txtDuration.setText(simpleDateFormat.format(duration));
        txtCurentTime.setText("00:00");
        txtCurentSongName.setText(nameSong);
        txtCurentArtist.setText(artist);
        txtCurentPosition.setText(curentPosition);
    }
}