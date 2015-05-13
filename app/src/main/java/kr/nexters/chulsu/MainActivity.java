package kr.nexters.chulsu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Window;
import android.widget.Toast;

import butterknife.ButterKnife;
import kr.nexters.chulsu.manager.PrefManager;
import kr.nexters.chulsu.manager.TTSManager;
import kr.nexters.chulsu.widget.RecommendDialogFragment;
import kr.nexters.chulsu.widget.TTSGuideDialogFragment;

public class MainActivity extends FragmentActivity {

	private long tempTime = 0;
	private final static int BACK_PRESS_THRESHOLD = 1200;
	
	public static final int REQ_GOOGLE_PLAY = 100;
	public static final int REQ_ACCESSIBILITY_SETTINGS = REQ_GOOGLE_PLAY + 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		ButterKnife.inject(this);
		
		TTSManager.getInstance();
		
		initialize();
	}

	private void initialize() {
		initView();
		initTelephony();

		showRecommendDialog();
	}

	private void initView() {
		BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.bg);
		drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		findViewById(R.id.root).setBackgroundDrawable(drawable);
	}

	private void initTelephony() {
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(new PhoneStateListener() {

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				super.onCallStateChanged(state, incomingNumber);
				switch(state) {
				case TelephonyManager.CALL_STATE_RINGING:
					TTSManager.getInstance().stop();
					break;
				}
			}

		}, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void showRecommendDialog() {
		boolean beforeShow = PrefManager.getInstance().getBoolean(PrefManager.PREFKEY_RECOMMAND_RATE_DIALOG, false);
		if (!beforeShow) {
			new RecommendDialogFragment().show(getSupportFragmentManager(), "RecommendDialogFragment");
		}
	}

	@Override
	public void onBackPressed() {
		if(tempTime != 0 && System.currentTimeMillis() - tempTime < BACK_PRESS_THRESHOLD) {
			super.onBackPressed();
		} else {
			Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 종료합니다.", Toast.LENGTH_SHORT).show();
			tempTime = System.currentTimeMillis();
			return;
		}
	}
	
	@Override
	protected void onResume() {
		TTSManager.getInstance().resume();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		TTSManager.getInstance().destroy();
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case REQ_GOOGLE_PLAY:
			new TTSGuideDialogFragment().show(getSupportFragmentManager(), "TTSGuideDialogFragment");
			break;
		case REQ_ACCESSIBILITY_SETTINGS:
			TTSManager.getInstance().refresh();
			break;
		}
	}
	
}
