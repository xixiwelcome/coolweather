package com.coolweather.app.model;


public class CityWeather {
	private int id;
	private String city_name;
	private String temp1;
	private String temp2;
	private String weather_code;
	private String weather_desp;
	private String publish_time;
	private int show_order;
	
	public CityWeather(String city_name, String temp1, String temp2, String weather_desp, 
			String weather_code, String publish_time) {
		this.setCity_name(city_name);
		this.setTemp1(temp1);
		this.setTemp2(temp2);
		this.setWeather_desp(weather_desp);
		this.setWeather_code(weather_code);
		this.setPublish_time(publish_time);
	}
	
	public CityWeather() {
		this.id = -1;
	}

	public String getCity_name() {
		return city_name;
	}
	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}
	public String getTemp1() {
		return temp1;
	}
	public void setTemp1(String temp1) {
		this.temp1 = temp1;
	}
	public String getTemp2() {
		return temp2;
	}
	public void setTemp2(String temp2) {
		this.temp2 = temp2;
	}
	public String getWeather_desp() {
		return weather_desp;
	}
	public void setWeather_desp(String weather_desp) {
		this.weather_desp = weather_desp;
	}
	public String getPublish_time() {
		return publish_time;
	}
	public void setPublish_time(String publish_time) {
		this.publish_time = publish_time;
	}
	public int getShow_order() {
		return show_order;
	}
	public void setShow_order(int show_order) {
		this.show_order = show_order;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getWeather_code() {
		return weather_code;
	}

	public void setWeather_code(String weather_code) {
		this.weather_code = weather_code;
	}


}
