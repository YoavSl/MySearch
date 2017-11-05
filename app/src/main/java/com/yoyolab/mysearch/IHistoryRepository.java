package com.yoyolab.mysearch;

import java.util.Set;

public interface IHistoryRepository {
    /**
     * Saves a query to the search history
     * @param queries to save
     */
    void addSearchQueries(Set<String> queries);
    /**
     * Get all the added search history
     * @return Set of all saved searches
     */
    Set<String> getSearchHistory();
}