package com.yoyolab.mysearch.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoyolab.mysearch.Services.GetOnlineImage;
import com.yoyolab.mysearch.Model.Product;
import com.yoyolab.mysearch.R;
import com.yoyolab.mysearch.Repositories.FavoritesRepository;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LocalProductPage extends AppCompatActivity {
    Product product;
    GetOnlineImage imageLoadTask;
    FavoritesRepository favoritesRepository;
    private AlphaAnimation fadeIn, fadeOut;

    @BindView(R.id.productIV) ImageView productIV;
    @BindView(R.id.productNameTV) TextView productNameTV;
    @BindView(R.id.productDescTV) TextView productDescTV;
    @BindView(R.id.productPriceTV) TextView productPriceTV;
    @BindView(R.id.priceBackgroundIV) ImageView priceBackgroundIV;
    @BindView(R.id.inWishListIV) ImageView inWishListIV;
    @BindView(R.id.buyBT) FloatingActionButton buyBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_product_page);
        ButterKnife.bind(this);

        favoritesRepository = new FavoritesRepository(getApplicationContext());

        confListeners();
        confWishListStateAnimations();

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("ProductDetails");
        displayProdDetails(bundle);
    }

    private void confListeners() {
        productIV.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (product.inWishList.equals("True")) {
                    inWishListIV.startAnimation(fadeOut);
                    product.inWishList = "False";

                    favoritesRepository.removeItem(product.id);
                }
                else {
                    inWishListIV.startAnimation(fadeIn);
                    product.inWishList = "True";

                    favoritesRepository.addItem(product.id);
                }
                LocalBroadcastManager.getInstance(LocalProductPage.this).sendBroadcast(new Intent("SearchPage - There is a change in the wish list"));
                LocalBroadcastManager.getInstance(LocalProductPage.this).sendBroadcast(new Intent("WishListPage - There is a change in the wish list"));
                return true;
            }
        });

        buyBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent onlineProdIntent = new Intent(LocalProductPage.this,OnlineProductPage.class);
                Bundle bundle = new Bundle();

                bundle.putInt("id",product.id);
                onlineProdIntent.putExtra("prodIdBundle",bundle);
                startActivity(onlineProdIntent);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            }
        });
    }

    private void confWishListStateAnimations() {
        fadeIn = new AlphaAnimation(0.0f , 1.0f );
        fadeIn.setDuration(400);
        fadeIn.setFillAfter(true);

        fadeOut = new AlphaAnimation( 1.0f , 0.0f );
        fadeOut.setDuration(400);
        fadeOut.setFillAfter(true);
    }

    private void displayProdDetails(Bundle productBundle) {
        product = new Product(
                productBundle.getInt("id"),
                productBundle.getInt("price"),
                productBundle.getString("name"),
                productBundle.getString("description"),
                productBundle.getString("imageUrl"),
                productBundle.getString("inWishList")
        );
        productNameTV.setText(product.name);
        productDescTV.setText(product.description);

        if (product.price == 0)   //Product doesn't have a price
            priceBackgroundIV.setVisibility(View.INVISIBLE);
        else {
            int length = String.valueOf(product.price).length();
            String price = product.price + "$";

                /*
                //Center the price position no matter how many digits it has
                if (length == 1){
                    productPriceTV.setX(productPriceTV.getX() + 17);
                    productPriceTV.setY(productPriceTV.getY() + 3);
                }
                else if (length == 3)
                    productPriceTV.setX(productPriceTV.getX() - 10);
                else if (length == 4) {
                    productPriceTV.setX(productPriceTV.getX() - 20);
                    productPriceTV.setY(productPriceTV.getY() - 6);
                } */
            productPriceTV.setText(price);
        }
        imageLoadTask = new GetOnlineImage(productIV,null);
        imageLoadTask.execute(product.imageUrl);

        if (product.inWishList.equals("True"))
            inWishListIV.setVisibility(View.VISIBLE);
        else
            inWishListIV.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }
}
