package com.yoyolab.mysearch.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

    @BindView(R.id.wishListBT) Button wishListBT;
    @BindView(R.id.itemIV) ImageView itemIV;
    @BindView(R.id.itemNameTV) TextView itemNameTV;
    @BindView(R.id.itemDescTV) TextView itemDescTV;
    @BindView(R.id.buyBT) FloatingActionButton buyBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_product_page);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("ProductDetails");
        displayProdDetails(bundle);

        favoritesRepository = new FavoritesRepository(getApplicationContext());

        wishListBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (product.inWishList.equals("True")) {
                    wishListBT.setBackgroundResource(R.drawable.not_in_wish_list);
                    product.inWishList = "False";

                    favoritesRepository.removeItem(product.id);
                }
                else {
                    wishListBT.setBackgroundResource(R.drawable.in_wish_list);
                    product.inWishList = "True";

                    favoritesRepository.addItem(product.id);
                }
                LocalBroadcastManager.getInstance(LocalProductPage.this).sendBroadcast(new Intent("ChangeInWishListState"));
            }
        });

        buyBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent onlineProdIntent = new Intent(LocalProductPage.this,OnlineProductPage.class);
                Bundle bundle = new Bundle();

                bundle.putInt("ID",product.id);
                onlineProdIntent.putExtra("ProdIDBundle",bundle);
                startActivity(onlineProdIntent);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            }
        });
    }

    private void displayProdDetails(Bundle productBundle) {
        product = new Product(productBundle.getString("Name"),
                productBundle.getString("Description"),
                productBundle.getInt("ID"),
                productBundle.getString("ImageURL"),
                productBundle.getString("InWishList")
        );

        itemNameTV.setText(product.name);
        itemDescTV.setText(product.description);

        imageLoadTask = new GetOnlineImage(itemIV,null);
        imageLoadTask.execute(product.imageUrl);

        if (product.inWishList.equals("True"))
            wishListBT.setBackgroundResource(R.drawable.in_wish_list);
        else
            wishListBT.setBackgroundResource(R.drawable.not_in_wish_list);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }
}
