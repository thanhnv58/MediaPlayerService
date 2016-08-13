package com.thanhnv.nicemp3player;

import java.text.SimpleDateFormat;

/**
 * Created by thanh on 7/19/2016.
 */
public class Song {
    private String name, path, artist, typeMusic, album;
    private int duration;
    private boolean isRunning = false;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    public Song(String name, String path, String artist, String typeMusic, String album, long duration) {
        this.name = name;
        this.path = path;
        this.artist = artist;
        this.typeMusic = typeMusic;
        this.album = album;
        this.duration =(int)(duration/1000);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getArtist() {
        return artist;
    }

    public int getDuration() {
        return duration;
    }

    public String getTime() {
        return simpleDateFormat.format(duration * 1000);
    }

    public void setRunning(boolean b){
        isRunning = b;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getAlbum() {
        return album;
    }
}
