package com.yoyolab.mysearch.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.yoyolab.mysearch.Adapters.ProductResultsAdapter;
import com.yoyolab.mysearch.Repositories.FavoritesRepository;
import com.yoyolab.mysearch.Model.Product;
import com.yoyolab.mysearch.R;
import com.yoyolab.mysearch.Services.SearchForProducts;


public class WishListPage extends AppCompatActivity {
    private boolean changeLayoutVisible = false;
    private Set<String> favoriteItems = new HashSet<>();
    private ProductResultsAdapter favoritesAdapter;
    private SearchForProducts searchForProducts;
    private int layoutMode = 1;
    private RecyclerView.LayoutManager listLayoutManager;
    private GridLayoutManager gridLayoutManager;

    @BindView(R.id.wishListTitleView) View wishListTitleView;
    @BindView(R.id.loadingPanel) View loadingPanel;
    @BindView(R.id.emptyWishListView) View emptyWishListView;
    @BindView(R.id.favoritesRV) RecyclerView favoritesRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wish_list_page);

        //Init ButterKnife
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);

        FavoritesRepository favoritesRepository = new FavoritesRepository(getApplicationContext());
        favoriteItems = favoritesRepository.getItems();

        if (favoriteItems.size() > 0) {
            IntentFilter productsFilter = new IntentFilter("WishListPage - Products are ready");
            LocalBroadcastManager.getInstance(this).registerReceiver(productsReceiver, productsFilter);

            IntentFilter wishListEmptyFilter = new IntentFilter("WishListPage - Wish list is empty");
            LocalBroadcastManager.getInstance(this).registerReceiver(wishListEmptyReceiver, wishListEmptyFilter);

            confFavoritesList();
            loadFavoriteItems();
        }
        else
            displayEmptyListView();

        if (savedInstanceState != null) {
            if (savedInstanceState.getString("FavoritesExist").equals("Yes")) {
                //loadFavoriteItems();
            }
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        if (favoritesAdapter == null)
            outState.putString("FavoritesExist", "No");
        else
            outState.putString("FavoritesExist", "Yes");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu from xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wish_list_menu, menu);
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
            favoritesRV.setLayoutManager(gridLayoutManager);
            layoutMode++;
        }
        else {
            item.setIcon(R.drawable.change_layout_grid);
            favoritesRV.setLayoutManager(listLayoutManager);
            layoutMode = 1;
        }
        favoritesAdapter.setLayoutMode(layoutMode);

        return super.onOptionsItemSelected(item);
    }

    private void confFavoritesList() {
        gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.itemsCount));
        listLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        favoritesRV.setLayoutManager(listLayoutManager);

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
        favoritesRV.setLayoutAnimation(controller);
    }

    private void loadFavoriteItems() {
        loadingPanel.setVisibility(View.VISIBLE);
        searchForProducts = new SearchForProducts(favoriteItems, "ByProductId", this);
    }

    private void setFavorites(List<Product> favorites) {
        favoritesAdapter = new ProductResultsAdapter(favorites, this);
        favoritesRV.setAdapter(favoritesAdapter);

        //emptySearchView.setVisibility(View.GONE);
        changeLayoutVisible = true;
        invalidateOptionsMenu();  //Now onCreateOptionsMenu() is called again
    }

    private void displayEmptyListView() {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f );
        fadeIn.setDuration(400);
        fadeIn.setFillAfter(true);

        AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f );
        fadeOut.setDuration(400);
        fadeOut.setFillAfter(true);

        wishListTitleView.setVisibility(View.GONE);
        wishListTitleView.startAnimation(fadeOut);

        favoritesRV.setVisibility(View.GONE);
        favoritesRV.startAnimation(fadeOut);

        emptyWishListView.setVisibility(View.VISIBLE);
        emptyWishListView.startAnimation(fadeIn);
    }

    private BroadcastReceiver productsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadingPanel.setVisibility(View.GONE);
            setFavorites(searchForProducts.getProducts());
        }
    };

    private BroadcastReceiver wishListEmptyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            displayEmptyListView();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }
}
