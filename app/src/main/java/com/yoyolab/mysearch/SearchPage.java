package com.yoyolab.mysearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
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
    @BindView(R.id.loadingPanel) View loadingPanel;
    @BindView(R.id.emptySearchView) View emptySearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_page);

        //Init ButterKnife
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);

        api = new API();

        confResultsList();
        confSearchView();
        confSearchHistory();

        if (savedInstanceState != null) {
            CharSequence savedSearchValue = savedInstanceState.getCharSequence("SearchValue");
            searchSV.setQuery(savedSearchValue, false);

            if (savedInstanceState.getString("ResultsExist").equals("Yes")) {
                lastQuery = savedSearchValue.toString();
                searchForProducts();
            }
        }

        //Display deep link address
        Uri data = this.getIntent().getData();
        if (data != null && data.isHierarchical()) {
            String uri = this.getIntent().getDataString();
            Toast.makeText(SearchPage.this, uri, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("SearchValue", searchSV.getQuery());

        if (resultsAdapter == null)
            outState.putString("ResultsExist", "No");
        else
            outState.putString("ResultsExist", "Yes");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu from xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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
            item.setIcon(R.drawable.change_layout_list);
            resultsRV.setLayoutManager(gridLayoutManager);
            layoutMode++;
        }
        else {
            item.setIcon(R.drawable.change_layout_grid);
            resultsRV.setLayoutManager(listLayoutManager);
            layoutMode = 1;
        }
        resultsAdapter.setLayoutMode(layoutMode);

        return super.onOptionsItemSelected(item);
    }

    private void confResultsList() {
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


        //Add RecyclerView entrance animation
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        recentSearchesRV.setLayoutAnimation(controller);
    }

    private void confSearchView() {
        searchSV.setActivated(true);
        searchSV.setQueryHint("Type your keyword here");
        searchSV.onActionViewExpanded();
        searchSV.setIconified(false);
        searchSV.clearFocus();

        searchSV.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchVS.showNext();
                    emptySearchView.setVisibility(View.GONE);
                    changeLayoutVisible = false;
                    invalidateOptionsMenu();
                }
                else {
                    searchVS.showPrevious();
                    if ((resultsAdapter == null) || (resultsAdapter.getItemCount() == 0))
                        emptySearchView.setVisibility(View.VISIBLE);
                    else {
                        changeLayoutVisible = true;
                        invalidateOptionsMenu();
                    }
                }
            }
        });

        searchSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Hide the soft keyboard
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                lastQuery = query;
                searchForProducts();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recentsAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void confSearchHistory() {
        searchHistory = getSearchHistory();
        recentsAdapter = new RecentSearchesAdapter(new ArrayList<>(searchHistory), this);
        recentSearchesRV.setAdapter(recentsAdapter);
        recentSearchesRV.setLayoutManager(new LinearLayoutManager
                (this, LinearLayoutManager.VERTICAL, false));

        recentSearchesRV.addOnItemTouchListener(new resultsTouchListener(getApplicationContext(), recentSearchesRV,
                new resultsTouchListener.IRecyclerTouchListener() {
                    @Override
                    public void onClickItem(View view, int position) {
                        if (position != 0) {
                            int counter = 0;

                            for (String recentSearch : searchHistory) {
                                if (counter == (position - 1)) {
                                    searchSV.setQuery(recentSearch, false);
                                    lastQuery = recentSearch;
                                    searchForProducts();
                                    break;
                                }
                                counter++;
                            }
                        }
                    }
                }));
    }

    private void searchForProducts() {
        loadingPanel.setVisibility(View.VISIBLE);
        searchResults = new ArrayList<>();
        GetProducts getProducts = new GetProducts();
        getProducts.execute(api);
    }

    private void setSearchResults(List<Product> results) {
        searchResults = results;
        if (resultsAdapter == null) {
            resultsAdapter = new SearchResultsAdapter(results, this);
            resultsRV.setAdapter(resultsAdapter);
        }
        else {
            resultsAdapter.setResults(results);
            resultsAdapter.notifyDataSetChanged();
        }

        if (resultsAdapter.getItemCount() == 0) {
            (Snackbar.make(recentSearchesRV, "No results, please try again", Snackbar.LENGTH_LONG)).show();
            changeLayoutVisible = false;
        }
        else {
            emptySearchView.setVisibility(View.GONE);
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
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> searchHistory = preferences.getStringSet("SearchHistory", this.searchHistory);
        HashSet<String> newSearchHistory = new HashSet<>();
        newSearchHistory.addAll(searchHistory);
        return newSearchHistory;
    }

    @Override
    public void addSearchQueries(Set<String> queries) {
        // We need an Editor object to make preference changes.
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("SearchHistory", searchHistory);

        // Commit the edits
        editor.apply();
    }

    class GetProducts extends AsyncTask<API,Void,List<Product>> {
        @Override
        protected void onPostExecute(List<Product> products) {
            loadingPanel.setVisibility(View.GONE);
            setSearchResults(products);
        }

        @Override
        protected List<Product> doInBackground(API... params) {
            API api = params[0];
            List<Product> products = null;
            try {
                products = api.get(lastQuery);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return products;
        }
    }
}
