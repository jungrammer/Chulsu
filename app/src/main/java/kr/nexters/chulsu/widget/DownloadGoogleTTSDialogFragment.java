package kr.nexters.chulsu.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import kr.nexters.chulsu.MainActivity;
import kr.nexters.chulsu.manager.PrefManager;

/**
 * Created by Junhoi on 2015. 5. 13..
 */
public class DownloadGoogleTTSDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("해당 디바이스의 TTS 엔진에는 한국어가 포함되어 있지 않습니다.\n한국어를 사용하시려면 구글 TTS를 다운받아 설치해 주시기 바랍니다.");
        builder.setPositiveButton("다운받기", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts&hl=ko");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                getActivity().startActivityForResult(intent, MainActivity.REQ_GOOGLE_PLAY);
                PrefManager.getInstance().putBoolean(PrefManager.PREFKEY_EXISTS_KOREAN_LANGAUAGE, true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("다시보지않기", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrefManager.getInstance().putBoolean(PrefManager.PREFKEY_EXISTS_KOREAN_LANGAUAGE, true);
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
