package com.yoyolab.mysearch;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

public class GetOnlineImage extends AsyncTask<String,Void,Bitmap> {
    private LruCache<String,Bitmap> imageCache;
    private ImageView imageView;
    private int maxImageSize;
    private String imageUrl;

    public GetOnlineImage(ImageView imageView, LruCache<String,Bitmap> imageCache) {
        this.imageView = imageView;
        this.imageCache = imageCache;
        this.maxImageSize = imageView.getMaxHeight();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        this.imageUrl = params[0];
        Bitmap bmp = BitmapLoader.loadBitmapFromURL(imageUrl,maxImageSize);
        return bmp;
    }

    @Override
    protected void onPreExecute() {
        imageView.setImageResource(R.drawable.loading_product_image);
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap != null) {
            imageView.setImageBitmap(bitmap);
            if(imageCache != null) {
                imageCache.put(imageUrl, bitmap);
            }
        }
    }
}
