package com.coolweather.app.service;

import com.coolweather.app.activity.WeatherActivity;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.CityWeather;
import com.coolweather.app.receiver.AutoUpdateReceiver;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class AutoUpdateService extends Service {

	CoolWeatherDB coolWeatherDB;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateWeather();
			}
		}).start();
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = intent.getIntExtra("freq_refresh", -1) * 60 * 60 * 1000; // 这是小时的毫秒数
		if (anHour > 0) {
			long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
			Intent i = new Intent(this, AutoUpdateReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
			manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 更新天气信息。
	 */
	private void updateWeather() {
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		String weatherCode = coolWeatherDB.loadMainCityWeather()
				.getWeather_code();
		if (!TextUtils.isEmpty(weatherCode)) {
			String address = "http://www.weather.com.cn/data/cityinfo/"
					+ weatherCode + ".html";
			HttpUtil.sendHttpRequest(address, new RefreshListener());
		}
		for (CityWeather cy : coolWeatherDB.loadMinorCityWeather()) {
			weatherCode = cy.getWeather_code();
			if (!TextUtils.isEmpty(weatherCode)) {
				String address = "http://www.weather.com.cn/data/cityinfo/"
						+ weatherCode + ".html";
				HttpUtil.sendHttpRequest(address, new RefreshListener());
			}
		}

	}

	class RefreshListener implements HttpCallbackListener {
		@Override
		public void onFinish(String response) {
			Utility.handleWeatherResponse(coolWeatherDB, response, false, true);
		}

		@Override
		public void onError(Exception e) {
			e.printStackTrace();
		}
	};
}
