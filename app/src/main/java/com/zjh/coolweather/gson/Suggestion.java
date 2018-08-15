package com.zjh.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Author：Created by zhaojh on 2018/8/14 18:42.
 * Description:
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort {
        @SerializedName("txt")
        public String info;
    }


    public class CarWash {
        @SerializedName("txt")
        public String info;
    }

    public class Sport {
        @SerializedName("txt")
        public String info;
    }
}
