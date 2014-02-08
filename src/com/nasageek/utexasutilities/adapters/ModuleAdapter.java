
package com.nasageek.utexasutilities.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foound.widget.AmazingAdapter;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.model.canvas.Module;
import com.nasageek.utexasutilities.model.canvas.ModuleItem;

public class ModuleAdapter extends AmazingAdapter {

    private LayoutInflater mInflater;
    private List<Module> mItems;

    public ModuleAdapter(Context context, List<Module> objects) {
        super();
        mItems = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        int count = 0;
        for (Module m : mItems) {
            count += m.items_count;
        }
        return count;
    }

    @Override
    public ModuleItem getItem(int position) {
        int c = 0;
        for (int i = 0; i < mItems.size(); i++) {
            if (position >= c && position < c + mItems.get(i).items_count) {
                return mItems.get(i).items.get(position - c);
            }
            c += mItems.get(i).items_count;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
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
    public void configurePinnedHeader(View header, int position, int arg2) {
        TextView lSectionHeader = (TextView) header;
        lSectionHeader.setText(getSections()[getSectionForPosition(position)]);
    }

    // TODO: show assignment score using the magic of content_details
    @Override
    public View getAmazingView(int position, View convertView, ViewGroup parent) {
        ModuleItem item = getItem(position);
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.file_view, parent, false);
        } else {
            view = convertView;
        }

        ((TextView) view.findViewById(R.id.file_name)).setText(item.title);
        view.findViewById(R.id.file_size).setVisibility(View.GONE);
        return view;
    }

    @Override
    protected View getLoadingView(ViewGroup position) {
        return null;
    }

    @Override
    public int getPositionForSection(int section) {
        if (section < 0) {
            section = 0;
        }
        if (section >= mItems.size()) {
            section = mItems.size() - 1;
        }
        int c = 0;
        for (int i = 0; i < mItems.size(); i++) {
            if (section == i) {
                return c;
            }
            c += mItems.get(i).items_count;
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        int c = 0;
        for (int i = 0; i < mItems.size(); i++) {
            if (position >= c && position < c + mItems.get(i).items_count) {
                return i;
            }
            c += mItems.get(i).items_count;
        }
        return 0;
    }

    @Override
    public String[] getSections() {
        String[] sect = new String[mItems.size()];
        for (int i = 0; i < mItems.size(); i++) {
            sect[i] = mItems.get(i).name;
        }
        return sect;
    }

    @Override
    protected void onNextPageRequested(int arg0) {
    }

}
