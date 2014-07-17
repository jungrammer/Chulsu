package kr.nexters.chulsu;

import kr.nexters.chulsu.manager.PrefManager;
import kr.nexters.chulsu.manager.TTSManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class MainActivity extends Activity {

	private View mDefaultView;
	private View adView;
	private long tempTime = 0;
	private final static int BACK_PRESS_THRESHOLD = 1200;
	
	public static final int REQ_GOOGLE_PLAY = 100;
	public static final int REQ_ACCESSIBILITY_SETTINGS = REQ_GOOGLE_PLAY + 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		TTSManager.getInstance();
		
		initialize();
	}

	private void initialize() {
		initView();
		initTelephony();
		showReqRecommandDialog();
	}
	
	private void initView() {
		BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.bg);
		drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		findViewById(R.id.root).setBackgroundDrawable(drawable);
		mDefaultView = findViewById(R.id.defaultView);
		adView = findViewById(R.id.xmladview);
		
		if(Common.IS_PREMIUM) {
			adView.setVisibility(View.GONE);
			LayoutParams params = (LayoutParams) mDefaultView.getLayoutParams();
			params.bottomMargin = 0;
			mDefaultView.setLayoutParams(params);
		}
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
	
	private void showReqRecommandDialog() {
		boolean beforeShow = PrefManager.getInstance().getBoolean(PrefManager.PREFKEY_RECOMMAND_RATE_DIALOG, false);
		if(!beforeShow) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("별점 주러 가기");
			builder.setMessage("별점과 리뷰를 남겨 주시면 개발자에게 큰 활력이 됩니다.");
			builder.setCancelable(false);
			builder.setPositiveButton("별점 주러 가기", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PrefManager.getInstance().putBoolean(PrefManager.PREFKEY_RECOMMAND_RATE_DIALOG, true);
					Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=kr.nexters.chulsu&hl");
	                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	                MainActivity.this.startActivity(intent);
					dialog.dismiss();
				}
			});
			builder.setNeutralButton("다시 보지 않기", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PrefManager.getInstance().putBoolean(PrefManager.PREFKEY_RECOMMAND_RATE_DIALOG, true);
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("닫기", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
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
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("TTS 세팅 방법");
			builder.setMessage("음성으로 읽어주기 메뉴에서 구글 TTS로 세팅해 주시기 바랍니다.\n(디바이스 마다 세팅 방법이 상이할 수 있습니다.)");
			builder.setCancelable(false);
			builder.setPositiveButton("설정하러 가기", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
					startActivityForResult(intent, REQ_ACCESSIBILITY_SETTINGS);
					dialog.dismiss();
				}
			});
			builder.create().show();
			break;
		case REQ_ACCESSIBILITY_SETTINGS:
			TTSManager.getInstance().refresh();
			break;
		}
	}
	
}
