package com.acproject;

import java.util.HashSet;
import java.util.Stack;

public class suanfa {
    public String removeDuplicates(String S) {
        StringBuilder stringBuilder = new StringBuilder();
        int index = -1;
        for(int i=0;i<S.length();i++){
            char ch = S.charAt(i);
            if(stringBuilder.charAt(index) == ch && index>=0){
                stringBuilder.deleteCharAt(index);
                index--;
            }
            else{
                stringBuilder.append(ch);
                index++;
            }
        }
        return stringBuilder.toString();
    }
}
