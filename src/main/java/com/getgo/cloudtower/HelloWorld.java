package com.getgo.cloudtower;

import static spark.Spark.*;

/**
 * Created by amirnashat on 7/6/16.
 */
public class HelloWorld {
    public static void main (String[] args) {
        get("/hello", (req,res) -> "Hello World");
    }
}
