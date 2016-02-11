
package com.nasageek.utexasutilities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foound.widget.AmazingAdapter;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.fragments.TransactionsFragment;
import com.nasageek.utexasutilities.model.Transaction;

import java.util.ArrayList;

public class TransactionAdapter extends AmazingAdapter {
    private final int VIEW_TYPE_HEADER = 0;
    private final int VIEW_TYPE_TRANSACTION = 1;

    private Context con;
    private ArrayList<Boolean> areHeaders;
    private ArrayList<Transaction> transactions;
    private LayoutInflater li;
    private String currentDate;
    private TransactionsFragment frag;
    private boolean drawLoadingView = false;

    public TransactionAdapter(Context c, TransactionsFragment frag,
            ArrayList<Transaction> transactions) {
        con = c;
        this.transactions = transactions;
        this.frag = frag;
        areHeaders = new ArrayList<>();
        li = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // just call updateHeaders() here? Did I duplicate this for a reason?
        for (int i = 0; i < transactions.size(); i++) {
            String date = transactions.get(i).getDate();
            if (i == 0) {
                currentDate = date;
                areHeaders.add(true);
            } else if (currentDate.equals(date)) {
                areHeaders.add(false);
            } else {
                areHeaders.add(true);
                currentDate = date;
            }
        }
    }

    @Override
    public int getCount() {
        return transactions.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return transactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return false;
    }

    // TODO: fix crash on refresh when in the middle of a scroll
    @Override
    public View getAmazingView(int position, View convertView, ViewGroup parent) {
        Transaction trans = transactions.get(position);

        String date = trans.getDate();
        String reason = "\t" + trans.getReason();
        String cost = trans.getCost();

        View view = convertView;
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                if (view == null) {
                    view = li.inflate(R.layout.trans_item_header_view, parent, false);
                }
                ((TextView) view.findViewById(R.id.list_item_section_text)).setText(date);
                // intentional fall-through so that the item and cost get set for headers too
            case VIEW_TYPE_TRANSACTION:
                if (view == null) {
                    view = li.inflate(R.layout.trans_item_view, parent, false);
                }
                ((TextView) view.findViewById(R.id.itemview)).setText(reason);
                ((TextView) view.findViewById(R.id.costview)).setText(cost);
                break;
        }
        return view;
    }



    @Override
    public int getItemViewType(int position) {
        if (position == getCount() - 1 && drawLoadingView) {
            return IGNORE_ITEM_VIEW_TYPE;
        } else if (areHeaders.size() == transactions.size() && areHeaders.get(position)) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_TRANSACTION;
        }
    }

    public void updateHeaders() {
        areHeaders.clear();
        for (int i = 0; i < transactions.size(); i++) {
            String date = transactions.get(i).getDate();
            if (i == 0) {
                currentDate = date;
                areHeaders.add(true);
            } else if (currentDate.equals(date)) {
                areHeaders.add(false);
            } else {
                areHeaders.add(true);
                currentDate = date;
            }
        }
    }

    @Override
    protected View getLoadingView(ViewGroup parent) {
        if (con != null) {
            LayoutInflater inflater = (LayoutInflater) con
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.loading_content_layout, parent, false);
        } else {
            return new View(con);
        }
    }

    @Override
    protected void onNextPageRequested(int page) {
        if (super.automaticNextPageLoading) {
            nextPage();
            frag.loadData(false);
        }
        super.automaticNextPageLoading = false;
    }

    @Override
    protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) { }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) { }

    @Override
    public int getPositionForSection(int section) {
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public String[] getSections() {
        return null;
    }

    @Override
    public void notifyNoMorePages() {
        drawLoadingView = false;
        super.notifyNoMorePages();
    }

    @Override
    public void notifyMayHaveMorePages() {
        drawLoadingView = true;
        super.notifyMayHaveMorePages();
    }
}
