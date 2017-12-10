package com.yoyolab.mysearch.Services;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.yoyolab.mysearch.Activities.SearchPage;
import com.yoyolab.mysearch.Model.Product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class SearchForProducts extends AsyncTask<API,Void,List<Product>> {
    private Set<String> queries;
    private String queryType;
    private Context context;
    private List<Product> products;

    public SearchForProducts(Set<String> queries, String queryType, Context context) {
        this.queries = queries;
        this.queryType = queryType;
        this.context = context;

        execute(new API());
    }

    @Override
    protected List<Product> doInBackground(API... params) {
        API api = params[0];
        List<Product> products = null;
        try {
            if (queries.size() == 1)
                products = api.get(queries.iterator().next(), queryType);
            else {   //queries.size > 1 & queryType is "ByProductId"
                products = new ArrayList<>();

                for (String query : queries) {
                    Product product = (api.get(query, queryType)).get(0);   //There is only 1 item in each api array as item ID is singular

                    if (product != null)   //Doing this check cause there is a possibility that the favorite product doesn't exist anymore on the website
                        products.add(product);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return products;
    }

    @Override
    protected void onPostExecute(List<Product> products) {
        this.products = products;

        if (context instanceof SearchPage)
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("SearchPage - Products are ready"));
        else  //instanceOf WishListPage
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("WishListPage - Products are ready"));
    }

    public List<Product> getProducts() {
        return products;
    }
}