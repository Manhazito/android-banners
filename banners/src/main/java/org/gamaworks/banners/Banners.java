/* Copyright (C) 2015 Lookatitude IT Services & Consulting - All Rights Reserved.
* Unauthorized copying of this file, via any medium is strictly prohibited.
* 
* Proprietary and confidential.
*/

package org.gamaworks.banners;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * [description]
 *
 * @author Filipe Ramos
 * @version 1.0
 */
public class Banners extends View {
    @SuppressWarnings("unused")
    private static final String TAG = Banners.class.getSimpleName();

    private final int FORWARD = -1;
    private final int BACKWARD = 1;
    private final int SEEING_TIME = 5000;

    private LoadBannerListener mLoadBannerListener;
    private GestureDetector mFlingDetector;

    private Rect currentImageRect = new Rect();
    private Rect nextImageRect = new Rect();
    private int mDislocation = 0;
    private int mIndex = 0;
    private int mNextIndex = 0;
    private final Paint mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mIndicatorRadius;
    private float mIndicatorY;
    private float mIndicatorStartX;
    private float mIndicatorXInterval;
    private Timer mChangeImageTimer;
    private Timer mAnimationTimer;
    private boolean mNoRealBanners = true;
    private Bitmap mDefaultBanner;

    private int mIndicatorColor = Color.argb(255, 102, 146, 203);
    private List<Bitmap> mBitmapList = new ArrayList<>();

