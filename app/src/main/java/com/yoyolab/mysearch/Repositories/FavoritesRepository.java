package com.yoyolab.mysearch.Repositories;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.yoyolab.mysearch.Activities.SearchPage.PREFS_NAME;

public class FavoritesRepository {
    private Context context;
    private Set<String> favoriteItems = new HashSet<>();

    public FavoritesRepository(Context context) {
        this.context = context;
        favoriteItems = getItems();
    }

    public Set<String> getItems() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> favoriteItemsTemp = preferences.getStringSet("FavoriteItems", favoriteItems);
        HashSet<String> newFavoriteItems = new HashSet<>();
        newFavoriteItems.addAll(favoriteItemsTemp);
        return newFavoriteItems;
    }

    public void saveItems(Set<String> items) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();editor.putStringSet("FavoriteItems", favoriteItems);
        editor.apply();
    }

    public void addItem(int item) {
        favoriteItems.add(String.valueOf(item));
        saveItems(favoriteItems);
    }

    public void removeItem(int item) {
        favoriteItems.remove(String.valueOf(item));
        saveItems(favoriteItems);
    }

    public Boolean checkIfExists(int item) {
        return favoriteItems.contains(String.valueOf(item));
    }
}
