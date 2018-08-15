package com.zjh.coolweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zjh.coolweather.MainActivity;
import com.zjh.coolweather.R;
import com.zjh.coolweather.activity.WeatherActivity;
import com.zjh.coolweather.db.City;
import com.zjh.coolweather.db.Country;
import com.zjh.coolweather.db.Province;
import com.zjh.coolweather.util.GsonUtil;
import com.zjh.coolweather.util.HttpUtil;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Author：Created by zhaojh on 2018/8/14 17:13.
 * Description:
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.list_view)
    ListView mListView;
    Unbinder unbinder;
    @BindView(R.id.ll_back)
    LinearLayout llBack;

    private ProgressDialog mProgressDialog;
    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<Country> countryList = new ArrayList<>();
    private Province selectedProvince;
    private City selectedCity;

    private int currenLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        unbinder = ButterKnife.bind(this, view);
        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (currenLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCitys();
                } else if (currenLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCountrys();
                } else if (currenLevel == LEVEL_COUNTRY) {
                    String weatherId = countryList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.mDrawerLayout.closeDrawers();
                        activity.mSwipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询省份
     */
    private void queryProvinces() {
        mTvTitle.setText("中国");
        llBack.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            currenLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询城市
     */
    private void queryCitys() {
        mTvTitle.setText(selectedProvince.getProvinceName());
        llBack.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currenLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china" + "/" + provinceCode;
            queryFromServer(address, "city");
        }
    }


    /**
     * 查询县
     */
    private void queryCountrys() {
        mTvTitle.setText(selectedCity.getCityName());
        llBack.setVisibility(View.VISIBLE);
        countryList = LitePal.where("cityid=?", String.valueOf(selectedCity.getId())).find(Country.class);
        if (countryList.size() > 0) {
            dataList.clear();
            for (Country country : countryList) {
                dataList.add(country.getCountryName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currenLevel = LEVEL_COUNTRY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china" + "/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "country");
        }
    }

    /**
     * 网络请求省市县数据
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = GsonUtil.handleProvinceRepose(responseText);
                } else if ("city".equals(type)) {
                    result = GsonUtil.handleCityRepose(responseText, selectedProvince.getId());
                } else if ("country".equals(type)) {
                    result = GsonUtil.handleCountryRepose(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCitys();
                            } else if ("country".equals(type)) {
                                queryCountrys();
                            }

                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setTitle("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.ll_back, R.id.btn_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
            case R.id.btn_back:
                if (currenLevel == LEVEL_COUNTRY) {
                    queryCitys();
                } else if (currenLevel == LEVEL_CITY) {
                    queryProvinces();
                }
                break;
        }
    }
}
