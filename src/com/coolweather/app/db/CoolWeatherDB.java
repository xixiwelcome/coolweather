package com.coolweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CityWeather;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "cool_weather";
	public static final int CITY_ADDED = 0;
	public static final int CITY_EXISTS = 1;
	public static final int CITY_IS_MAIN = 2;
	public static final int NO_WEATHER_DATA = -1;
	
	public static final int REFRESHED = 100;
	/**
	 * 数据库版本
	 */
	public static final int VERSION = 2;
	private static final int SHOW_ORDER_MAIN = 0;
	private volatile static CoolWeatherDB coolWeatherDB;
	private SQLiteDatabase db;

	/**
	 * 将构造方法私有化
	 */
	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	/*
	 * 获取CoolWeatherDB的实例。
	 */
	public static CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			synchronized (CoolWeatherDB.class) {
				if (coolWeatherDB == null) {
					coolWeatherDB = new CoolWeatherDB(context);
				}
			}
		}
		return coolWeatherDB;
	}

	/**
	 * 将Province实例存储到数据库。
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}

	/**
	 * 从数据库读取全国所有的省份信息。
	 */
	public List<Province> loadProvinces() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db
				.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor
						.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor
						.getColumnIndex("province_code")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		return list;
	}

	/**
	 * 将City实例存储到数据库。
	 */
	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}

	/**
	 * 从数据库读取某省下所有的城市信息。
	 */
	public List<City> loadCities(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?",
				new String[] { String.valueOf(provinceId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor
						.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor
						.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			} while (cursor.moveToNext());
		}
		return list;
	}

	/**
	 * 将County实例存储到数据库。
	 */
	public void saveCounty(County county) {
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
		}
	}

	/**
	 * 从数据库读取某城市下所有的县信息。
	 */
	public List<County> loadCounties(int cityId) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id = ?",
				new String[] { String.valueOf(cityId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor
						.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor
						.getColumnIndex("county_code")));
				county.setCityId(cityId);
				list.add(county);
			} while (cursor.moveToNext());
		}
		return list;
	}
	
	/**
	 * 存储城市天气数据
	 */
	public int saveCityWeather(CityWeather cityWeather, boolean isAdd, boolean isRefresh) {
		if (cityWeather != null) {
			int state = CITY_ADDED;
			int max_show_order = 0;
			Cursor cursor = db.rawQuery("select max(show_order) as max_show_order "
					+ "from CityWeather", null);
			if (cursor.moveToFirst()) {
				max_show_order = cursor.getInt(cursor.getColumnIndex("max_show_order"));
			}			 
			ContentValues values = new ContentValues();
			values.put("city_name", cityWeather.getCity_name());
			values.put("publish_time", cityWeather.getPublish_time());
			values.put("temp1", cityWeather.getTemp1());
			values.put("temp2", cityWeather.getTemp2());
			values.put("weather_code", cityWeather.getWeather_code());
			values.put("weather_desp", cityWeather.getWeather_desp());
			
			if (isRefresh) {
				db.update("CityWeather", values, "weather_code=?", 
			    		new String[] {cityWeather.getWeather_code()});
				return REFRESHED;
			}
			if (!isAdd) {
				if (db.delete("CityWeather", "weather_code=?", 
						new String[] {cityWeather.getWeather_code()}) > 0) {
					state = CITY_EXISTS;
				}				 
				values.put("show_order", SHOW_ORDER_MAIN);
				//将旧的主城市变为辅助城市
				ContentValues values_old_main_city = new ContentValues();
				values_old_main_city.put("show_order", ++max_show_order);
			    db.update("CityWeather", values_old_main_city, "show_order=?", 
			    		new String[] {SHOW_ORDER_MAIN+""});				
			} else {
				cursor = db.query("CityWeather", null, "show_order=?",
						new String[] {SHOW_ORDER_MAIN+""}, null, null, null);
				if (cursor.moveToFirst() && cityWeather.getWeather_code().equals(
						cursor.getString(cursor.getColumnIndex("weather_code")))) {
					return CITY_IS_MAIN;
				}
				if (db.delete("CityWeather", "weather_code=?", 
						new String[] {cityWeather.getWeather_code()}) > 0) {
					state = CITY_EXISTS;
				}
				values.put("show_order", ++max_show_order);
			}
			db.insert("CityWeather", null, values);
			return state;
		}
		return NO_WEATHER_DATA;
	}
	
	/**
	 * 获取主要城市天气数据
	 */
	public CityWeather loadMainCityWeather() {
		CityWeather cityWeather = new CityWeather();
		Cursor cursor = db.query("CityWeather", null, "show_order=?",
				new String[] {SHOW_ORDER_MAIN+""}, null, null, null);
		if (cursor.moveToFirst()) {
			cityWeather.setId(cursor.getInt(cursor.getColumnIndex("id")));
			cityWeather.setCity_name(cursor.getString(cursor.getColumnIndex("city_name")));
			cityWeather.setPublish_time(cursor.getString(cursor.getColumnIndex("publish_time")));
			cityWeather.setTemp1(cursor.getString(cursor.getColumnIndex("temp1")));
			cityWeather.setTemp2(cursor.getString(cursor.getColumnIndex("temp2")));
			cityWeather.setWeather_desp(cursor.getString(cursor.getColumnIndex("weather_desp")));
			cityWeather.setWeather_code(cursor.getString(cursor.getColumnIndex("weather_code")));
			cityWeather.setShow_order(cursor.getInt(cursor.getColumnIndex("show_order")));
		}
		return cityWeather;
	}
	
	/**
	 * 获取辅助城市天气数据
	 */
	public List<CityWeather> loadMinorCityWeather() {
		List<CityWeather> cityWeatherList = new ArrayList<CityWeather>();
		Cursor cursor = db.query("CityWeather", null, "show_order>?",
				new String[] {SHOW_ORDER_MAIN+""}, null, null, "show_order desc");
		if (cursor.moveToFirst()) {
			do {
				CityWeather cityWeather = new CityWeather();
				cityWeather.setId(cursor.getInt(cursor.getColumnIndex("id")));
				cityWeather.setCity_name(cursor.getString(cursor.getColumnIndex("city_name")));
				cityWeather.setPublish_time(cursor.getString(cursor.getColumnIndex("publish_time")));
				cityWeather.setTemp1(cursor.getString(cursor.getColumnIndex("temp1")));
				cityWeather.setTemp2(cursor.getString(cursor.getColumnIndex("temp2")));
				cityWeather.setWeather_desp(cursor.getString(cursor.getColumnIndex("weather_desp")));
				cityWeather.setWeather_code(cursor.getString(cursor.getColumnIndex("weather_code")));
				cityWeather.setShow_order(cursor.getInt(cursor.getColumnIndex("show_order")));
				cityWeatherList.add(cityWeather);
			} while (cursor.moveToNext());

		}
		return cityWeatherList;
	}
	
}