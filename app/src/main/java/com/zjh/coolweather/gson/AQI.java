package com.zjh.coolweather.gson;

/**
 * Author：Created by zhaojh on 2018/8/14 18:39.
 * Description:
 */

public class AQI {

    public AQICity city;

    public class AQICity{

        public String aqi;

        public String pm25;
    }
}
