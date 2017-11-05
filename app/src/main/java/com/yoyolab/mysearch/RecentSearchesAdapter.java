package com.yoyolab.mysearch;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesAdapter.ViewHolder> implements Filterable {
    private List<String> recentSearches;
    List<String> mFilterList;
    private ValueFilter valueFilter;

    public RecentSearchesAdapter (List<String> recents) {
        recentSearches = recents;
        mFilterList = recents;
    }

    public void setRecents(List<String> recents) {
        recentSearches = recents;
        mFilterList = recents;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_search,parent,false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(recentSearches.get(position));
    }

    @Override
    public int getItemCount() {
        return recentSearches == null ? 0 : recentSearches.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.recentSearchTV) TextView recentSearchTV;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
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
                for (int i = 0; i < mFilterList.size(); i++) {
                    if ((mFilterList.get(i).toUpperCase()).contains(constraint.toString().toUpperCase()))
                        filterList.add(mFilterList.get(i));
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mFilterList.size();
                results.values = mFilterList;
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
