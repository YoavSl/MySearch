package com.yoyolab.mysearch.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yoyolab.mysearch.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class OnlineProductPage extends AppCompatActivity {
    @BindView(R.id.productWV) WebView productWV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_product_page);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("prodIdBundle");
        int productId = bundle.getInt("id");

        productWV.setWebViewClient(new WebViewClient());
        productWV.loadUrl("https://www.shopyourway.com/xxx/" + productId);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }
}