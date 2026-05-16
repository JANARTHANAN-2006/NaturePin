package com.example.naturepin;

public class ImgBBResponse {
    public Data data;
    public boolean success;
    public int status;

    public static class Data {
        public String url;
        public String display_url;
    }
}
