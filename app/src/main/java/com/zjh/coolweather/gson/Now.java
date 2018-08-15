package com.zjh.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Author：Created by zhaojh on 2018/8/14 18:40.
 * Description:
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;
    }
}
