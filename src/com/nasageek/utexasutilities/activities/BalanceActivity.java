
package com.nasageek.utexasutilities.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.adapters.MultiPanePagerAdapter;
import com.nasageek.utexasutilities.fragments.TransactionsFragment;
import com.nasageek.utexasutilities.fragments.TransactionsFragment.TransactionType;

import java.util.List;
import java.util.Vector;

public class BalanceActivity extends BaseActivity {

    private MultiPanePagerAdapter mPagerAdapter;
    private ViewPager pager;
    private int pagesDisplayed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_layout);

        pager = (ViewPager) findViewById(R.id.viewpager);
        pagesDisplayed = getResources().getInteger(R.integer.balance_num_visible_pages);

        this.initialisePaging();

        setupActionBar();
        actionBar.setElevation(0);
    }

    private void initialisePaging() {

        List<Fragment> fragments = new Vector<>();
        /**
         * this is a bit of a hacky solution for something that should be
         * handled by default. on a rotate, pager caches the old fragments (with
         * setRetainInstance(true)), but the adapter does not, so I have to add
         * the old fragments back to the adapter manually
         */
        if (getSupportFragmentManager().findFragmentByTag(
                Utility.makeFragmentName(pager.getId(), 0)) != null) {
            fragments.add(getSupportFragmentManager().findFragmentByTag(
                    Utility.makeFragmentName(pager.getId(), 0)));
            fragments.add(getSupportFragmentManager().findFragmentByTag(
                    Utility.makeFragmentName(pager.getId(), 1)));
        } else {
            fragments.add(TransactionsFragment.newInstance("Dine In", TransactionType.Dinein));
            fragments.add(TransactionsFragment.newInstance("Bevo Bucks", TransactionType.Bevo));
        }

        mPagerAdapter = new MultiPanePagerAdapter(getSupportFragmentManager(), fragments);
        mPagerAdapter.setPagesDisplayed(pagesDisplayed);

        pager.setAdapter(this.mPagerAdapter);
        pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin));
        // TODO: reimplement selectAll hack with design support library
//        if (pagesDisplayed > 1) {
//            tabIndicator.setSelectAll(true);
//        }

        ((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(pager);
        float elevationPx = getResources().getDimensionPixelSize(R.dimen.actionbar_elevation);
        ViewCompat.setElevation(findViewById(R.id.tabs), elevationPx);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        pager.setCurrentItem(Integer.parseInt(sp.getString("default_balance_tab", "0")));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // tightly coupling the activity to the fragments for the sake of
            // graphical consistency
            // was getting weird disappearing menu buttons when I had them in
            // the fragments
            // TODO: should at least do this with an interface
            case R.id.balance_refresh:
                if (pagesDisplayed > 1) {
                    ((TransactionsFragment) ((MultiPanePagerAdapter) pager.getAdapter()).getItem(0))
                            .refresh();
                    ((TransactionsFragment) ((MultiPanePagerAdapter) pager.getAdapter()).getItem(1))
                            .refresh();
                } else {
                    // if the viewpager is only showing 1 page at a time only
                    // refresh the fragment currently in view
                    ((TransactionsFragment) ((MultiPanePagerAdapter) pager.getAdapter())
                            .getItem(pager.getCurrentItem())).refresh();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.balance_menu, menu);
        return true;
    }
}
