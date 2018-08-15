package com.zjh.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Author：Created by zhaojh on 2018/8/14 18:47.
 * Description:
 */

public class Weather {

    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

}
