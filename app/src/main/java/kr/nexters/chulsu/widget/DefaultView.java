package kr.nexters.chulsu.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import kr.nexters.chulsu.MainActivity;
import kr.nexters.chulsu.R;
import kr.nexters.chulsu.manager.PrefManager;
import kr.nexters.chulsu.manager.TTSManager;
import kr.nexters.chulsu.manager.TTSManager.OnTTSInitializeListener;

public class DefaultView extends LinearLayout {

	@InjectView(R.id.listenerEditText) EditText editText;
	@InjectView(R.id.shareButton) Button shareButton;
	@InjectView(R.id.shareButtonForKakao) Button shareButtonForKakao;
	@InjectView(R.id.cancelButton) View cancelButton;
	@InjectView(R.id.playButton) View mPlayButton;
	@InjectView(R.id.languageSelectButton) TextView mLanguageSelectButton;
	@InjectView(R.id.soundSelectButton) TextView soundSelectButton;

	public DefaultView(Context context) {
		super(context);
		initialize();
	}

	public DefaultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	private void initialize() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.default_view_layout, this);
		ButterKnife.inject(view);

		initOthers();
		initControls();
	}

	private void initControls() {
		Typeface typeFace = Typeface.createFromAsset(getContext().getAssets(), "Gungsuh.ttf");
		editText.setTypeface(typeFace);
		shareButton.setTypeface(typeFace);
		shareButtonForKakao.setTypeface(typeFace);
		mLanguageSelectButton.setTypeface(typeFace);
		soundSelectButton.setTypeface(typeFace);
		
		TTSManager.getInstance().setOnTTSInitializeListener(new OnTTSInitializeListener() {

			@Override
			public void initialized() {
				try {
					mLanguageSelectButton.setText(TTSManager.getInstance().getLocale().getDisplayLanguage());
				} catch (NullPointerException e) {
					mLanguageSelectButton.setText("로딩중");
				}
			}
		});
	}
	
	private void initOthers() {
		setVisibilityShareKakaoButton();
	}
	
	private void setVisibilityShareKakaoButton() {
		try {
			getContext().getPackageManager().getPackageInfo("com.kakao.talk", PackageManager.GET_ACTIVITIES); 
		} catch(NameNotFoundException e) {
			shareButtonForKakao.setVisibility(View.GONE);
		}
	}

	@OnClick(R.id.shareButton)
	void onClickShareButton() {
		if(!checkEditText()) {
			return;
		}
		String text = editText.getText().toString();
		TTSManager.getInstance().shareFile((Activity) getContext(), text);
	}

	@OnClick(R.id.playButton)
	void onPlayButtonClick() {
		if(!checkEditText()) {
			return;
		}
		if(!TTSManager.getInstance().isInit()) {
			Toast.makeText(getContext(), "아직 TTS 기능이 초기화 되지 않았습니다. 잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show(); 
			return; 
		}
		String text = editText.getText().toString();
		
		TTSManager.getInstance().speak(text);
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getWindowToken(), 0);
	}

	@OnClick(R.id.shareButtonForKakao)
	void onShareForKakaoButtonClick() {
		if(!checkEditText()) return;
		String text = editText.getText().toString();
		TTSManager.getInstance().shareFileForKakao((Activity) getContext(), text);
	}
	
	@OnClick(R.id.languageSelectButton)
	void onLanguageSelectButtonClick() {
		if(!TTSManager.getInstance().isInit()) {
			Toast.makeText(getContext(), "아직 TTS 기능이 초기화 되지 않았습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show(); 
			return; 
		}
		if(!checkTTSEnable()) return;
        boolean beforeShowDownloadGoogleTTS = PrefManager.getInstance().getBoolean(PrefManager.PREFKEY_EXISTS_KOREAN_LANGAUAGE, false);
		if(!TTSManager.getInstance().existsKorean() && !beforeShowDownloadGoogleTTS) {
		    showDownloadGoogleTTSDialog();
		} else {
		    showLanguageSelectDialog();
		}
	}

	private void showDownloadGoogleTTSDialog() {
		FragmentActivity activity = (FragmentActivity) getContext();
		DialogFragment dialogFragment = new DownloadGoogleTTSDialogFragment();
		dialogFragment.show(activity.getSupportFragmentManager(), "DownloadGoogleTTSDialogFragment");
	}

	@OnTextChanged(value = R.id.listenerEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
	void onAfterTextChange(Editable editable) {
		if (editable.toString().equals("")) {
			mPlayButton.setVisibility(View.GONE);
			cancelButton.setVisibility(View.GONE);
		} else {
			mPlayButton.setVisibility(View.VISIBLE);
			cancelButton.setVisibility(View.VISIBLE);
		}
	}

	private void showLanguageSelectDialog() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        int selected = PrefManager.getInstance().getInt(PrefManager.PREFKEY_LAST_SET_LANGUAGE, 0);
        builder.setTitle("언어 선택");
        builder.setSingleChoiceItems(TTSManager.getInstance().getLanguageListToArray(), selected, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TTSManager.getInstance().setLanguage(which);     
                mLanguageSelectButton.setText(TTSManager.getInstance().getLocale().getDisplayLanguage());
                PrefManager.getInstance().putInt(PrefManager.PREFKEY_LAST_SET_LANGUAGE, which);
                dialog.dismiss();
            }
        });
        builder.show();
	}
	
	@OnClick(R.id.soundSelectButton)
	void onTTSSettingButtonClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("대신말해줌 설정");
		builder.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, new String[]{"볼륨 조절", "속도 조절", "톤 조절"}), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				switch(arg1) {
				case 0:
					onVolumeSetting();
					break;
				case 1:
					onRateSetting();
					break;
				case 2:
					onPitchSetting();
					break;
				}
				arg0.dismiss();
			}
		});
		builder.show();
	}
	
	private void onVolumeSetting() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final AudioManager manager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		final int maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		
		int selected = (int) (5 * ((float)currentVolume / maxVolume));
		if(selected == 0) {
			selected = 1;
		}
		builder.setTitle("볼륨 조절");
		builder.setSingleChoiceItems(new String[]{"정말 크게", "크게", "중간", "작게", "정말 작게"}, Math.abs(selected - 5), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int newWhich = Math.abs(which - 5);
				float diff = maxVolume / 5.0f;
				int setSound = (int) (newWhich * diff);
				manager.setStreamVolume(AudioManager.STREAM_MUSIC, setSound, AudioManager.FLAG_PLAY_SOUND);
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	private void onRateSetting() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		float currentRate = TTSManager.getInstance().getSpeechRate();
		int selected = -1;
		if(currentRate == 1) {
			selected = 1;
		} else if(currentRate == 0.5) {
			selected = 2;
		} else if(currentRate == 2) {
			selected = 0;
		}
		builder.setTitle("속도 조절");
		builder.setSingleChoiceItems(new String[]{"빠르게", "중간", "느리게"}, selected, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
				case 0:
					TTSManager.getInstance().setSpeechRate(2.0f);
					break;
				case 1:
					TTSManager.getInstance().setSpeechRate(1.0f);
					break;
				case 2:
					TTSManager.getInstance().setSpeechRate(0.5f);
					break;
				}
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	private void onPitchSetting() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		float currentPitch = TTSManager.getInstance().getPitch();
		int selected = -1;
		if(currentPitch == 1) {
			selected = 1;
		} else if(currentPitch == 0.5) {
			selected = 2;
		} else if(currentPitch == 2) {
			selected = 0;
		}
		builder.setTitle("톤 조절");
		builder.setSingleChoiceItems(new String[]{"높게", "중간", "낮게"}, selected, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
				case 0:
					TTSManager.getInstance().setPitch(2.0f);
					break;
				case 1:
					TTSManager.getInstance().setPitch(1.0f);
					break;
				case 2:
					TTSManager.getInstance().setPitch(0.5f);
					break;
				}
				dialog.dismiss();
			}
		});
		builder.show();
	}

	@OnClick(R.id.cancelButton)
	void onCancelButtonClick() {
		editText.setText("");
	}
	
	private boolean checkEditText() {
		if(!checkTTSEnable()) return false;
		if(editText.getText().toString() == null || editText.getText().toString().equals("")) {
			Toast.makeText(getContext(), "문장이 입력되지 않았습니다.", Toast.LENGTH_SHORT).show(); 
			return false; 
		}
		return true;
	}
	
	private boolean checkTTSEnable() {
		if(!TTSManager.getInstance().isEnableTTS()) {
			Toast.makeText(getContext(), "해당 디바이스에 TTS 기능이 설치 되어있지 않습니다. 구글 TTS를 다운 받아 설치해 주시기 바랍니다.", Toast.LENGTH_SHORT).show(); 
			Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts&hl=ko");
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			((Activity) getContext()).startActivityForResult(intent, MainActivity.REQ_GOOGLE_PLAY);
			return false;
		}
		return true;
	}
}
