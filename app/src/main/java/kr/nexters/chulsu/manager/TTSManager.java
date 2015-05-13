package kr.nexters.chulsu.manager;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import kr.nexters.chulsu.Common;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class TTSManager implements OnInitListener {
	
	private static volatile TTSManager instance;
	private TextToSpeech mTTS;
	
	private boolean isTTSInit = false;
	private List<Locale> mEnableLanguageList;
	private List<String> mLanguageList;
	
	private OnTTSInitializeListener listener;
	
	private final static String SAVE_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.pronunciation/";
	private static String lastRecordFileName = "";
	
	private TTSManager() {
		mTTS = new TextToSpeech(Common.getContext(), this);
		
		File file = new File(SAVE_FILE_PATH);
		if(!file.exists()) {
			file.mkdirs();
		}
	}
	
	public static TTSManager getInstance() {
		if(instance == null) {
			synchronized (TTSManager.class) {
				if(instance == null) {
					instance = new TTSManager();
				}
			}
		}
		return instance;
	}
	
	public List<Locale> getEnableLocale() {
		return mEnableLanguageList;
	}
	
	public List<String> getLanguageList() {
		return mLanguageList;
	}
	
	public String[] getLanguageListToArray() {
		String[] result = new String[mLanguageList.size()];
		
		int i = 0;
		for(String list : mLanguageList) {
			result[i++] = list;
		}
		return result;
	}
	
	public void speak(String text) {
		mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	public boolean isInit() {
		return isTTSInit;
	}
	
	public void setLanguage(Locale loc) {
		mTTS.setLanguage(loc);
	}
	
	public void setLanguage(int selectLoc) {
		mTTS.setLanguage(mEnableLanguageList.get(selectLoc));
	}
	
	public Locale getLocale() {
		return mTTS.getLanguage();
	}
	
	public void shareFile(Activity activity, String text) {
		writeFile(text);
		activity.startActivity(getShareIntent());
	}
	
	public void shareFileForKakao(Activity activity, String text) {
		writeFile(text);
		activity.startActivity(getShareIntent().setPackage("com.kakao.talk"));
	}
	
	private Intent getShareIntent() {
		String path = SAVE_FILE_PATH + lastRecordFileName;
		Uri audioUri = Uri.parse("file://" + path);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("audio/*");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Intent.EXTRA_STREAM, audioUri);
		
		return intent;
	}
	
	private void writeFile(String text) {
		if(mTTS.isSpeaking()) {
			mTTS.stop();
		}		
		System.gc();
		long lastRecordTime = System.currentTimeMillis();
		lastRecordFileName = lastRecordTime + ".wav";
		mTTS.synthesizeToFile(text, new HashMap<String, String>(), SAVE_FILE_PATH + lastRecordFileName);
		deleteWithoutLastRecordFile5();
	}
	
	public void stop() {
		if(mTTS.isSpeaking() && isTTSInit) {
			mTTS.stop();
		}
	}
	
	private void deleteWithoutLastRecordFile5() {
		File file = new File(SAVE_FILE_PATH);
		if(file.isDirectory()) {
			if(file.listFiles().length > 5) {
				File[] list = file.listFiles();
				Arrays.sort(list);
				for(int i = 0; i < list.length - 5; i++) {
					list[i].delete();
				}
			}
		}
	}
	
	public void destroy() {
		if(mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
			mTTS = null;
		}
		if(instance != null) {
			instance = null;
		}
	}
	
	public void resume() {
		String oldEngineInfo = PrefManager.getInstance().getString(PrefManager.PREFKEY_ENGINE_INFO, null);
		String newEngineInfo = mTTS.getDefaultEngine();
		if(oldEngineInfo == null) {
			PrefManager.getInstance().putString(PrefManager.PREFKEY_ENGINE_INFO, newEngineInfo);
		} else if(!oldEngineInfo.equals(newEngineInfo)) {
			PrefManager.getInstance().remove(PrefManager.PREFKEY_LAST_SET_LANGUAGE);
			PrefManager.getInstance().remove(PrefManager.PREFKEY_LOCALE_INFO);

			mTTS = new TextToSpeech(Common.getContext(), this);
		}
	}
	
	@Override
	public void onInit(int status) {
		boolean initSuccess = status == TextToSpeech.SUCCESS;
		if(!initSuccess) {
			return; 
		}
		isTTSInit = true;
		
		loadLocaleInfo();
		initLocaleInfo();
		initEngineInfo();
		initTTSSetting();
		
		if(listener != null) {
			listener.initialized();
		}
	}
	
	private void loadLocaleInfo() {
		mEnableLanguageList = new LinkedList<Locale>();
		mLanguageList = new LinkedList<String>();
		
		String localeInfo = PrefManager.getInstance().getString(PrefManager.PREFKEY_LOCALE_INFO, "");
		
		if(localeInfo.equals("")) {
			Locale[] allLocale = Locale.getAvailableLocales();
			
			for(Locale locale : allLocale) {
				try {
					if(mTTS.isLanguageAvailable(locale) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
						mEnableLanguageList.add(locale);
						mLanguageList.add(locale.getDisplayName());
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			if(mEnableLanguageList.size() == 0) {
				for(Locale locale : allLocale) {
					try {
						if(mTTS.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
							mEnableLanguageList.add(locale);
							mLanguageList.add(locale.getDisplayName());
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			StringBuilder saveInfo = new StringBuilder();
			for(Locale locale : mEnableLanguageList) {
				saveInfo.append(locale.getLanguage() + ":" + locale.getCountry() + "/");
			}
			PrefManager.getInstance().putString(PrefManager.PREFKEY_LOCALE_INFO, saveInfo.toString());
		} else {
			String[] infos = localeInfo.split("[/]");	
			for(String info : infos) {
				String[] split = info.split("[:]");
				
				Locale locale = null;
				if(split.length == 2) {
					locale = new Locale(split[0], split[1]);
					mEnableLanguageList.add(locale);
					mLanguageList.add(locale.getDisplayName());
				}
			}
		}
	}
	
	private void initLocaleInfo() {
		if(isEnableTTS()) {
			int selected = PrefManager.getInstance().getInt(PrefManager.PREFKEY_LAST_SET_LANGUAGE, -1);
			if(selected == -1) {
				if(mEnableLanguageList.contains(new Locale("ko", "KR"))) {
					int index = mEnableLanguageList.indexOf(new Locale("ko", "KR"));
					selected = index;
					PrefManager.getInstance().putInt(PrefManager.PREFKEY_LAST_SET_LANGUAGE, selected);
				} else {
					selected = 0;
				}
			}
			if(mEnableLanguageList.size() <= selected) {
				mTTS.setLanguage(mEnableLanguageList.get(0));
			} else {
				mTTS.setLanguage(mEnableLanguageList.get(selected));
			}
		}
	}
	
	private void initEngineInfo() {
		if(isEnableTTS()) {
			PrefManager.getInstance().putString(PrefManager.PREFKEY_ENGINE_INFO, mTTS.getDefaultEngine());
		}
	}
	
	private void initTTSSetting() {
		mTTS.setPitch(getPitch());
		mTTS.setSpeechRate(getSpeechRate());
	}
	
	public boolean isEnableTTS() {
		if(mEnableLanguageList != null) {
			return mEnableLanguageList.size() != 0;
		}
		return false;
	}
	
	public boolean existsKorean() {
	    return mEnableLanguageList.contains(new Locale("ko", "KR"));
	}
	
	public void refresh() {
		if(instance != null) {
			instance.destroy();
			instance = null;
		}
		
		instance = new TTSManager();
	}
	
	public void setPitch(float pitch) {
		mTTS.setPitch(pitch);
		PrefManager.getInstance().putFloat(PrefManager.PREFKEY_TTS_PITCH, pitch);
	}
	
	public void setSpeechRate(float speechRate) {
		mTTS.setSpeechRate(speechRate);
		PrefManager.getInstance().putFloat(PrefManager.PREFKEY_TTS_SPEECH_RATE, speechRate);
	}
	
	public float getPitch() {
		return PrefManager.getInstance().getFloat(PrefManager.PREFKEY_TTS_PITCH, 1.0f);
	}
	
	public float getSpeechRate() {
		return PrefManager.getInstance().getFloat(PrefManager.PREFKEY_TTS_SPEECH_RATE, 1.0f);
	}
	
	public void setOnTTSInitializeListener(OnTTSInitializeListener l) {
		listener = l;
	}
	
	public interface OnTTSInitializeListener {
		public void initialized();
	}

}
