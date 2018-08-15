package com.zjh.coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.zjh.coolweather.db.City;
import com.zjh.coolweather.db.Country;
import com.zjh.coolweather.db.Province;
import com.zjh.coolweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author：Created by zhaojh on 2018/8/14 16:58.
 * Description:
 */

public class GsonUtil {

    /**
     * 处理网络请求返回省级数据
     *
     * @param response
     * @return
     */
    public static boolean handleProvinceRepose(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(object.getString("name"));
                    province.setProvinceCode(object.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理网络请求返回市级数据
     *
     * @param response
     * @return
     */
    public static boolean handleCityRepose(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    City city = new City();
                    city.setCityName(object.getString("name"));
                    city.setCityCode(object.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理网络请求返回县级数据
     *
     * @param response
     * @return
     */
    public static boolean handleCountryRepose(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    Country country = new Country();
                    country.setCountryName(object.getString("name"));
                    country.setWeatherId(object.getString("weather_id"));
                    country.setCityId(cityId);
                    country.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析天气预报数据信息
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject object = new JSONObject(response);
            JSONArray array = object.getJSONArray("HeWeather");
            String weatherContent = array.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
