package com.a2013myway.team.capstonedesign;

import java.util.HashMap;

/**
 * Created by shotc on 2018-03-27.
 */

public class Gattattributes {

    private static HashMap<String,String> attributes = new HashMap();

    public static String lookup(String uuid, String defaultName){
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
