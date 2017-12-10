package com.yoyolab.mysearch.Adapters;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoyolab.mysearch.Activities.LocalProductPage;

import com.yoyolab.mysearch.Activities.SearchPage;
import com.yoyolab.mysearch.Activities.WishListPage;
import com.yoyolab.mysearch.Services.GetOnlineImage;
import com.yoyolab.mysearch.Model.Product;
import com.yoyolab.mysearch.R;
import com.yoyolab.mysearch.Repositories.FavoritesRepository;

import butterknife.BindView;
import butterknife.ButterKnife;

import java.util.List;


public class ProductResultsAdapter extends RecyclerView.Adapter<ProductResultsAdapter.ViewHolder> {
    private Context context;
    private List<Product> results;
    private FavoritesRepository favoritesRepository;
    private int layoutMode = 1;
    private AlphaAnimation fadeIn, fadeOut;
    private LruCache<String,Bitmap> imageCache = new LruCache<String, Bitmap>(14440000 * 15) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    public ProductResultsAdapter(List<Product> results, Context context) {
        this.results = results;
        this.context = context;
        this.favoritesRepository = new FavoritesRepository(context);

        if (context instanceof SearchPage) {
            IntentFilter wishListStatusFilterSP = new IntentFilter("SearchPage - There is a change in the wish list");
            LocalBroadcastManager.getInstance(context).registerReceiver(wishListStatusReceiverSP, wishListStatusFilterSP);
        }
        else {
            IntentFilter wishListStatusFilterWLP = new IntentFilter("WishListPage - There is a change in the wish list");
            LocalBroadcastManager.getInstance(context).registerReceiver(wishListStatusReceiverWLP, wishListStatusFilterWLP);
        }
        confWishListStateAnimations();
    }

    private void confWishListStateAnimations() {
        fadeIn = new AlphaAnimation(0.0f , 1.0f );
        fadeIn.setDuration(400);
        fadeIn.setFillAfter(true);

        fadeOut = new AlphaAnimation( 1.0f , 0.0f );
        fadeOut.setDuration(400);
        fadeOut.setFillAfter(true);
    }

    public void setResults(List<Product> results) {
        this.results = results;
    }

    public void setLayoutMode(int mode) {
        layoutMode = mode;
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root;

        if (layoutMode == 1)
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_result_horizontal,parent,false);
        else
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_result_vertical,parent,false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(results.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return layoutMode;
    }

    @Override
    public int getItemCount() {
        return results == null ? 0 : results.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.productIV) ImageView productIV;
        @BindView(R.id.productNameTV) TextView productNameTV;
        @BindView(R.id.productDescTV) TextView productDescTV;
        @BindView(R.id.productPriceTV) TextView productPriceTV;
        @BindView(R.id.priceBackgroundIV) ImageView priceBackgroundIV;
        @BindView(R.id.inWishListView) View inWishListView;

        GetOnlineImage getImageTask;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //removedFromWish = getAdapterPosition();

                    Intent localProdIntent = new Intent(context, LocalProductPage.class);
                    Bundle bundle = new Bundle();

                    bundle.putInt("id",results.get(getAdapterPosition()).id);
                    bundle.putInt("price",results.get(getAdapterPosition()).price);
                    bundle.putString("name",results.get(getAdapterPosition()).name);
                    bundle.putString("description",results.get(getAdapterPosition()).description);
                    bundle.putString("imageUrl",results.get(getAdapterPosition()).imageUrl);
                    bundle.putString("inWishList",results.get(getAdapterPosition()).inWishList);

                    localProdIntent.putExtra("ProductDetails",bundle);
                    context.startActivity(localProdIntent);
                    ((Activity) context).overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    if (results.get(getAdapterPosition()).inWishList.equals("True")) {
                        inWishListView.startAnimation(fadeOut);
                        results.get(getAdapterPosition()).inWishList = "False";

                        favoritesRepository.removeItem(results.get(getAdapterPosition()).id);
                    }
                    else {
                        inWishListView.startAnimation(fadeIn);
                        results.get(getAdapterPosition()).inWishList = "True";

                        favoritesRepository.addItem(results.get(getAdapterPosition()).id);
                    }
                    if (context instanceof WishListPage) {
                        results.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("SearchPage - There is a change in the wish list"));

                        if (results.size() == 0)
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("WishListPage - Wish list is empty"));
                    }
                    return true;
                }
            });
        }

        private void setImage(String imageURL) {
            Bitmap bmp = imageCache.get(imageURL);
            if(bmp != null)
                productIV.setImageBitmap(bmp);

            else {
                if(getImageTask != null) {
                    getImageTask.cancel(true);
                }
                getImageTask = new GetOnlineImage(productIV,imageCache);
                getImageTask.execute(imageURL);
            }
        }

        public void bind(Product result) {
            productNameTV.setText(result.name);
            productDescTV.setText(result.description);
            setImage(result.imageUrl);

            if (result.price == 0)   //Product doesn't have a price
                priceBackgroundIV.setVisibility(View.INVISIBLE);
            else {
                int length = String.valueOf(result.price).length();
                String price = result.price + "$";

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

            if (favoritesRepository.checkIfExists(results.get(getAdapterPosition()).id)) {
                inWishListView.setVisibility(View.VISIBLE);
                results.get(getAdapterPosition()).inWishList = "True";
            }
            else {
                inWishListView.setVisibility(View.INVISIBLE);
                results.get(getAdapterPosition()).inWishList = "False";
            }
        }
    }

    private BroadcastReceiver wishListStatusReceiverSP = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            favoritesRepository = new FavoritesRepository(context);
            notifyDataSetChanged();        }
    };

    private BroadcastReceiver wishListStatusReceiverWLP = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            favoritesRepository = new FavoritesRepository(context);

            for (int i = 0; i < results.size(); i++) {
                if (!favoritesRepository.checkIfExists(results.get(i).id))
                    results.remove(i);
            }
            notifyDataSetChanged();
        }
    };
}