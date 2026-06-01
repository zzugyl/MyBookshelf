package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * used to fetch books from Google Books API
 * https://developers.google.com/books/docs/v1/reference/volumes
 */

public class GoogleBooksFetcher extends BookFetcher {
    private static final String TAG = "GoogleBooksFetcher";

    @Override
    public void getBookInfo(final Context context, final String isbn, final int mode) {
        mContext = context;
        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/books/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GB_API api = mRetrofit.create(GB_API.class);
        Call<GoogleBooksJson> call = api.getGBResult("isbn:" + isbn, BuildConfig.googleBooksApiKey);
        call.enqueue(new Callback<GoogleBooksJson>() {
            @Override
            public void onResponse(Call<GoogleBooksJson> call, Response<GoogleBooksJson> response) {
                if (response.body() != null && response.body().getItems() != null
                        && !response.body().getItems().isEmpty()) {
                    Log.i(TAG, "response code = " + response.code());
                    GoogleBooksJson.VolumeInfoBean info = response.body().getItems().get(0).getVolumeInfo();
                    if (info != null && info.getTitle() != null) {
                        Log.i(TAG, "GET Google Books information successfully, title = " + info.getTitle());
                        mBook = new Book();
                        mBook.setIsbn(isbn);
                        mBook.setTitle(info.getTitle());
                        List<String> authors = new ArrayList<>();
                        if (info.getAuthors() != null) {
                            for (String author : info.getAuthors()) {
                                authors.add(author);
                            }
                        }
                        mBook.setAuthors(authors);
                        if (info.getPublisher() != null) {
                            mBook.setPublisher(info.getPublisher());
                        } else {
                            mBook.setPublisher("");
                        }
                        String rawDate = info.getPublishedDate();
                        int pubYear = 9999;
                        int pubMonth = 0;
                        if (rawDate != null && rawDate.length() >= 4) {
                            try {
                                pubYear = Integer.parseInt(rawDate.substring(0, 4));
                            } catch (NumberFormatException e) {
                                pubYear = 9999;
                            }
                            if (rawDate.length() >= 7) {
                                try {
                                    pubMonth = Integer.parseInt(rawDate.substring(5, 7)) - 1;
                                } catch (NumberFormatException e) {
                                    pubMonth = 0;
                                }
                            }
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(pubYear, pubMonth, 1);
                        mBook.setPubTime(calendar);
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                        boolean addWebsite = pref.getBoolean("settings_pref_acwebsite", true);
                        if (addWebsite && info.getInfoLink() != null) {
                            mBook.setWebsite(info.getInfoLink());
                        }
                        final String imageURL;
                        if (info.getImageLinks() != null && info.getImageLinks().getThumbnail() != null) {
                            // Google Books returns http, upgrade to https
                            imageURL = info.getImageLinks().getThumbnail().replace("http://", "https://");
                        } else {
                            imageURL = null;
                        }
                        if (mode == 0) {
                            ((SingleAddActivity) mContext).fetchSucceed(mBook, imageURL);
                        } else if (mode == 1) {
                            ((BatchAddActivity) mContext).fetchSucceed(mBook, imageURL);
                        }
                    } else {
                        Log.e(TAG, "Null volumeInfo, code = " + response.code() + ", isbn = " + isbn);
                        notifyFailed(mode, 0, isbn);
                    }
                } else {
                    Log.e(TAG, "Null Response Body or empty items, code = " + response.code() + ", isbn = " + isbn);
                    notifyFailed(mode, 0, isbn);
                }
            }

            @Override
            public void onFailure(Call<GoogleBooksJson> call, Throwable t) {
                Log.w(TAG, "GET Google Books information failed, " + t.toString());
                notifyFailed(mode, 1, isbn);
            }
        });
    }

    private void notifyFailed(int mode, int event, String isbn) {
        if (mode == 0) {
            ((SingleAddActivity) mContext).fetchFailed(BookFetcher.fetcherID_GB, event, isbn);
        } else if (mode == 1) {
            ((BatchAddActivity) mContext).fetchFailed(BookFetcher.fetcherID_GB, event, isbn);
        }
    }

    private interface GB_API {
        @GET("volumes")
        Call<GoogleBooksJson> getGBResult(@Query("q") String query, @Query("key") String apiKey);
    }
}
