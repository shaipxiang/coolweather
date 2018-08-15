package com.zjh.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zjh.coolweather.R;
import com.zjh.coolweather.gson.Forecast;
import com.zjh.coolweather.gson.Weather;
import com.zjh.coolweather.service.AutoUpdateService;
import com.zjh.coolweather.util.GsonUtil;
import com.zjh.coolweather.util.HttpUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Author：Created by zhaojh on 2018/8/14 18:50.
 * Description: 天气页面
 */

public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_update_time)
    TextView mTvUpdateTime;
    @BindView(R.id.tv_degree)
    TextView mTvDegree;
    @BindView(R.id.tv_weather_info)
    TextView mTvWeatherInfo;
    @BindView(R.id.ll_forecast_layout)
    LinearLayout mLlForecastLayout;
    @BindView(R.id.tv_aqi)
    TextView mTvAqi;
    @BindView(R.id.tv_pm25)
    TextView mTvPm25;
    @BindView(R.id.tv_comfort)
    TextView mTvComfort;
    @BindView(R.id.tv_carwash)
    TextView mTvCarwash;
    @BindView(R.id.tv_sport)
    TextView mTvSport;
    @BindView(R.id.sv_layout)
    ScrollView mSvLayout;
    @BindView(R.id.iv_bg)
    ImageView mIvBg;
    @BindView(R.id.swipe_refresh)
    public SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.drawer_layout)
    public DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        final String weatherId;
        if (weatherString != null) {
            // 有缓存数据，就直接解析天气数据
            Weather weather = GsonUtil.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            // 无缓存数据，从网络请求数据
            weatherId = getIntent().getStringExtra("weather_id");
            mSvLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        // 下拉刷新最新天气信息
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }

        });        // 获取必应上的每日一图，更改天气预报的背景
        String bgUrl = preferences.getString("bing_pic", null);
        if (bgUrl != null) {
            Glide.with(this).load(bgUrl).into(mIvBg);
        } else {
            loadBingPic();
        }
    }

    /**
     * 根据城市id，获取城市天气信息
     */
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=ce1193bd0ff641e5952e1a6c93eae6d1";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = GsonUtil.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity
                                    .this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            Intent intent = new Intent(WeatherActivity.this, AutoUpdateService.class);
                            startService(intent);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                        }
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void loadBingPic() {
        String loadPicUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(loadPicUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String pingString = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", pingString);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(pingString).into(mIvBg);
                    }
                });
            }
        });
    }

    /**
     * 展示天气信息
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        mTvTitle.setText(cityName);
        mTvUpdateTime.setText(updateTime);
        mTvDegree.setText(degree);
        mTvWeatherInfo.setText(weatherInfo);
        mLlForecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, mLlForecastLayout, false);
            TextView tvData = (TextView) view.findViewById(R.id.tv_data);
            TextView tvInfo = (TextView) view.findViewById(R.id.tv_info);
            TextView tvMax = (TextView) view.findViewById(R.id.tv_max);
            TextView tvMin = (TextView) view.findViewById(R.id.tv_min);
            tvData.setText(forecast.date);
            tvInfo.setText(forecast.more.info);
            tvMax.setText(forecast.temperature.max + "℃");
            tvMin.setText(forecast.temperature.min + "℃");
            mLlForecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            mTvAqi.setText(weather.aqi.city.aqi);
            mTvPm25.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        mTvComfort.setText(comfort);
        mTvCarwash.setText(carWash);
        mTvSport.setText(sport);
        mSvLayout.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.btn_back, R.id.ll_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
            case R.id.ll_back:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
    }
}
