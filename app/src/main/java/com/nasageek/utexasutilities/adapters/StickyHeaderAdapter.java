package com.nasageek.utexasutilities.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foound.widget.AmazingAdapter;
import com.nasageek.utexasutilities.MyPair;
import com.nasageek.utexasutilities.R;

import java.util.List;

public abstract class StickyHeaderAdapter<T> extends AmazingAdapter {

    private List<MyPair<String, List<T>>> all;
    protected Context mContext;

    public StickyHeaderAdapter(Context con, List<MyPair<String, List<T>>> all) {
        this.all = all;
        this.mContext = con;
    }

    @Override
    public int getCount() {
        int res = 0;
        for (MyPair<String, List<T>> item : all) {
            res += item.second.size();
        }
        return res;
    }

    @Override
    public T getItem(int position) {
        int c = 0;
        for (MyPair<String, List<T>> item : all) {
            if (position >= c && position < c + item.second.size()) {
                return item.second.get(position - c);
            }
            c += item.second.size();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected void onNextPageRequested(int page) {
    }

    @Override
    protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
        if (displaySectionHeader) {
            view.findViewById(R.id.header).setVisibility(View.VISIBLE);
            TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
            lSectionTitle.setText(getSections()[getSectionForPosition(position)]);
        } else {
            view.findViewById(R.id.header).setVisibility(View.GONE);
        }
    }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) {
        TextView lSectionHeader = (TextView) header;
        lSectionHeader.setText(getSections()[getSectionForPosition(position)]);
    }

    @Override
    public int getPositionForSection(int section) {
        if (section < 0) {
            section = 0;
        }
        if (section >= all.size()) {
            section = all.size() - 1;
        }
        int c = 0;
        for (int i = 0; i < all.size(); i++) {
            if (section == i) {
                return c;
            }
            c += all.get(i).second.size();
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        int c = 0;
        for (int i = 0; i < all.size(); i++) {
            if (position >= c && position < c + all.get(i).second.size()) {
                return i;
            }
            c += all.get(i).second.size();
        }
        return 0;
    }

    @Override
    public String[] getSections() {
        String[] res = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            res[i] = all.get(i).first;
        }
        return res;
    }

    @Override
    protected View getLoadingView(ViewGroup parent) {
        return null;
    }
}
