package com.sid.soundrecorderutils.util;

public class StringUtil {

    //获取第一个文件名，并删除
    public static String[] getAndDelete(String s) {
        String[] res = new String[2];
        String[] array = s.split(" ");

        String str = "";
        for (int i = 1; i < array.length; ++i) {
            str = append(str, array[i]);
        }
        res[0] = array[0];
        res[1] = str;
        return res;
    }

    //追加一条记录
    public static String append(String s1, String s2) {
        String s;
        if (s1 == null || s1.equals(""))
            s = s2;
        else
            s = s1 + " " + s2;
        return s;
    }


}
