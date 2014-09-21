
package com.nasageek.utexasutilities;

import android.support.v4.app.ListFragment;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Collection;

public class CanvasRequestListener<T extends Collection> implements RequestListener<T> {

    private ListAdapter mAdapter;
    private ListFragment mListFragment;
    private T mItems;

    public CanvasRequestListener(ListFragment frag, ListAdapter adapter, T items) {
        this.mAdapter = adapter;
        this.mListFragment = frag;
        this.mItems = items;
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        spiceException.printStackTrace();
        Toast.makeText(mListFragment.getActivity(), "Canvas request failed", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onRequestSuccess(final T result) {
        mItems.addAll(result);
        mListFragment.setListAdapter(mAdapter);
    }

}
