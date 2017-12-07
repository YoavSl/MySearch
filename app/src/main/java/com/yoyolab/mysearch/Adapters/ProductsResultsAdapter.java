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
        import android.util.Log;
        import android.util.LruCache;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
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


public class ProductsResultsAdapter extends RecyclerView.Adapter<ProductsResultsAdapter.ViewHolder> {
    private Context context;
    private List<Product> results;
    private FavoritesRepository favoritesRepository;
    private int layoutMode = 1;
    private int lastClickedItemPosition;
    private LruCache<String,Bitmap> imageCache = new LruCache<String, Bitmap>(14440000 * 15) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    public ProductsResultsAdapter(List<Product> results, Context context) {
        this.results = results;
        this.context = context;
        this.favoritesRepository = new FavoritesRepository(context);

        if (context instanceof SearchPage) {
            IntentFilter inWishListStatusFilter = new IntentFilter("ChangeInWishListState");
            LocalBroadcastManager.getInstance(context).registerReceiver(inWishListStatusReceiver, inWishListStatusFilter);
        }
    }

    public void setResults(List<Product> results) {
        this.results = results;
    }

    public void setLayoutMode(int mode) {
        layoutMode = mode;
        this.notifyDataSetChanged();
    }

    private BroadcastReceiver inWishListStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (results.get(lastClickedItemPosition).inWishList.equals("True"))
                results.get(lastClickedItemPosition).inWishList = "False";
            else
                results.get(lastClickedItemPosition).inWishList = "True";
            notifyItemChanged(lastClickedItemPosition);
        }
    };

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
        @BindView(R.id.wishListBT) Button wishListBT;
        @BindView(R.id.productIV) ImageView productIV;
        @BindView(R.id.productNameTV) TextView productNameTV;
        @BindView(R.id.productDescTV) TextView productDescTV;
        GetOnlineImage getImageTask;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lastClickedItemPosition = getAdapterPosition();

                    Intent localProdIntent = new Intent(context, LocalProductPage.class);
                    Bundle bundle = new Bundle();

                    bundle.putString("Name",results.get(getAdapterPosition()).name);
                    bundle.putString("Description",results.get(getAdapterPosition()).description);
                    bundle.putInt("ID",results.get(getAdapterPosition()).id);
                    bundle.putString("ImageURL",results.get(getAdapterPosition()).imageUrl);
                    bundle.putString("InWishList",results.get(getAdapterPosition()).inWishList);

                    localProdIntent.putExtra("ProductDetails",bundle);
                    context.startActivity(localProdIntent);
                    ((Activity) context).overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                }
            });

            wishListBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (results.get(getAdapterPosition()).inWishList.equals("True")) {
                        view.setBackgroundResource(R.drawable.not_in_wish_list);
                        results.get(getAdapterPosition()).inWishList = "False";

                        favoritesRepository.removeItem(results.get(getAdapterPosition()).id);
                    }
                    else {
                        view.setBackgroundResource(R.drawable.in_wish_list);
                        results.get(getAdapterPosition()).inWishList = "True";

                        favoritesRepository.addItem(results.get(getAdapterPosition()).id);
                    }
                    if (context instanceof WishListPage) {
                        Log.d("myTag", "myCheck1");
                        results.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("ChangeInWishListState"));
                    }
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

            if (results.get(getAdapterPosition()).inWishList == null) {  //Haven't checked yet if exists in the favorites list
                if (favoritesRepository.checkIfExists(results.get(getAdapterPosition()).id)) {
                    wishListBT.setBackgroundResource(R.drawable.in_wish_list);
                    results.get(getAdapterPosition()).inWishList = "True";
                }
                else {
                    wishListBT.setBackgroundResource(R.drawable.not_in_wish_list);
                    results.get(getAdapterPosition()).inWishList = "False";
                }
            }
            else {
                if (results.get(getAdapterPosition()).inWishList.equals("True"))
                    wishListBT.setBackgroundResource(R.drawable.in_wish_list);
                else
                    wishListBT.setBackgroundResource(R.drawable.not_in_wish_list);
            }
        }
    }
}