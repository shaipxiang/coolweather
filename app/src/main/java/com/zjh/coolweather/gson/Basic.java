package com.zjh.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Author：Created by zhaojh on 2018/8/14 18:29.
 * Description:
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;
    }

}
