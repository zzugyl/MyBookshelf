package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;

/**
 * check new versions
 * Created by smartjinyu on 2017/3/4.
 */

public class UpdateCheck {
    private static final String TAG = "UpdateCheck";
    private Context mContext;

    public UpdateCheck(Context context) {
        mContext = context;
        Log.i(TAG,"Update check started");
        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/smartjinyu/MyBookshelf/master/")
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
        Version_API api = mRetrofit.create(Version_API.class);

        Call<VersionUpdateData> call = api.getWebVersion();
        call.enqueue(new Callback<VersionUpdateData>() {
            @Override
            public void onResponse(Call<VersionUpdateData> call, Response<VersionUpdateData> response) {
                if (response.code() == 200 && response.body() != null) {
                    int newVersionCode = response.body().getVersion_code();
                    Log.i(TAG, "Newest Version Code is = " + newVersionCode + ", current code is " + BuildConfig.VERSION_CODE);
                    if (newVersionCode > BuildConfig.VERSION_CODE) {
                        new MaterialDialog(mContext)
                                .title(R.string.new_version_find_dialog_title, null)
                                .message(null, String.format(
                                        mContext.getString(R.string.new_version_find_dialog_content),
                                        response.body().getVersion_name()), null)
                                .positiveButton(R.string.new_version_find_dialog_positive, null, d -> {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("market://details?id=com.smartjinyu.mybookshelf"));
                                    mContext.startActivity(i);
                                    return kotlin.Unit.INSTANCE;
                                })
                                .negativeButton(R.string.new_version_find_dialog_negative, null, d -> {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://smartjinyu.com/android/2017/02/09/mybookshelf.html"));
                                    mContext.startActivity(i);
                                    return kotlin.Unit.INSTANCE;
                                })
                                .neutralButton(android.R.string.cancel, null, d -> {
                                    d.dismiss();
                                    return kotlin.Unit.INSTANCE;
                                })
                                .noAutoDismiss()
                                .show();

                    }
                } else {
                    Log.e(TAG, "Failed. Response code = " + response.code() + ", body = " + response.body());
                }
            }

            @Override
            public void onFailure(Call<VersionUpdateData> call, Throwable t) {
                Log.e(TAG, "Response Failed, " + t.toString());
            }
        });
    }

    private interface Version_API {
        @GET("version_update.xml")
        Call<VersionUpdateData> getWebVersion();

    }


}
