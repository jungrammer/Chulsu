package kr.nexters.chulsu;

import android.app.Application;

public class ChulsuApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		Common.setContext(this);
	}
}
