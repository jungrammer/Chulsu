package kr.nexters.chulsu.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import kr.nexters.chulsu.MainActivity;

/**
 * Created by Junhoi on 2015. 5. 13..
 */
public class TTSGuideDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("TTS 세팅 방법");
        builder.setMessage("음성으로 읽어주기 메뉴에서 구글 TTS로 세팅해 주시기 바랍니다.\n(디바이스 마다 세팅 방법이 상이할 수 있습니다.)");
        builder.setCancelable(false);
        builder.setPositiveButton("설정하러 가기", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                getActivity().startActivityForResult(intent, MainActivity.REQ_ACCESSIBILITY_SETTINGS);
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
