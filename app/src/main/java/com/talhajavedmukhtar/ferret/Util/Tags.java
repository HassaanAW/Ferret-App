package com.talhajavedmukhtar.ferret.Util;

/**
 * Created by Talha on 11/18/18.
 */

public class Tags {
    public final static String application = "Ferret";

    public static String makeTag(String tag){
        return application + "." + tag;
    }
}
