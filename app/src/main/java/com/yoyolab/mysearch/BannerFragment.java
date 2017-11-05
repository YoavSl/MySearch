package com.yoyolab.mysearch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inneractive.api.ads.sdk.InneractiveAdManager;
import com.inneractive.api.ads.sdk.InneractiveAdView;

public class BannerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup fragmentLayout = (ViewGroup) inflater.inflate(R.layout.banner_fragment, container, false);
        InneractiveAdManager.initialize(getContext());
        InneractiveAdView banner = new InneractiveAdView(getContext(), "Nirit_MobileSchool_Android", InneractiveAdView.AdType.Banner);
        fragmentLayout.addView(banner);
        banner.loadAd();

        return fragmentLayout;
    }
}