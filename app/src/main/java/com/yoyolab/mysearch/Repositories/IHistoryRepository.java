package com.yoyolab.mysearch.Repositories;

import java.util.Set;

public interface IHistoryRepository {
    /**
     * Saves all queries to the search history
     * @param queries to save
     */
    void saveSearchQueries(Set<String> queries);

    /**
     * Get all the added search history
     * @return Set of all saved searches
     */
    Set<String> getSearchHistory();
}