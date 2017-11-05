package com.yoyolab.mysearch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.google.gson.Gson;

public class SearchPage extends AppCompatActivity implements IHistoryRepository {
    private boolean changeLayoutVisible = false;
    private API api;
    private List<Product> searchResults;
    private SearchResultsAdapter resultsAdapter;
    private RecentSearchesAdapter recentsAdapter;
    private int layoutMode = 1;
    private RecyclerView.LayoutManager listLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private Set<String> searchHistory = new HashSet<>();
    private String lastQuery;
    public static final String PREFS_NAME = "MyPrefsFile";

    @BindView(R.id.resultsRV) RecyclerView resultsRV;
    @BindView(R.id.recentSearchesRV) RecyclerView recentSearchesRV;
    @BindView(R.id.searchVS) ViewSwitcher searchVS;
    @BindView(R.id.searchSV) SearchView searchSV;
    @BindView(R.id.resultsView) View resultsView;
    @BindView(R.id.recentSearchesView) View recentSearchesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_page);
        initButterknife();
        api = new API();

        gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.itemsCount));
        listLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        resultsRV.setLayoutManager(listLayoutManager);

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

        searchHistory = getSearchHistory();
        recentsAdapter = new RecentSearchesAdapter(new ArrayList<>(searchHistory));
        recentSearchesRV.setAdapter(recentsAdapter);
        recentSearchesRV.setLayoutManager(new LinearLayoutManager
                (this, LinearLayoutManager.VERTICAL, false));

        searchSV.setActivated(true);
        searchSV.setQueryHint("Type your keyword here");
        searchSV.onActionViewExpanded();
        searchSV.setIconified(false);
        searchSV.clearFocus();

        searchSV.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    searchVS.showNext();
                else
                    searchVS.showPrevious();
            }
        });

        searchSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(SearchPage.this,"Loading results...",Toast.LENGTH_SHORT).show();
                lastQuery = query;
                searchResults = new ArrayList();
                GetProducts getProducts = new GetProducts();
                getProducts.execute(api);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recentsAdapter.getFilter().filter(newText);
                return false;
            }
        });

        recentSearchesRV.addOnItemTouchListener(new resultsTouchListener(getApplicationContext(), recentSearchesRV,
                new resultsTouchListener.IRecyclerTouchListener() {
                    @Override
                    public void onClickItem(View view, int position) {
                        Intent localProdIntent = new Intent(getApplicationContext(),LocalProductPage.class);
                        Bundle bundle = new Bundle();

                        int counter = 0;
                        for (String recentSearch : searchHistory) {
                            if  (counter == position) {
                                //initiateSearch(recentSearch);
                                break;
                            }
                            counter++;
                        }

                        bundle.putString("Name",searchResults.get(position).name);

                    }
                }));
    }

    /*private void initiateSearch(String recentSearch) {
        searchSV.setQuery(recentSearch);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu from xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        if (changeLayoutVisible)
            menu.getItem(0).setVisible(true);
        else
            menu.getItem(0).setVisible(false);

        return true;
    }

    //Handles menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (layoutMode == 1) {
            resultsRV.setLayoutManager(gridLayoutManager);
            layoutMode++;
        }
        else {
            resultsRV.setLayoutManager(listLayoutManager);
            layoutMode = 1;
        }
        resultsAdapter.setLayoutMode(layoutMode);

        return super.onOptionsItemSelected(item);
    }

    private void initButterknife() {
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);
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

        if (resultsAdapter.getItemCount() == 0) {
            Toast.makeText(SearchPage.this, "No results, please try again", Toast.LENGTH_SHORT).show();
            changeLayoutVisible = false;
        }
        else {
            changeLayoutVisible = true;
            searchSV.clearFocus();

            searchHistory.add(lastQuery);
            addSearchQueries(searchHistory);
            recentsAdapter.setRecents(new ArrayList<>(searchHistory));
            recentsAdapter.notifyDataSetChanged();
        }
        invalidateOptionsMenu();  //Now onCreateOptionsMenu() is called again
    }

    @Override
    public Set<String> getSearchHistory() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getStringSet("SearchHistory", searchHistory);
    }

    @Override
    public void addSearchQueries(Set<String> queries) {
        // We need an Editor object to make preference changes.
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("SearchHistory", searchHistory);

        // Commit the edits
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
                products = api.get(lastQuery);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return products;
        }
    }
}
