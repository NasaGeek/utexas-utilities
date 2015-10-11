
package com.nasageek.utexasutilities.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.v4.ArrayPagerAdapter;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.adapters.MultiPanePagerAdapter;
import com.nasageek.utexasutilities.fragments.TransactionsFragment;
import com.nasageek.utexasutilities.fragments.TransactionsFragment.TransactionType;
import com.nasageek.utexasutilities.model.SimplePageDescriptor;

import java.util.ArrayList;
import java.util.List;

public class BalanceActivity extends BaseActivity implements ArrayPagerAdapter.RetentionStrategy {

    private TransactionsPagerAdapter mPagerAdapter;
    private ViewPager pager;
    private int pagesDisplayed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_layout);
        pager = (ViewPager) findViewById(R.id.viewpager);
        pagesDisplayed = getResources().getInteger(R.integer.balance_num_visible_pages);
        initialisePaging();
        setupActionBar();
        actionBar.setElevation(0);
    }



    private void initialisePaging() {
        List<PageDescriptor> pages = new ArrayList<>();
        pages.add(new TransactionsPageDescriptor("Dine In", TransactionType.Dinein));
        pages.add(new TransactionsPageDescriptor("Bevo Bucks", TransactionType.Bevo));
        mPagerAdapter = new TransactionsPagerAdapter(getSupportFragmentManager(), pages, this);
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
            // tightly coupling the activity to the fragments for the sake of graphical consistency
            // was getting weird disappearing menu buttons when I had them in the fragments
            // TODO: should at least do this with an interface
            case R.id.balance_refresh:
                if (pagesDisplayed > 1) {
                    mPagerAdapter.getExistingFragment(0).refresh();
                    mPagerAdapter.getExistingFragment(1).refresh();
                } else {
                    // if the viewpager is only showing 1 page at a time only
                    // refresh the fragment currently in view
                    mPagerAdapter.getCurrentFragment().refresh();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.balance_menu, menu);
        return true;
    }

    @Override
    public void attach(Fragment fragment, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.attach(fragment);
    }

    @Override
    public void detach(Fragment fragment, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.remove(fragment);
    }

    static class TransactionsPagerAdapter extends MultiPanePagerAdapter<TransactionsFragment> {

        @Override
        protected TransactionsFragment createFragment(PageDescriptor pageDescriptor) {
            TransactionsPageDescriptor page = (TransactionsPageDescriptor) pageDescriptor;
            return TransactionsFragment.newInstance(page.getTransactionType());
        }

        public TransactionsPagerAdapter(FragmentManager fm, List<PageDescriptor> pages,
                                        RetentionStrategy strategy) {
            super(fm, pages, strategy);
        }
    }

    static class TransactionsPageDescriptor extends SimplePageDescriptor {

        private TransactionType type;

        public static final Parcelable.Creator<TransactionsPageDescriptor> CREATOR =
                new Parcelable.Creator<TransactionsPageDescriptor>() {
                    public TransactionsPageDescriptor createFromParcel(Parcel in) {
                        return new TransactionsPageDescriptor(in);
                    }

                    public TransactionsPageDescriptor[] newArray(int size) {
                        return new TransactionsPageDescriptor[size];
                    }
                };

        public TransactionsPageDescriptor(String title, TransactionType type) {
            super(title, title);
            this.type = type;
        }

        protected TransactionsPageDescriptor(Parcel in) {
            super(in);
            type = (TransactionType) in.readSerializable();
        }

        public TransactionType getTransactionType() {
            return type;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSerializable(type);
        }
    }
}
