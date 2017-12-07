package com.yoyolab.mysearch.Services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class BitmapLoader {
    public static Bitmap loadBitmapFromURL(String urlString, int maxSize) {
        Bitmap bmp = null;

        try {
            URL imageURL = new URL(urlString);
            InputStream input = imageURL.openStream();
            BitmapFactory.Options optionsSource = getBitmapBounds(input);
            input.close();

            input = imageURL.openStream();
            bmp = loadScaledBitmap(input,optionsSource,maxSize);
            input.close();

        }
        catch (IOException ex) {
            Log.e("ERROR: Loading Bitmap",ex.getMessage());
            ex.printStackTrace();
        }
        return bmp;
    }

    /**
     * get the bounds and details of a bitmap resource without loading it.
     * @param input a Bitmap {@link InputStream}
     * @return an {@link BitmapFactory.Options} object containing image details
     */
    private static BitmapFactory.Options getBitmapBounds(InputStream input) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input,null,options);
        return options;
    }

    private static Bitmap loadScaledBitmap(InputStream input, BitmapFactory.Options optionsSource, int maxSize) {
        BitmapFactory.Options optionsScaled = new BitmapFactory.Options();
        optionsScaled.inSampleSize = calculateInSampleSize(optionsSource,maxSize);
        return BitmapFactory.decodeStream(input,null,optionsScaled);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int maxSize) {
        int scale = 1;
        if(options.outHeight > maxSize || options.outWidth > maxSize) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(maxSize / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
        }
        return  scale;
    }
}
