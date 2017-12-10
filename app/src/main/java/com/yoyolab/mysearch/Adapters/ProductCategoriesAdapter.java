package com.yoyolab.mysearch.Adapters;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoyolab.mysearch.Activities.SearchPage;
import com.yoyolab.mysearch.R;
import com.yoyolab.mysearch.Services.SearchForProducts;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ProductCategoriesAdapter extends RecyclerView.Adapter<ProductCategoriesAdapter.ViewHolder> {
    private String[] categories;
    private SearchPage context;
    private SearchForProducts searchForProducts;
    private int layoutMode = 1;

    public ProductCategoriesAdapter(String[] categories, SearchPage context) {
        this.categories = categories;
        this.context = context;
    }

    public SearchForProducts getSearchForProductsInstance() {
        return searchForProducts;
    }

    public void setLayoutMode(int mode) {
        layoutMode = mode;
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root;

        if (layoutMode == 1)
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.products_category_horizontal,parent,false);
        else
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.products_category_vertical,parent,false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(categories[position]);
    }

    @Override
    public int getItemViewType(int position) {
        return layoutMode;
    }

    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.categoryIV) ImageView categoryIV;
        @BindView(R.id.categoryNameTV) TextView categoryNameTV;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            context.loadingPanel.setVisibility(View.VISIBLE);
                        }
                    });

                   String categoryName = categoryNameTV.getText().toString();
                   String categoryId;

                   switch(categoryName) {
                       case "Clothing":
                           categoryId = "76532600";
                           break;
                       case "Shoes":
                           categoryId = "93968101";
                           break;
                       case "Electronics":
                           categoryId = "4000236";
                           break;
                       case "Appliances":
                           categoryId = "1075400";
                           break;
                       case "Tools":
                           categoryId = "1075450";
                           break;
                       case "Jewelry":
                           categoryId = "1075425";
                           break;
                       case "Beauty":
                           categoryId = "1075404";
                           break;
                       default:   //case "Groceries"
                           categoryId = "1075415";
                           break;
                   }
                    searchForProducts = new SearchForProducts(new HashSet<>(Arrays.asList(categoryId)), "ByCategoryID", context);
                }
            });
        }

        public void bind(String category) {
            String drawableCategoryName;

            categoryNameTV.setText(category);

            Field[] drawables = R.drawable.class.getFields();
            for (Field f : drawables) {
                drawableCategoryName = f.getName();

                if ((drawableCategoryName.length() > 9) && ((drawableCategoryName.subSequence(0, 9)).equals("category_"))) {
                    if ((category.toLowerCase()).equals(drawableCategoryName.substring(9))) {
                        Resources resources = context.getResources();
                        int imageResourceId = resources.getIdentifier(drawableCategoryName, "drawable", context.getPackageName());
                        Drawable image = resources.getDrawable(imageResourceId);
                        categoryIV.setImageDrawable(image);
                        break;
                    }
                }
            }
        }
    }
}