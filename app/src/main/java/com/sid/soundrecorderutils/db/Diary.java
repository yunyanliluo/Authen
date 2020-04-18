package com.sid.soundrecorderutils.db;

import java.io.Serializable;

public class Diary implements Serializable {
    public int _id;
    //    public String name;
    public String date; //2020_04_10
    public String time; //09_56_58
    public int isImage; //1 for image, 0 for audio
//    public String username;
//    public String password;
//    public String dateSignup; //date of register
//    public String dateLastLogin; //last date of login
    public String discription;
//    public String uploadID;

    public Diary() {
    }

    public Diary(String date, String time, int isImage, String discription) {
        this.date = date;
        this.time = time;
        this.isImage = isImage;
        this.discription = discription;
    }

    public Diary(String date, String time, int isImage) {
        this.date = date;
        this.time = time;
        this.isImage = isImage;
    }

    public Diary(Diary diary) {
        _id = diary._id;
        date = diary.date;
        time = diary.time;
        isImage = diary.isImage;
        discription = diary.discription;
    }

    public String toString() {
        String stringDiary = new String();
        stringDiary += String.valueOf(_id);
        stringDiary += ":";
        stringDiary += this.date;
        stringDiary += "_";
        stringDiary += this.time;
        if(isImage == 1) {
            stringDiary += ".jpg ";
        }
        else {
            stringDiary += ".mp3 ";
        }
        stringDiary += this.discription;
        return stringDiary;
    }
}

