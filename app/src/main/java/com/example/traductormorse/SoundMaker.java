package com.example.traductormorse;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;


public class SoundMaker {

    private Context context;
    private AsyncPlayer asyncPlayer;
    private Uri uriDot, uriDash;


    public SoundMaker(Context context) {
        this.context = context;
        this.asyncPlayer = new AsyncPlayer("Sound");
        this.uriDot = getUriToResource(context, R.raw.dot);
        this.uriDash = getUriToResource(context, R.raw.dash);
    }


    public void playDot() {
        asyncPlayer.play(context, uriDot, false, AudioManager.STREAM_MUSIC);
    }

    public void playDash() {
        asyncPlayer.play(context, uriDash, false, AudioManager.STREAM_MUSIC);
    }

    // Obtenido de https://stackoverflow.com/questions/45722227/how-to-get-an-uri-from-a-raw-resource
    private Uri getUriToResource(@NonNull Context context, @AnyRes int resId) throws Resources.NotFoundException {
        Resources res = context.getResources();

        Uri resUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId));
        return resUri;
    }


}
