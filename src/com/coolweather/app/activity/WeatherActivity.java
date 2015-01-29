package com.coolweather.app.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.CityWeather;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;
import com.example.coolweather.R;

public class WeatherActivity extends Activity implements OnClickListener {
	private CoolWeatherDB coolWeatherDB;

	private LinearLayout weatherInfoLayout;
	/**
	 * 用于显示城市名
	 * */
	private TextView cityNameText;
	/**
	 * 用于显示发布时间
	 */
	private TextView publishText;
	/**
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;
	/**
	 * 用于显示气温1
	 */
	private TextView temp1Text;
	/**
	 * 用于显示气温2
	 */
	private TextView temp2Text;
	/**
	 * 用于显示当前日期
	 */
	private TextView currentDateText;
	/**
	 * 切换城市按钮
	 */
	private Button switchCity;
	/**
	 * 更新天气按钮
	 */
	private Button refreshWeather;
	/**
	 * 设置按钮
	 */
	private Button setting;
	/**
	 * 添加城市
	 */
	private LinearLayout addCity;

	/**
	 * 是否从添加城市跳转过来
	 */
	boolean isAdd;

	/**
	 * 尚未设置主城市
	 */
	boolean noMainCity;

	String currentDate;

	public class CityWeatherAdapter extends ArrayAdapter<CityWeather> {

		private int resourceId;

		public CityWeatherAdapter(Context context, int resource,
				List<CityWeather> objects) {
			super(context, resource, objects);
			resourceId = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CityWeather cityWeather = getItem(position);
			View view;
			ViewHolder viewHolder;
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(resourceId,
						null);
				viewHolder = new ViewHolder();
				viewHolder.city_name_s = (TextView) view
						.findViewById(R.id.city_name_s);
				viewHolder.publish_text_s = (TextView) view
						.findViewById(R.id.publish_text_s);
				viewHolder.current_date_s = (TextView) view
						.findViewById(R.id.current_date_s);
				viewHolder.weather_desp_s = (TextView) view
						.findViewById(R.id.weather_desp_s);
				viewHolder.temp1_s = (TextView) view.findViewById(R.id.temp1_s);
				viewHolder.temp2_s = (TextView) view.findViewById(R.id.temp2_s);
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}
			viewHolder.city_name_s.setText(cityWeather.getCity_name());
			viewHolder.publish_text_s.setText("今天"
					+ cityWeather.getPublish_time() + "发布");
			viewHolder.current_date_s.setText(currentDate);
			viewHolder.weather_desp_s.setText(cityWeather.getWeather_desp());
			viewHolder.temp1_s.setText(cityWeather.getTemp1());
			viewHolder.temp2_s.setText(cityWeather.getTemp2());
			return view;
		}

	}

	class ViewHolder {
		TextView city_name_s;
		TextView publish_text_s;
		TextView current_date_s;
		TextView weather_desp_s;
		TextView temp1_s;
		TextView temp2_s;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		// 初始化各控件
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		setting = (Button) findViewById(R.id.setting);
		addCity = (LinearLayout) findViewById(R.id.add_city);
		String countyCode = getIntent().getStringExtra("county_code");
		isAdd = getIntent().getBooleanExtra("add_city", false);
		noMainCity = getIntent().getBooleanExtra("no_main_city", false);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		currentDate = sdf.format(new Date());
		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代号时就去查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// 没有县级代号时就直接显示本地天气
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		setting.setOnClickListener(this);
		addCity.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.switch_city:
			intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("switch_city", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			refresh();
			break;
		case R.id.setting:
			intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
			break;
		case R.id.add_city:
			intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("add_city", true);
			startActivity(intent);
			finish();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 刷新天气
	 */
	public void refresh() {
		publishText.setText("同步中...");
		String weatherCode = coolWeatherDB.loadMainCityWeather()
				.getWeather_code();
		if (!TextUtils.isEmpty(weatherCode)) {
			queryWeatherInfo(weatherCode, true);
		}
		for ( CityWeather cy : coolWeatherDB.loadMinorCityWeather() ) {
			weatherCode = cy.getWeather_code();
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode, true);
			}
		}
	}

	/**
	 * 查询县级代号所对应的天气代号。
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode", false);
	}

	/**
	 * 查询天气代号所对应的天气。
	 */
	private void queryWeatherInfo(String weatherCode, boolean isRefresh) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode", isRefresh);
	}

	/**
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
	 */
	private void queryFromServer(final String address, final String type, final boolean isRefresh) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(final String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// 从服务器返回的数据中解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode, false);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// 处理服务器返回的天气信息
					switch (Utility.handleWeatherResponse(coolWeatherDB,
							response, isAdd, isRefresh)) {
					case CoolWeatherDB.CITY_ADDED:

						break;
					case CoolWeatherDB.CITY_EXISTS:

						break;
					case CoolWeatherDB.CITY_IS_MAIN:

						break;
					case CoolWeatherDB.NO_WEATHER_DATA:

						break;
					default:
						break;
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				Log.e("###ERROR", e.toString());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}
		});
	}

	/**
	 * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
	 */
	/*
	 * private void showWeather() { SharedPreferences prefs = PreferenceManager
	 * .getDefaultSharedPreferences(this);
	 * cityNameText.setText(prefs.getString("city_name", ""));
	 * temp1Text.setText(prefs.getString("temp1", ""));
	 * temp2Text.setText(prefs.getString("temp2", ""));
	 * weatherDespText.setText(prefs.getString("weather_desp", ""));
	 * publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
	 * currentDateText.setText(prefs.getString("current_date", ""));
	 * weatherInfoLayout.setVisibility(View.VISIBLE);
	 * cityNameText.setVisibility(View.VISIBLE); }
	 */
	/**
	 * 从数据库中读取存储的天气信息，并显示到界面上。
	 */
	private void showWeather() {
		CityWeather mainCityWeather = coolWeatherDB.loadMainCityWeather();
		List<CityWeather> minorCityWeatherList = coolWeatherDB
				.loadMinorCityWeather();
		cityNameText.setText(mainCityWeather.getCity_name());
		temp1Text.setText(mainCityWeather.getTemp1());
		temp2Text.setText(mainCityWeather.getTemp2());
		weatherDespText.setText(mainCityWeather.getWeather_desp());
		publishText.setText("今天" + mainCityWeather.getPublish_time() + "发布");
		currentDateText.setText(currentDate);
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);

		CityWeatherAdapter adapter = new CityWeatherAdapter(
				WeatherActivity.this, R.layout.city_weather_item,
				minorCityWeatherList);
		ListView listView = (ListView) findViewById(R.id.city_weather_list);
		listView.setAdapter(adapter);

	}
};
