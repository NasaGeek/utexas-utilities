
package com.nasageek.utexasutilities;

import java.util.Collection;

import android.support.v4.app.ListFragment;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class CanvasRequestListener<T extends Collection> implements RequestListener<T> {

    private ListAdapter mAdapter;
    private ListFragment mListFragment;
    private T mItems;
    private String mEmptyText;

    public CanvasRequestListener(ListFragment frag, ListAdapter adapter, T items, String emptyText) {
        this.mAdapter = adapter;
        this.mListFragment = frag;
        this.mItems = items;
        this.mEmptyText = emptyText;
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        spiceException.printStackTrace();
        Toast.makeText(mListFragment.getActivity(), "failure", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestSuccess(final T result) {
        mItems.addAll(result);
        mListFragment.setListAdapter(mAdapter);
        mListFragment.setEmptyText(mEmptyText);
    }

}
