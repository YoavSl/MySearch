package com.yoyolab.mysearch;

import java.util.Set;

public interface IHistoryRepository {
    /**
     * Saves a query to the search history
     * @param query to save
     */
    void addSearchQuery(String query);
    /**
     * Get all the added search history
     * @return Set of all saved searches
     */
    Set<String> getSearchHistory();
}