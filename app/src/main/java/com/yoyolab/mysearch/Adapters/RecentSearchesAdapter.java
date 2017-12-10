package com.yoyolab.mysearch.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.yoyolab.mysearch.Activities.SearchPage;
import com.yoyolab.mysearch.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesAdapter.ViewHolder> implements Filterable {
    private List<String> recentSearches, filterList;
    private SearchPage searchPageActivity;
    private ValueFilter valueFilter;
    private int itemType;


    public RecentSearchesAdapter (List<String> recentSearches, SearchPage searchPageActivity) {
        //Collections.sort(recentSearches , Collections.reverseOrder());

        this.recentSearches = recentSearches;
        this.filterList = recentSearches;
        this.searchPageActivity = searchPageActivity;
    }

    public void setRecents(List<String> recents) {
        //Collections.sort(recents , Collections.reverseOrder());

        recentSearches = recents;
        filterList = recents;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            itemType = 1;
            return  itemType;
        }
        else{
            itemType = 2;
            return  itemType;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root;

        if (viewType == 1)
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.clear_recent_searches,parent,false);
        else
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_search,parent,false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (itemType == 2)
            holder.bind(recentSearches.get(position - 1));
    }

    @Override
    public int getItemCount() {
        return ((recentSearches == null) || (recentSearches.size() == 0)) ? 0 : (recentSearches.size() + 1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private Button clearRecentsBT;
        private TextView recentSearchTV;

        public ViewHolder(View itemView) {
            super(itemView);

            if (itemType == 1) {
                clearRecentsBT = itemView.findViewById(R.id.clearRecentsBT);

                clearRecentsBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int listSize = recentSearches.size() + 1;
                        recentSearches.clear();
                        filterList.clear();
                        notifyItemRangeRemoved(0, listSize);

                        searchPageActivity.saveSearchQueries(null);
                    }
                });
            }
            else {
                recentSearchTV = itemView.findViewById(R.id.recentSearchTV);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchPageActivity.initiateSearchFromHistory(getAdapterPosition());
                    }
                });
            }
        }

        public void bind(String recentSearch) {
            recentSearchTV.setText(recentSearch);
        }
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<String> filterList = new ArrayList<>();
                for (int i = 0; i < filterList.size(); i++) {
                    if ((filterList.get(i).toUpperCase()).contains(constraint.toString().toUpperCase()))
                        filterList.add(filterList.get(i));
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = filterList.size();
                results.values = filterList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            recentSearches = (List<String>) results.values;
            notifyDataSetChanged();
        }

    }
}
