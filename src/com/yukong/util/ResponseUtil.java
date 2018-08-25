package com.yukong.util;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    private static Map<String, String> map;

    static {
        map = new HashMap<>();
        map.put("yukong", "一位热爱变编程，有geek精神的同学");
        map.put("马云", "只是一位英语老师");
        map.put("淘宝", "女人的最爱");
        map.put("刺激战场", "大吉大利，今晚吃鸡");
    }

    public static String queryMessage(String key) {
        String rep = map.get(key);
        if (rep == null) {
            rep = "小yu太笨了，听不懂您在说什么哦。";
        }
        return rep;
    }

}
