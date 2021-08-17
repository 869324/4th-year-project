package com.dekut.dekutchat.utils;

public class GetId {

    public GetId (){}

    public String getId(String s1, String s2){
        int num = s1.compareTo(s2);
        String id = "";
        if(num < 0){
            id = s1+"-"+s2;
        }
        else if(num > 0){
            id = s2+"-"+s1;
        }
        else {
            id = s2+"-"+s1;
        }
        String id1 = id.replace(".", "_");
        return id1;
    }
}
