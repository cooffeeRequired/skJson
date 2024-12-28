package cz.coffeerequired;

import com.google.gson.GsonBuilder;

public class Test {
    public static void main(String[] args) {
        var gson = new GsonBuilder().setLenient().create();


        System.out.println(gson.toJsonTree("test raw ?"));
    }
}