    public Banners(final Context context, AttributeSet attrs) {
        super(context, attrs);

        mBitmapList.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.banner_default));

        mIndicatorPaint.setStrokeWidth(2f);
        mIndicatorPaint.setColor(mIndicatorColor);

        mFlingDetector = new GestureDetector(context, new FlingDetector());
    }

    private void changeImage(int direction) {
        mDislocation = 0;

        switch (direction) {
            case FORWARD:
                mNextIndex = mIndex + 1;
                if (mNextIndex >= mBitmapList.size()) mNextIndex = 0;

                if (mAnimationTimer != null) mAnimationTimer.cancel();

                mAnimationTimer = new Timer();
                mAnimationTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        mDislocation += getWidth() / 10;

                        if (mDislocation >= getWidth()) mDislocation = 0;

                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recalculateRectangles();
                                invalidate();
                            }
                        });

                        if (mDislocation == 0) {
                            cancel();
                            mIndex++;
                            if (mIndex >= mBitmapList.size()) mIndex = 0;
                        }
                    }

                }, 0, 36);

                break;
            case BACKWARD:
                mNextIndex = mIndex - 1;
                if (mNextIndex < 0) mNextIndex = mBitmapList.size() - 1;

                if (mAnimationTimer != null) mAnimationTimer.cancel();

                mAnimationTimer = new Timer();
                mAnimationTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        mDislocation -= getWidth() / 10;

                        if (mDislocation <= -(getWidth())) mDislocation = 0;

                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recalculateRectangles();
                                invalidate();
                            }
                        });

                        if (mDislocation == 0) {
                            cancel();
                            mIndex--;
                            if (mIndex < 0) mIndex = mBitmapList.size() - 1;
                        }
                    }

                }, 0, 36);

                break;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int numberOfBmp = mBitmapList.size();

        mIndicatorRadius = (height / 50);
        mIndicatorXInterval = 2 * mIndicatorRadius + 2 * mIndicatorRadius;
        mIndicatorY = height - 4f * mIndicatorRadius;
        mIndicatorStartX = (width / 2) - (numberOfBmp - 1) * mIndicatorXInterval / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mBitmapList.get(mIndex) != null) {
            double aspectRatio = ((double) mBitmapList.get(mIndex).getWidth()) / ((double) mBitmapList.get(mIndex).getHeight());
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = widthMode == MeasureSpec.UNSPECIFIED ? mBitmapList.get(mIndex).getWidth() : MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = heightMode == MeasureSpec.UNSPECIFIED ? mBitmapList.get(mIndex).getHeight() : MeasureSpec.getSize(heightMeasureSpec);

            int measuredWidth;
            int measuredHeight;

            if (widthMode == MeasureSpec.EXACTLY) {
                measuredWidth = widthSize;
                if (heightMode == MeasureSpec.EXACTLY) {
                    measuredHeight = heightSize;
                } else {
                    measuredHeight = (int) Math.min(heightSize, (measuredWidth / aspectRatio));
                }
            } else {
                if (heightMode == MeasureSpec.EXACTLY) {
                    measuredHeight = heightSize;
                    measuredWidth = (int) Math.min(widthSize, (measuredHeight * aspectRatio));
                } else {
                    if (widthSize > heightSize * aspectRatio) {
                        measuredHeight = heightSize;
                        measuredWidth = (int) (measuredHeight * aspectRatio);
                    } else {
                        measuredWidth = widthSize;
                        measuredHeight = (int) (measuredWidth / aspectRatio);
                    }
                }
            }

            currentImageRect.set(0, 0, measuredWidth, measuredHeight);
            nextImageRect.set(measuredWidth, 0, 2 * measuredWidth, measuredHeight);
            setMeasuredDimension(measuredWidth, measuredHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        if (mBitmapList != null) {
            int numberOfBmp = mBitmapList.size();
            int width = getWidth() - getPaddingLeft() - getPaddingRight();
            mIndicatorStartX = (width / 2) - (numberOfBmp - 1) * mIndicatorXInterval / 2;
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int numberOfBmp;
        numberOfBmp = mBitmapList.size();

        if (mBitmapList.get(mIndex) != null && mBitmapList.get(mNextIndex) != null) {
            canvas.drawBitmap(mBitmapList.get(mIndex), null, currentImageRect, mIndicatorPaint);
            if (numberOfBmp > 1) canvas.drawBitmap(mBitmapList.get(mNextIndex), null, nextImageRect, mIndicatorPaint);
        }

        if (numberOfBmp > 1) {
            for (int i = 0; i < numberOfBmp; i++) {
                if (i == mIndex) mIndicatorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                else mIndicatorPaint.setStyle(Paint.Style.STROKE);

                canvas.drawCircle(mIndicatorStartX + i * mIndicatorXInterval, mIndicatorY, mIndicatorRadius, mIndicatorPaint);
            }
        }
    }

    private void recalculateRectangles() {
        int startSecond = (mDislocation < 0) ? -getWidth() : getWidth();

        currentImageRect.set(0 - mDislocation, 0, getWidth() - mDislocation, getHeight());
        nextImageRect.set(startSecond - mDislocation, 0, startSecond + getWidth() - mDislocation, getHeight());
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return mFlingDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    class FlingDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mBitmapList.size() > 1) {
                try {
                    if (Math.abs(e1.getY() - e2.getY()) > 250) return false;

                    if (e1.getX() - e2.getX() > 120 && Math.abs(velocityX) > 200) {
                        // Left fling
                        mChangeImageTimer.cancel();
                        changeImage(FORWARD);
                        mChangeImageTimer = new Timer();
                        mChangeImageTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                changeImage(FORWARD);
                            }
                        }, SEEING_TIME, SEEING_TIME);
                    } else if (e2.getX() - e1.getX() > 120 && Math.abs(velocityX) > 200) {
                        // Right fling
                        mChangeImageTimer.cancel();
                        changeImage(BACKWARD);
                        mChangeImageTimer = new Timer();
                        mChangeImageTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                changeImage(FORWARD);
                            }
                        }, SEEING_TIME, SEEING_TIME);
                    }
                } catch (Exception e) {
                    // Do nothing
                }
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mLoadBannerListener != null) mLoadBannerListener.bannerClicked(mIndex);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    /**
     * Sets the color of the banner indicators circles.
     *
     * @param indicatorColor The indicators color.
     */
    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
        invalidate();
    }


    public void setDefaultBanner(Bitmap defaultBanner) {
        if (defaultBanner == null) return;

        this.mDefaultBanner = defaultBanner;
        if (mNoRealBanners) {
            mBitmapList.clear();
            mBitmapList.add(mDefaultBanner);
        }
    }

    public void clearBanners() {
        mBitmapList.clear();
        mBitmapList.add(mDefaultBanner);
        mNoRealBanners = true;
    }

    /**
     * Set the banners images.
     *
     * @param bitmapList The list of bitmaps to use as banners.
     */
    public synchronized void setBitmapList(List<Bitmap> bitmapList) {
        this.mBitmapList = bitmapList;
        mNoRealBanners = false;

        requestLayout();

        if (mBitmapList.size() > 1) {
            if (mChangeImageTimer != null) mChangeImageTimer.cancel();
            mChangeImageTimer = new Timer();
            mChangeImageTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    changeImage(FORWARD);
                }
            }, SEEING_TIME, SEEING_TIME);
        }
    }

    /**
     * Add a image to the banners.
     *
     * @param bitmap The bitmaps to add to banners.
     */
    public synchronized void addBitmap(Bitmap bitmap) {
        if (mNoRealBanners) {
            this.mBitmapList.clear(); // Clear default bitmap!
            mNoRealBanners = false;
        }

        this.mBitmapList.add(bitmap);

        requestLayout();

        if (mBitmapList.size() > 1) {
            if (mChangeImageTimer != null) mChangeImageTimer.cancel();
            mChangeImageTimer = new Timer();
            mChangeImageTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    changeImage(FORWARD);
                }
            }, SEEING_TIME, SEEING_TIME);
        }
    }

    /**
     * Loads an image from the web and adds it to the banners.
     *
     * @param webAddress The WEB address of the image to add to banners.
     */
    public void loadAndAdd(final String webAddress) {
        Handler loadBannerHandler = new Handler();
        loadBannerHandler.post(new Runnable() {
            @Override
            public void run() {
                RetrieveBannerTask retrieveBannerTask = new RetrieveBannerTask();
                retrieveBannerTask.execute(webAddress);
            }
        });
    }

    class RetrieveBannerTask extends AsyncTask<String, Void, Bitmap> {
        String webAddress;
        String errorMsg = "Error decoding image";

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                webAddress = params[0];
                URL url = new URL(webAddress);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (OutOfMemoryError e) {
//                e.printStackTrace();
                errorMsg = "Out of memory";
                return null;
            } catch (MalformedURLException e) {
//                e.printStackTrace();
                errorMsg = "Malformed URL";
                return null;
            } catch (IOException e) {
//                e.printStackTrace();
                errorMsg = "Connection error";
                return null;
            } catch (Exception e) {
//                e.printStackTrace();
                errorMsg = "Unknown error";
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                addBitmap(bitmap);
                if (mLoadBannerListener != null) mLoadBannerListener.loadedSuccessfully(webAddress, bitmap);
            } else {
                if (mLoadBannerListener != null) mLoadBannerListener.loadingError(webAddress, errorMsg);
            }
        }
    }


    public void setLoadBannerListener(LoadBannerListener loadBannerListener) {
        this.mLoadBannerListener = loadBannerListener;
    }

    public interface LoadBannerListener {
        void loadedSuccessfully(String webAddress, Bitmap imageBmp);

        void loadingError(String webAddress, String errorMsg);

        void bannerClicked(int index);
    }
}
