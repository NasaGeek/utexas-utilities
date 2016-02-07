
package com.nasageek.utexasutilities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.MyBus;
import com.nasageek.utexasutilities.NotAuthenticatedException;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.UTLoginTask;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.adapters.TransactionAdapter;
import com.nasageek.utexasutilities.model.LoadFailedEvent;
import com.nasageek.utexasutilities.model.Transaction;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: last transaction doesn't show when loading dialog is present at the bottom, low priority fix

public class TransactionsFragment extends DataLoadFragment {
    private LinearLayout progressLayout;
    private LinearLayout transactionsLayout;
    private AmazingListView transactionsListView;
    private TextView balanceView;
    private LinearLayout errorLayout;
    private TextView errorTextView;
    private TransactionAdapter adapter;

    private RequestBody form;
    private ArrayList<Transaction> transactionlist = new ArrayList<>();
    private FetchTransactionDataTask fetch;
    private String url;
    private String TASK_TAG;
    private final UTilitiesApplication mApp = UTilitiesApplication.getInstance();

    public enum TransactionType {
        Bevo, Dinein
    }

    private TransactionType mType;

    public static TransactionsFragment newInstance(TransactionType type, String url) {
        TransactionsFragment f = new TransactionsFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", type);
        args.putString("url", url);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vg = inflater.inflate(R.layout.transactions_fragment_layout, container, false);

        transactionsListView = (AmazingListView) vg.findViewById(R.id.transactions_listview);
        progressLayout = (LinearLayout) vg.findViewById(R.id.trans_progressbar_ll);
        transactionsLayout = (LinearLayout) vg.findViewById(R.id.transactions_layout);
        errorTextView = (TextView) vg.findViewById(R.id.tv_failure);
        errorLayout = (LinearLayout) vg.findViewById(R.id.trans_error);
        balanceView = (TextView) vg.findViewById(R.id.balance_tv);
        if (savedInstanceState != null) {
            balanceView.setText(savedInstanceState.getString("balance"));
            switch (loadStatus) {
                case NOT_STARTED:
                    // defaults should suffice
                    break;
                case LOADING:
                    progressLayout.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                    transactionsLayout.setVisibility(View.GONE);
                    break;
                case SUCCEEDED:
                    progressLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.GONE);
                    transactionsLayout.setVisibility(View.VISIBLE);
                    break;
                case FAILED:
                    progressLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                    transactionsLayout.setVisibility(View.GONE);
                    break;
            }
        }
        transactionsListView.setLoadingView(inflater.inflate(R.layout.loading_content_layout, null));
        transactionsListView.setAdapter(adapter);
        if (loadStatus == LoadStatus.NOT_STARTED && mApp.getCachedTask(TASK_TAG) == null) {
            loadData(false);
        }
        return vg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            transactionlist = savedInstanceState.getParcelableArrayList("transactions");
        }
        adapter = new TransactionAdapter(getActivity(), this, transactionlist);
    }

    @Override
    public void onStart() {
        super.onStart();
        MyBus.getInstance().register(this);
    }

    @Override
    public void onStop() {
        MyBus.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelableArrayList("transactions", transactionlist);
        out.putString("balance", balanceView.getText().toString());
//        out.putSerializable("form", form);
    }

    public void loadData(boolean refresh) {
        // only show the loading view if we're loading the first page of
        // transactions or refreshing
        loadStatus = LoadStatus.LOADING;
        if (adapter.page == 1 || refresh) {
            prepareToLoad();
        }
        FormEncodingBuilder postdata = new FormEncodingBuilder();
        mType = (TransactionType) getArguments().getSerializable("type");
        url = getArguments().getString("url");
        TASK_TAG = getClass().getSimpleName() + mType.toString();
        if (TransactionType.Bevo.equals(mType)) {
            postdata.add("sRequestSw", "B");
        } else if (TransactionType.Dinein.equals(mType)) {
            postdata.add("rRequestSw", "B");
        }
        form = postdata.build();

        fetch = new FetchTransactionDataTask(TASK_TAG, mType, url, form);
        Utility.parallelExecute(fetch, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetch != null) {
            fetch.cancel(true);
        }
    }

    public void refresh() {
        if (loadStatus == LoadStatus.LOADING) {
            return;
        }
        transactionlist.clear();

        loadData(true);
        adapter.resetPage();
        ((ListView) transactionsListView).setSelectionFromTop(0, 0);
    }

    static class FetchTransactionDataTask extends UTLoginTask<Boolean, Void, Boolean> {
        private String balance = "";
        private List<Transaction> transactions = new ArrayList<>();
        private TransactionType type;
        private RequestBody form;

        public FetchTransactionDataTask(String tag, TransactionType type, String url,
                                        RequestBody form) {
            super(tag, url);
            this.type = type;
            this.form = form;
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            Boolean recursing = params[0];

            HttpCookie screenSizeCookie = new HttpCookie("webBrowserSize", "B");
            screenSizeCookie.setDomain(".utexas.edu");
            ((CookieManager) client.getCookieHandler()).getCookieStore()
                   .add(URI.create(".utexas.edu"), screenSizeCookie);
            Request request = new Request.Builder()
                    .post(form)
                    .url(reqUrl)
                    .build();
            String pagedata;
            try {
                pagedata = fetchData(request);
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch your transactions. Try refreshing.";
                e.printStackTrace();
                cancel(true);
                return null;
            } catch (NotAuthenticatedException e) {
                e.printStackTrace();
                cancel(true);
                return null;
            }

            Pattern reasonPattern = Pattern.compile("\"center\">\\s+(.*?)\\s*<");
            Matcher reasonMatcher = reasonPattern.matcher(pagedata);

            Pattern costPattern = Pattern.compile("\"right\">\\s*(.*)</td>\\s*<td");
            Matcher costMatcher = costPattern.matcher(pagedata);

            Pattern datePattern = Pattern.compile("\"left\">\\s*?(\\S+)");
            Matcher dateMatcher = datePattern.matcher(pagedata);

            Pattern balancePattern = Pattern.compile("\"right\">\\s*(.*)</td>\\s*</tr");
            Matcher balanceMatcher = balancePattern.matcher(pagedata);

            // might need to check if we're on page 1 before setting balance
            if (balanceMatcher.find()) {
                balance = balanceMatcher.group(1);
            }
            while (reasonMatcher.find() && costMatcher.find() && dateMatcher.find()
                    && !this.isCancelled()) {
                Transaction tran = new Transaction(reasonMatcher.group(1).trim(), costMatcher
                        .group(1).replaceAll("\\s", ""), dateMatcher.group(1));
                transactions.add(tran);
            }
            if (transactions.isEmpty()) {
                cancel(true);
                errorMsg = UTilitiesApplication.getInstance()
                                .getText(R.string.balance_tabs_no_balance).toString();
                return null;
            }
            // check for additional pages
            if (pagedata.contains("<form name=\"next\"") && !this.isCancelled()) {
                Pattern namePattern = Pattern.compile("sNameFL\".*?value=\"(.*?)\"");
                Matcher nameMatcher = namePattern.matcher(pagedata);
                Pattern nextTransPattern = Pattern.compile("nexttransid\".*?value=\"(.*?)\"");
                Matcher nextTransMatcher = nextTransPattern.matcher(pagedata);
                Pattern dateTimePattern = Pattern.compile("sStartDateTime\".*?value=\"(.*?)\"");
                Matcher dateTimeMatcher = dateTimePattern.matcher(pagedata);
                if (nameMatcher.find() && nextTransMatcher.find() && dateTimeMatcher.find()
                        && !this.isCancelled()) {
                    FormEncodingBuilder postdata = new FormEncodingBuilder();
                    postdata.add("sNameFL", nameMatcher.group(1));
                    postdata.add("nexttransid", nextTransMatcher.group(1));
                    if (TransactionType.Bevo.equals(type)) {
                        postdata.add("sRequestSw", "B");
                    } else if (TransactionType.Dinein.equals(type)) {
                        postdata.add("rRequestSw", "B");
                    }
                    postdata.add("sStartDateTime", dateTimeMatcher.group(1));
                    form = postdata.build();
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean morePagesAvailable) {
            super.onPostExecute(morePagesAvailable);
            MyBus.getInstance().post(new LoadSucceededEvent(getTag(), transactions, balance,
                    morePagesAvailable, form));
        }
    }

    public void prepareToLoad() {
        progressLayout.setVisibility(View.VISIBLE);
        transactionsLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    @Subscribe
    public void loadFailed(LoadFailedEvent event) {
        if (event.tag.equals(TASK_TAG)) {
            if (adapter.page == 1) {
                // if the first page fails just hide everything
                errorTextView.setText(event.errorMessage);
                progressLayout.setVisibility(View.GONE);
                transactionsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
                // only set LoadStatus to failed if it's loading the first page, otherwise
                // everything gets hidden on rotate FIXME
                loadStatus = LoadStatus.FAILED;
            } else {
                // on later pages we should let them see what's already loaded
                Toast.makeText(getActivity(), event.errorMessage, Toast.LENGTH_SHORT).show();
                adapter.notifyNoMorePages();
                adapter.notifyDataSetChanged();
                // FIXME
                loadStatus = LoadStatus.SUCCEEDED;
            }
        }
    }

    @Subscribe
    public void loadSucceeded(LoadSucceededEvent event) {
        if (event.tag.equals(TASK_TAG)) {
            loadStatus = LoadStatus.SUCCEEDED;
            transactionlist.addAll(event.transactions);
            adapter.notifyDataSetChanged();
            adapter.updateHeaders();
            adapter.notifyDataSetChanged();
            View v = transactionsListView.getChildAt(0);
            int index = transactionsListView.getFirstVisiblePosition();
            int top = (v == null) ? 0 : v.getTop();
            if (event.morePagesAvailable) {
                adapter.notifyMayHaveMorePages();
                form = event.form;
            } else {
                adapter.notifyNoMorePages();
            }
//        if (!refresh) {
//            ((ListView) transactionsListView).setSelectionFromTop(index, top);
//        } else {
//            transactionsListView.setSelection(0);
//        }
            if (!"".equals(event.balance)) {
                balanceView.setText(event.balance);
            }
            transactionsLayout.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
        }
    }

    static class LoadSucceededEvent {
        public String tag;
        public List<Transaction> transactions;
        public String balance;
        public boolean morePagesAvailable;
        public RequestBody form;

        public LoadSucceededEvent(String tag, List<Transaction> transactions, String balance,
                                  boolean morePagesAvailable, RequestBody form) {
            this.tag = tag;
            this.transactions = transactions;
            this.balance = balance;
            this.morePagesAvailable = morePagesAvailable;
            this.form = form;
        }
    }
}
