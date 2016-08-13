package com.thanhnv.nicemp3player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thanh on 8/11/2016.
 */
public class MediaService extends Service {
    private static final String TAG = MediaService.class.getSimpleName();
    private static final int    STATE_IDLE = -1, STATE_PLAYING = 1, STATE_PAUSE = 2,
            STATE_STOP = 3, STATE_PREVIOUS = 4, STATE_NEXT = 5,
            STATE_CLICK_RD = 6;

    private MediaPlayer myMediaPlayer = new MediaPlayer();
    private Context mContext;

    private List<Song> listSong = new ArrayList<Song>();
    private int position = 0;

    private int state = STATE_IDLE;

    private MediaPlayer.OnCompletionListener onCompleListener;
    public void setOnCompleListener(MediaPlayer.OnCompletionListener event){
        onCompleListener = event;
        myMediaPlayer.setOnCompletionListener(onCompleListener);
    }

    private OnStartSongService onStartSong;
    public void setOnStartSong(OnStartSongService event){
        onStartSong = event;
    }
    public interface OnStartSong{
        void onStartSong(int maxSeekbar, String nameSong, String artist, String position);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public int getPosition() {
        return position;
    }

    public class MyBinder extends Binder{
        public MediaService getMediaService(){
            return MediaService.this;
        }
    }

    public void getListSong(Context context){
        Log.d(TAG, "get list song");
        mContext = context;
        GetListSongAsyntask getListSongAsyntask = new GetListSongAsyntask();
        getListSongAsyntask.execute(context);

//        Log.d(TAG, "do in back ground");
//        String projection[] = new String[]{
//                //Name
//                MediaStore.MediaColumns.TITLE,
//                //FileName
//                MediaStore.MediaColumns.DISPLAY_NAME,
//                //Path
//                MediaStore.MediaColumns.DATA,
//                MediaStore.Audio.Media.ARTIST,
//                MediaStore.Audio.Media.ALBUM,
//                MediaStore.Audio.Media.DURATION
//        };
//        Cursor cursor = context.getContentResolver()
//                .query(
//                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                        projection,
//                        null,
//                        null,
//                        null
//                );
//        if (cursor == null) {
//            return;
//        }
//        cursor.moveToFirst();
//        while (cursor.isAfterLast() == false) {
//            String name = cursor.getString(cursor.getColumnIndex(
//                    MediaStore.MediaColumns.TITLE));
//            String fileName = cursor.getString(cursor.getColumnIndex(
//                    MediaStore.MediaColumns.DISPLAY_NAME));
//            String path = cursor.getString(cursor.getColumnIndex(
//                    MediaStore.MediaColumns.DATA));
//            String artist = cursor.getString(cursor.getColumnIndex(
//                    MediaStore.Audio.Media.ARTIST));
//            String album = cursor.getString(cursor.getColumnIndex(
//                    MediaStore.Audio.Media.ALBUM));
//            int duration = cursor.getInt(cursor.getColumnIndex(
//                    MediaStore.Audio.Media.DURATION));
//
//            Song song = new Song(name, path, artist, "nhac tre", album, duration);
//            listSong.add(song);
//
//            Log.i(TAG, "Name: "+name);
//            Log.i(TAG, "File Name: "+fileName);
//            Log.i(TAG, "Path: "+path);
//            Log.i(TAG, "Artist: "+artist);
//            Log.i(TAG, "Album: "+album);
//            Log.i(TAG, "Duration: "+duration);
//            Log.i(TAG, "-----------------------");
//            cursor.moveToNext();
//        }
//        cursor.close();
//        mMediaServiceInterface.doneLoadSongFromDevice(listSong);

    }

    private class GetListSongAsyntask extends AsyncTask<Context, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Context... params) {
            Log.d(TAG, "do in back ground");
            String projection[] = new String[]{
                    //Name
                    MediaStore.MediaColumns.TITLE,
                    //FileName
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    //Path
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION
            };
            Cursor cursor = params[0].getContentResolver()
                    .query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            projection,
                            null,
                            null,
                            null
                    );
            if (cursor == null) {
                return false;
            }
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                String name = cursor.getString(cursor.getColumnIndex(
                        MediaStore.MediaColumns.TITLE));
                String fileName = cursor.getString(cursor.getColumnIndex(
                        MediaStore.MediaColumns.DISPLAY_NAME));
                String path = cursor.getString(cursor.getColumnIndex(
                        MediaStore.MediaColumns.DATA));
                String artist = cursor.getString(cursor.getColumnIndex(
                        MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(
                        MediaStore.Audio.Media.ALBUM));
                int duration = cursor.getInt(cursor.getColumnIndex(
                        MediaStore.Audio.Media.DURATION));

                Song song = new Song(name, path, artist, "nhac tre", album, duration);
                listSong.add(song);

                Log.i(TAG, "Name: "+name);
                Log.i(TAG, "File Name: "+fileName);
                Log.i(TAG, "Path: "+path);
                Log.i(TAG, "Artist: "+artist);
                Log.i(TAG, "Album: "+album);
                Log.i(TAG, "Duration: "+duration);
                Log.i(TAG, "-----------------------");
                cursor.moveToNext();
            }
            cursor.close();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                Log.d(TAG, listSong.size() + "");
                mMediaServiceInterface.doneLoadSongFromDevice(listSong);
            }
        }

    }

    public void play(){
        if (state == STATE_PAUSE){
            myMediaPlayer.start();
            state = STATE_PLAYING;
            return;
        }

        try {
            myMediaPlayer.reset();
            myMediaPlayer.setDataSource(mContext, Uri.parse(listSong.get(position).getPath()));
            myMediaPlayer.prepare();
            myMediaPlayer.start();
            state = STATE_PLAYING;
            Song song = listSong.get(position);
            String strPosition = (position + 1) + "/" +listSong.size();
            onStartSong.onStartSong(myMediaPlayer.getDuration(), song.getName(), song.getArtist(), strPosition);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        if (state == STATE_IDLE){
            state = STATE_STOP;
            return;
        }
        myMediaPlayer.stop();
        state = STATE_STOP;
    }

    public void pause(){
        if (state == STATE_PLAYING){
            myMediaPlayer.pause();
            state = STATE_PAUSE;
        }
    }

    public void next(){
        if (position >= listSong.size() - 1){
            position = 0;
        } else {
            position ++;
        }
        state = STATE_NEXT;
        play();
    }

    public void previous(){
        if (position <= 0){
            position = listSong.size() - 1;
        } else {
            position --;
        }
        state = STATE_PREVIOUS;
        play();
    }

    public void setPosition(int position) {
        this.position = position;
        state = STATE_CLICK_RD;
    }

    public boolean isPlaying() {
        return state == STATE_PLAYING;
    }

    public int getCurentTime() {
        return myMediaPlayer.getCurrentPosition();
    }

    public List<Song> getListSong(){
        return listSong;
    }

    public void setCurentTime(int i) {
        myMediaPlayer.seekTo(i);
    }

    public interface OnStartSongService{
        void onStartSong(int maxSeekbar, String nameSong, String artist, String position);
    }


    public interface MediaServiceInterface{
        void doneLoadSongFromDevice(List<Song> listSong);
    }

    private MediaServiceInterface mMediaServiceInterface;
    public void setMediaServiceInterface(MediaServiceInterface event){
        mMediaServiceInterface = event;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "destroy");
        super.onDestroy();
    }
}
