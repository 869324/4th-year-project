package com.dekut.dekutchat.utils;

public class ByteCalc {
    long Byte = 1024;
    long KB = 1024 * Byte;
    long MB = 1024 * KB;
    long GB = 1024 * MB;

    public String getSize(long bytes){
        String size = null;
        if (bytes < MB){
            size = String.valueOf(bytes / KB) + " kb";
        }

        else if (bytes < GB){
            size = String.valueOf(bytes / MB) + " MB";
        }

        else {
            size = String.valueOf(bytes / GB) + " GB";
        }
        return size;
    }
}
