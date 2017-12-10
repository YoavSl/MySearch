package com.yoyolab.mysearch.Services;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;


public class GetImageFromUrl {
    public void get(String url, Context context, ImageView intoView, int placeHolderResId) {
        RequestCreator request = Picasso.with(context).load(Uri.parse(url));
        if(placeHolderResId != 0) {
            request.placeholder(placeHolderResId);
        }
        request.into(intoView);
    }
}
