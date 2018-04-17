package com.empti.firebaseauthdemo;

/**
 * Created by Emptii on 05-08-2017.
 */

public class Users {
    public String name;
    public String status;
    public String image;
    public String thumb_img;

    public Users(){

    }

    public Users(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_img = thumb_img;
    }

    public String getThumb_img() {
        return thumb_img;
    }

    public void setThumb_img(String thumb_img) {
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
