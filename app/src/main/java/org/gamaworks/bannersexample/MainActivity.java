package org.gamaworks.bannersexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.gamaworks.banners.Banners;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Banners.LoadBannerListener {
    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Banners banners = (Banners) findViewById(R.id.banners);
        banners.setIndicatorColor(R.color.blue);
        banners.setLoadBannerListener(this);
        banners.setDefaultBanner(BitmapFactory.decodeResource(getResources(), R.drawable.mobile_warrior));

        List<String> urls = new ArrayList<>();
        urls.add("http://www.nolanfans.com/wordpress/wp-content/uploads/2010/06/inception_new_banner3_large.jpg");
        urls.add("http://www.blackfilm.com/read/wp-content/uploads/2011/04/Green-Lantern-big-banner-2.jpg");
        urls.add("http://tweeting.com/wp-content/uploads/2012/01/Star-Wars-Facebook-Picture.jpg");
//        urls.add("http://www.mobilab.unina.it/workshop_dd4lcci/EDCC-2010-banner-big.jpg"); // Too big...
        urls.add("http://snag.gy/LSFfb.jpg");
        urls.add("http://snag.gy/yNaCv.jpg");
        urls.add("http://snag.gy/uQawi.jpg");

        for (String url : urls) banners.loadAndAdd(url);
    }

    @Override
    public void loadedSuccessfully(String webAddress, Bitmap imageBmp) {
        Log.d(TAG, "Loaded banner " + webAddress);
    }

    @Override
    public void loadingError(String webAddress, String errorMsg) {
        Log.e(TAG, "Could not loaded banner " + webAddress + ": " + errorMsg);
    }

    @Override
    public void bannerClicked(int index) {
        Log.d(TAG, "Clicked banner " + index);
    }

}
