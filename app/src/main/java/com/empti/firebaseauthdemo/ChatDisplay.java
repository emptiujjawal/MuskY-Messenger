package com.empti.firebaseauthdemo;

/**
 * Created by Emptii on 11-08-2017.
 */

public class ChatDisplay {
    public String name;
    public String status;
    public String thumb_img;

    public ChatDisplay(){

    }

    public ChatDisplay(String name, String status, String thumb_img) {
        this.name = name;
        this.status = status;
        this.thumb_img = thumb_img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_img() {
        return thumb_img;
    }

    public void setThumb_img(String thumb_img) {
        this.thumb_img = thumb_img;
    }
}
