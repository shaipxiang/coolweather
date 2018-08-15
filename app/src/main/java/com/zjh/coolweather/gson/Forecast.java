package com.zjh.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Author：Created by zhaojh on 2018/8/14 18:45.
 * Description:
 */

public class Forecast {

    public String date;
    @SerializedName("tmp")
    public Temperature temperature;
    @SerializedName("cond")
    public More more;

    public class Temperature {
        public String max;
        public String min;
    }

    public class More {
        @SerializedName("txt_d")
        public String info;
    }
}
