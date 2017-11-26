package com.yoyolab.mysearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.yoyolab.mysearch.SearchPage.PREFS_NAME;


public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesAdapter.ViewHolder> implements Filterable {
    private List<String> recentSearches;
    private List<String> mFilterList;
    private Context appContext;
    private ValueFilter valueFilter;
    private int itemType;

    public RecentSearchesAdapter (List<String> recents, Context context) {
        recentSearches = recents;
        mFilterList = recents;
        appContext = context;
    }

    public void setRecents(List<String> recents) {
        recentSearches = recents;
        mFilterList = recents;
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
        private TextView clearRecentsBT;
        private TextView recentSearchTV;

        public ViewHolder(View itemView) {
            super(itemView);
            if (itemType == 1) {
                clearRecentsBT = itemView.findViewById(R.id.clearRecentsBT);
                /*clearRecentsBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recentSearches.remove(0);
                        notifyItemRemoved(0);
                        notifyItemRangeChanged(0, recentSearches.size() + 1);
                    }
                }); */

                clearRecentsBT.setOnTouchListener(new View.OnTouchListener(){
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            int listSize = recentSearches.size() + 1;
                            recentSearches.clear();
                            mFilterList.clear();
                            notifyItemRangeRemoved(0, listSize);

                            //Delete recent searches from shared preferences
                            SharedPreferences settings = appContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putStringSet("SearchHistory", new HashSet<String>());
                            editor.apply();
                        }
                        return true;
                    }
                });
            }
            else
                recentSearchTV = itemView.findViewById(R.id.recentSearchTV);
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
