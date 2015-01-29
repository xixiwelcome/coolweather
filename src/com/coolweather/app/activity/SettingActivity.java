package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.coolweather.app.service.AutoUpdateService;
import com.example.coolweather.R;

public class SettingActivity extends Activity implements OnClickListener {

	private CheckBox is_refresh_checkbox;
	private EditText freq_refresh_edit;
	private Button ok;
	private Button cancle;

	private final TextWatcher mTextWatcher = new TextWatcher() {
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void afterTextChanged(Editable s) {
			if (s.length() > 0) {
				try {
					int n = Integer.parseInt(s.toString());
					if (n < 1 || n > 24) {
						if (n < 1) {
							freq_refresh_edit.setText("1");
						} else if (n > 24) {
							freq_refresh_edit.setText("24");
						}
						Toast.makeText(SettingActivity.this,
								R.string.freq_illegal, Toast.LENGTH_SHORT)
								.show();
					}
				} catch (Exception e) {
					freq_refresh_edit.setText("1");
					Toast.makeText(SettingActivity.this, R.string.freq_illegal,
							Toast.LENGTH_SHORT).show();
				}
				freq_refresh_edit.setSelection(freq_refresh_edit.getText().length());
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_layout);
		is_refresh_checkbox = (CheckBox) findViewById(R.id.is_auto_refresh_checkbox);
		freq_refresh_edit = (EditText) findViewById(R.id.freq_refresh_edit);
		ok = (Button) findViewById(R.id.setting_ok);
		cancle = (Button) findViewById(R.id.setting_cancle);
		SharedPreferences preference = PreferenceManager
				.getDefaultSharedPreferences(this);
		int int_cur_freq = preference.getInt("freq_refresh", -1);
		boolean is_auto_refresh = preference.getBoolean("is_auto_refresh",
				false);
		if (int_cur_freq < 0) {
			is_refresh_checkbox.setChecked(false);
			freq_refresh_edit.setText("");
		} else {
			is_refresh_checkbox.setChecked(is_auto_refresh);
			freq_refresh_edit.setText(int_cur_freq+"");
		}
		if (is_auto_refresh == false) {
			freq_refresh_edit.setEnabled(false);
		}
		is_refresh_checkbox.setOnClickListener(this);
		ok.setOnClickListener(this);
		cancle.setOnClickListener(this);
		freq_refresh_edit.addTextChangedListener(mTextWatcher);
	}

	@Override
	public void onClick(View v) {
		String str_freq = freq_refresh_edit.getText().toString();
		switch (v.getId()) {
		case R.id.is_auto_refresh_checkbox:
			if (is_refresh_checkbox.isChecked()) {
				freq_refresh_edit.setEnabled(true);
			} else {
				freq_refresh_edit.setEnabled(false);
			}
			break;
		case R.id.setting_ok:
			Intent intent = new Intent(this, AutoUpdateService.class);
			if (is_refresh_checkbox.isChecked()) {
				if (str_freq.equals("")) {
					Toast.makeText(SettingActivity.this,
							R.string.freq_empty_allert, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				intent.putExtra("freq_refresh",
						Integer.parseInt(str_freq));
				startService(intent);
			} else {
				stopService(intent);
			}
			SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(this).edit();
			editor.putBoolean("is_auto_refresh",
					is_refresh_checkbox.isChecked());
			if (str_freq.equals("")) {
				str_freq = "-1";
			}
			editor.putInt("freq_refresh", Integer.parseInt(str_freq));
			editor.commit();
			finish();
			break;
		case R.id.setting_cancle:
			finish();
			break;
		default:
			break;
		}

	}
}
