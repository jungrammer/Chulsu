package kr.nexters.chulsu.widget;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import kr.nexters.chulsu.manager.PrefManager;

/**
 * Created by Junhoi on 2015. 5. 13..
 */
public class RecommendDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("별점 주러 가기");
        builder.setMessage("별점과 리뷰를 남겨 주시면 개발자에게 큰 활력이 됩니다.");
        builder.setCancelable(false);
        builder.setPositiveButton("별점 주러 가기", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrefManager.getInstance().putBoolean(PrefManager.PREFKEY_RECOMMAND_RATE_DIALOG, true);
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=kr.nexters.chulsu&hl");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                getActivity().startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("다시 보지 않기", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrefManager.getInstance().putBoolean(PrefManager.PREFKEY_RECOMMAND_RATE_DIALOG, true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
