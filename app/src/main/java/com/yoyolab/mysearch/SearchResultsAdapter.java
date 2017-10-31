package com.yoyolab.mysearch;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;


public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>
{
    private List<Product> results;
    private int layoutMode = 1;
    private LruCache<String,Bitmap> imageCache = new LruCache<String, Bitmap>(14440000 * 15) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    public void setResults(List<Product> results)
    {
        this.results = results;
    }

    public SearchResultsAdapter(List<Product> results)
    {
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
        holder.bind(results.get(position),position);
    }

    @Override
    public int getItemViewType(int position) {
        return layoutMode;
    }

    @Override
    public int getItemCount() {
        return results == null ? 0 : results.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.productIV) ImageView productIV;
        @BindView(R.id.productNameTV) TextView productNameTV;
        @BindView(R.id.productDescTV) TextView productDescTV;
        GetOnlineImage getImageTask;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
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

        public void bind(Product result, int position) {
            productNameTV.setText(result.name);
            productDescTV.setText(result.description);
            setImage(result.imageUrl);
        }
    }
}
