package kr.nexters.chulsu.manager;

import kr.nexters.chulsu.Common;
import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
	public static final String PREFKEY_LAST_SET_LANGUAGE = "last.set.language";
	public static final String PREFKEY_LOCALE_INFO = "locale.info";
	public static final String PREFKEY_ENGINE_INFO = "tts.engine.info";
	
	public static final String PREFKEY_TTS_PITCH = "tts.pitch.value";
	public static final String PREFKEY_TTS_SPEECH_RATE = "tts.speech.rate.value";
	
	public static final String PREFKEY_EXISTS_KOREAN_LANGAUAGE = "exists.korean.language";
	
	public static final String PREFKEY_RECOMMAND_PREMIUM_DIALOG = "recommand.premium.dialog";
	
	public static final String PREFKEY_RECOMMAND_RATE_DIALOG = "recommand.rate.dialog";
	
	private SharedPreferences pref;
	
	private static volatile PrefManager instance;
	
	private PrefManager() {
		pref = Common.getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
	}
	
	public static PrefManager getInstance() {
		if(null == instance) {
			synchronized (PrefManager.class) {
				if(null == instance) {
					instance = new PrefManager();
				}
			}
		}
		
		return instance;
	}
	
	public int getInt(String key, int defValue) {
		return pref.getInt(key, defValue);
	}
	
	public void putInt(String key, int val) {
		pref.edit().putInt(key, val).commit();
	}
	
	public float getFloat(String key, float defValue) {
		return pref.getFloat(key, defValue);
	}
	
	public void putFloat(String key, float val) {
		pref.edit().putFloat(key, val).commit();
	}
	
	public String getString(String key, String defValue) {
		return pref.getString(key, defValue);
	}
	
	public void putString(String key, String val) {
		pref.edit().putString(key, val).commit();
	}
	
	public boolean getBoolean(String key, boolean defValue) {
		return pref.getBoolean(key, defValue);
	}
	
	public void putBoolean(String key, boolean val) {
		pref.edit().putBoolean(key, val).commit();
	}
	
	public void remove(String key) {
		pref.edit().remove(key).commit();
	}
}
