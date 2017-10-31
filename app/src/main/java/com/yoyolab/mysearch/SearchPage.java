package com.yoyolab.mysearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchPage extends AppCompatActivity implements View.OnClickListener, IHistoryRepository {
    private int page;
    private API api;
    private List<Product> searchResults;
    private SearchResultsAdapter resultsAdapter;
    private int layoutMode = 1;
    private RecyclerView.LayoutManager listLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private Set<String> searchHistory = new HashSet<>();
    public static final String PREFS_NAME = "MyPrefsFile";

    @BindView(R.id.resultsRV) RecyclerView resultsRV;
    @BindView(R.id.searchET) EditText searchET;
    @BindView(R.id.searchBT) Button searchBT;
    @BindView(R.id.changeLayoutBT) Button changeLayoutBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_page);
        initButterknife();
        api = new API();

        gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.itemsCount));
        listLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        resultsRV.setLayoutManager(listLayoutManager);

        searchBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page = Integer.parseInt(searchET.getText().toString());
                if(page != 0) {
                    //Hide the soft keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    Toast.makeText(SearchPage.this,"Loading results...",Toast.LENGTH_SHORT).show();
                    searchResults = new ArrayList();
                    GetProducts getProducts = new GetProducts();
                    getProducts.execute(api);

                    addSearchQuery(searchET.getText().toString());
                }
                else
                    Toast.makeText(SearchPage.this,"Please enter a value greater than 0",Toast.LENGTH_SHORT).show();
            }
        });

        resultsRV.addOnItemTouchListener(new resultsTouchListener(getApplicationContext(), resultsRV,
                new resultsTouchListener.IRecyclerTouchListener() {
                    @Override
                    public void onClickItem(View view, int position) {
                        Intent localProdIntent = new Intent(getApplicationContext(),LocalProductPage.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("Name",searchResults.get(position).name);
                        bundle.putString("Description",searchResults.get(position).description);
                        bundle.putInt("ID",searchResults.get(position).id);
                        bundle.putString("ImageURL",searchResults.get(position).imageUrl);

                        localProdIntent.putExtra("ProductDetails",bundle);
                        startActivity(localProdIntent);
                        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                    }
                }));

        changeLayoutBT.setOnClickListener(this);

        searchHistory = getSearchHistory();
        Log.d("myTag", "FirstVal: " + searchHistory.toString());
    }

    private void initButterknife() {
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);
    }

    //Change layout mode
    @Override
    public void onClick(View v) {
        if (layoutMode == 1) {
            resultsRV.setLayoutManager(gridLayoutManager);
            layoutMode++;
        }
        else {
            resultsRV.setLayoutManager(listLayoutManager);
            layoutMode = 1;
        }
        resultsAdapter.setLayoutMode(layoutMode);
    }

    private void setSearchResults(List<Product> results) {
        searchResults = results;
        if (resultsAdapter == null) {
            resultsAdapter = new SearchResultsAdapter(results);
            resultsRV.setAdapter(resultsAdapter);
        }
        else {
            resultsAdapter.setResults(results);
            resultsAdapter.notifyDataSetChanged();
        }

        if (resultsAdapter.getItemCount() == 0)
            Toast.makeText(SearchPage.this,"Page doesn't exist, Try to enter a lower value",Toast.LENGTH_SHORT).show();
    }

    @Override
    public Set<String> getSearchHistory() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getStringSet("SearchHistory", searchHistory);
    }

    @Override
    public void addSearchQuery(String query) {
        // We need an Editor object to make preference changes.
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("SearchHistory", searchHistory);

        // Commit the edits!
        editor.commit();
    }

    class GetProducts extends AsyncTask<API,Void,List<Product>> {
        @Override
        protected void onPostExecute(List<Product> products) {
            setSearchResults(products);
        }

        @Override
        protected List<Product> doInBackground(API... params) {
            API api = params[0];
            List products = null;
            try {
                products = api.get(page);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return products;
        }
    }
}
