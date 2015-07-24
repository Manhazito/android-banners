package org.gamaworks.bannersexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import org.gamaworks.banners.Banners;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Banners banners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        banners = (Banners) findViewById(R.id.banners);
        banners.setIndicatorColor(R.color.blue);

        Handler loadBannersHandler = new Handler();
        loadBannersHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RetrieveBannersTask retrieveBannersTask = new RetrieveBannersTask();
                retrieveBannersTask.execute();
            }
        }, 5000);
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    class RetrieveBannersTask extends AsyncTask<Void, Void, List<Bitmap>> {

        @Override
        protected List<Bitmap> doInBackground(Void... params) {
            String[] urls = new String[]{
                    "http://snag.gy/LSFfb.jpg",
                    "http://snag.gy/yNaCv.jpg",
                    "http://snag.gy/uQawi.jpg"
            };
            List<Bitmap> bannersBmp = new ArrayList<>();

            for (String url : urls) {
                Bitmap bitmap = getBitmapFromURL(url);

                if (bitmap == null) continue;

                bannersBmp.add(bitmap);
            }

            return bannersBmp;
        }

        @Override
        protected void onPostExecute(List<Bitmap> bitmaps) {
            if (bitmaps.size() > 0) banners.setBitmapList(bitmaps);
        }
    }
}
