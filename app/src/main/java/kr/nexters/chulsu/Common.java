package kr.nexters.chulsu;

import android.content.Context;

public class Common {
	public static Context context;
	
	public static void setContext(Context _context) {
		context = _context;
	}
	
	public static Context getContext() {
		return context;
	}
}
