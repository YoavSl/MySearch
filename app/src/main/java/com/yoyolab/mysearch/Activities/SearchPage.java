package com.yoyolab.mysearch.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.yoyolab.mysearch.Adapters.ProductResultsAdapter;
import com.yoyolab.mysearch.Services.SearchForProducts;
import com.yoyolab.mysearch.Adapters.ProductCategoriesAdapter;
import com.yoyolab.mysearch.Repositories.IHistoryRepository;
import com.yoyolab.mysearch.Model.Product;
import com.yoyolab.mysearch.R;
import com.yoyolab.mysearch.Adapters.RecentSearchesAdapter;


public class SearchPage extends AppCompatActivity implements IHistoryRepository {
    private boolean homeButtonVisible = false, changeLayoutButtonVisible = true, categoriesSearchExecuted = false;
    private ProductCategoriesAdapter categoriesAdapter;
    private ProductResultsAdapter resultsAdapter;
    private RecentSearchesAdapter recentsAdapter;
    private SearchForProducts searchForProducts;
    private int layoutMode = 1;
    private RecyclerView.LayoutManager listLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private Set<String> searchHistory = new LinkedHashSet<>();
    private String lastQuery;
    public static final String PREFS_NAME = "MyPrefsFile";

    public @BindView(R.id.loadingPanel) View loadingPanel;
    @BindView(R.id.productsRV) RecyclerView productsRV;
    @BindView(R.id.recentSearchesRV) RecyclerView recentSearchesRV;
    @BindView(R.id.searchVS) ViewSwitcher searchVS;
    @BindView(R.id.searchSV) SearchView searchSV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_page);

        //Init ButterKnife
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);

        confRecyclerViews();
        confCategoriesAdapter();
        confSearchView();
        confSearchHistory();

        IntentFilter productsFilter = new IntentFilter("SearchPage - Products are ready");
        LocalBroadcastManager.getInstance(this).registerReceiver(productsReceiver, productsFilter);

        if (savedInstanceState != null) {
            CharSequence savedSearchValue = savedInstanceState.getCharSequence("SearchValue");
            searchSV.setQuery(savedSearchValue, false);

            if (savedInstanceState.getString("ResultsExist").equals("Yes")) {
                lastQuery = savedSearchValue.toString();
                searchQuery();
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
        if (homeButtonVisible)
            menu.getItem(0).setVisible(true);
        else
            menu.getItem(0).setVisible(false);

        if (changeLayoutButtonVisible)
            menu.getItem(1).setVisible(true);
        else
            menu.getItem(1).setVisible(false);
        return true;
    }

    //Handles menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.homePage) {
            productsRV.setAdapter(categoriesAdapter);
            homeButtonVisible = false;
            invalidateOptionsMenu();
        }
        else if (item.getItemId() == R.id.changeLayout) {
            if (layoutMode == 1) {
                item.setIcon(R.drawable.change_layout_list);
                productsRV.setLayoutManager(gridLayoutManager);
                layoutMode++;
            }
            else {
                item.setIcon(R.drawable.change_layout_grid);
                productsRV.setLayoutManager(listLayoutManager);
                layoutMode = 1;
            }
            categoriesAdapter.setLayoutMode(layoutMode);

            if (resultsAdapter != null)
                resultsAdapter.setLayoutMode(layoutMode);
        }
        else {   //if itemId is R.id.wishList
            startActivity(new Intent(this, WishListPage.class));
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }

        return super.onOptionsItemSelected(item);
    }

    private void confRecyclerViews() {
        gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.itemsCount));
        listLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        productsRV.setLayoutManager(listLayoutManager);

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

    private void confCategoriesAdapter() {  //Conf it so it'll run only once when you change orientation
        String[] test = {"Clothing", "Shoes", "Electronics", "Appliances", "Tools", "Jewelry", "Beauty", "Groceries"};
        categoriesAdapter = new ProductCategoriesAdapter(test, this);
        productsRV.setAdapter(categoriesAdapter);
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
                    homeButtonVisible = false;
                    changeLayoutButtonVisible = false;
                    invalidateOptionsMenu();
                }
                else {
                    searchVS.showPrevious();

                    if (homeButtonVisible)
                        homeButtonVisible = true;

                    changeLayoutButtonVisible = true;
                    invalidateOptionsMenu();
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
                searchQuery();
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
    }

    public void initiateSearchFromHistory(int position) {
        int counter = 0;

        for (String recentSearch : searchHistory) {
            if (counter == (position - 1)) {
                searchSV.setQuery(recentSearch, false);
                lastQuery = recentSearch;
                searchQuery();
                break;
            }
            counter++;
        }
    }

    private void searchQuery() {
        //Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

        loadingPanel.setVisibility(View.VISIBLE);
        searchForProducts = new SearchForProducts(new HashSet<>(Arrays.asList(lastQuery)), "ByName", this);
    }

    private void setSearchResults(List<Product> results) {
        if (resultsAdapter == null) {
            resultsAdapter = new ProductResultsAdapter(results, this);
            resultsAdapter.setLayoutMode(layoutMode);
        }
        else {
            //Doesn't work from some reason so I'm creating a new adapter instead
            //resultsAdapter.setResults(results);
            //resultsAdapter.notifyDataSetChanged();

            resultsAdapter = new ProductResultsAdapter(results, this);
        }

        if (resultsAdapter.getItemCount() == 0) {
            (Snackbar.make(recentSearchesRV, "No results, please try again", Snackbar.LENGTH_LONG)).show();
            homeButtonVisible = false;

            productsRV.setAdapter(categoriesAdapter);
        }
        else {
            homeButtonVisible = true;
            searchSV.clearFocus();

            productsRV.setAdapter(resultsAdapter);

            if (categoriesSearchExecuted)   //If the search was done through clicking on one of the categories
                categoriesSearchExecuted = false;
            else {   //If the search was done through the SearchView
                searchHistory.add(lastQuery);
                saveSearchQueries(searchHistory);
                recentsAdapter.setRecents(new ArrayList<>(searchHistory));
                recentsAdapter.notifyDataSetChanged();
            }
        }
        invalidateOptionsMenu();  //Now onCreateOptionsMenu() is called again
    }

    private BroadcastReceiver productsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadingPanel.setVisibility(View.GONE);

            if (searchForProducts == null) {   //It means that the search was through the categoriesAdapter
                searchForProducts = categoriesAdapter.getSearchForProductsInstance();
                categoriesSearchExecuted = true;
            }

            setSearchResults(searchForProducts.getProducts());
            searchForProducts = null;
        }
    };

    @Override
    public Set<String> getSearchHistory() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> searchHistoryTemp = preferences.getStringSet("SearchHistory", this.searchHistory);
        LinkedHashSet<String> newSearchHistory = new LinkedHashSet<>();
        newSearchHistory.addAll(searchHistoryTemp);
        return newSearchHistory;
    }

    @Override
    public void saveSearchQueries(Set<String> queries) {
        //if clear recents button pressed
        if (queries == null)
            searchHistory = new LinkedHashSet<>();

        // We need an Editor object to make preference changes.
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("SearchHistory", searchHistory);

        // Commit the edits
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }
}
