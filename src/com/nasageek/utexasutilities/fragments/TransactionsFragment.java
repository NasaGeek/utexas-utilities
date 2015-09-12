
package com.nasageek.utexasutilities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.TempLoginException;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.activities.LoginActivity;
import com.nasageek.utexasutilities.adapters.TransactionAdapter;
import com.nasageek.utexasutilities.model.Transaction;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nasageek.utexasutilities.UTilitiesApplication.UTD_AUTH_COOKIE_KEY;

//TODO: last transaction doesn't show when loading dialog is present at the bottom, low priority fix

public class TransactionsFragment extends Fragment {
    private OkHttpClient httpclient;
    private LinearLayout t_pb_ll;
    private AmazingListView tlv;
    private ArrayList<Transaction> transactionlist;
    private TransactionAdapter ta;
    private TextView balanceLabelView, balanceView;
    private View balanceLabelSeparatorView;
    private TextView etv;
    private LinearLayout ell, transactionsLayout;

    private FormEncodingBuilder postdata;

    private View vg;
    private String balance;
    private fetchTransactionDataTask fetch;

    public enum TransactionType {
        Bevo, Dinein
    }

    private TransactionType mType;

    public TransactionsFragment() {
    }

    public static TransactionsFragment newInstance(String title, TransactionType type) {
        TransactionsFragment f = new TransactionsFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", type);
        args.putString("title", title);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vg = inflater.inflate(R.layout.transactions_fragment_layout, container, false);

        tlv = (AmazingListView) vg.findViewById(R.id.transactions_listview);
        t_pb_ll = (LinearLayout) vg.findViewById(R.id.trans_progressbar_ll);
        transactionsLayout = (LinearLayout) vg.findViewById(R.id.transactions_layout);
        etv = (TextView) vg.findViewById(R.id.tv_failure);
        ell = (LinearLayout) vg.findViewById(R.id.trans_error);
        balanceLabelView = (TextView) vg.findViewById(R.id.balance_label_tv);
        balanceLabelSeparatorView = vg.findViewById(R.id.balance_label_separator);
        balanceView = (TextView) vg.findViewById(R.id.balance_tv);

        /*
         * if(TransactionType.Bevo.equals(mType))
         * balanceLabelView.setText("Bevo Bucks "); else
         * if(TransactionType.Dinein.equals(mType))
         * balanceLabelView.setText("Dine In Dollars ");
         */
        if (!"".equals(balance)) {
            balanceView.setText(balance);
        }

        tlv.setLoadingView(inflater.inflate(R.layout.loading_content_layout, null));
        tlv.setAdapter(ta);

        if (ta.getCount() == 0) {
            parser(false);
        }

        return vg;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        postdata = new FormEncodingBuilder();
        mType = (TransactionType) getArguments().getSerializable("type");
        if (TransactionType.Bevo.equals(mType)) {
            postdata.add("sRequestSw", "B");
        } else if (TransactionType.Dinein.equals(mType)) {
            postdata.add("rRequestSw", "B");
        }

        if (savedInstanceState == null) {
            transactionlist = new ArrayList<>();
            balance = "";
        } else {
            transactionlist = savedInstanceState.getParcelableArrayList("transactions");
            // someone was crashing with a null transactionlist, shouldn't be
            // happening
            // but this should fix it regardless
            if (transactionlist == null) {
                transactionlist = new ArrayList<>();
            }
            balance = savedInstanceState.getString("balance");
        }

        ta = new TransactionAdapter(getActivity(), this, transactionlist);

    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelableArrayList("transactions", transactionlist);
        out.putString("balance", balance);
    }

    public void parser(boolean refresh) {
        httpclient = UTilitiesApplication.getInstance(getActivity()).getHttpClient();
        httpclient.setCookieHandler(CookieHandler.getDefault());
        fetch = new fetchTransactionDataTask(httpclient, refresh);
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
        if (fetch != null) {
            fetch.cancel(true);
            fetch = null;
        }
        transactionlist.clear();
        balance = "";

        postdata = new FormEncodingBuilder();
        if (TransactionType.Bevo.equals(mType)) {
            postdata.add("sRequestSw", "B");
        } else if (TransactionType.Dinein.equals(mType)) {
            postdata.add("rRequestSw", "B");
        }

        parser(true);
        ta.resetPage();
        ((ListView) tlv).setSelectionFromTop(0, 0);
    }

    private class fetchTransactionDataTask extends AsyncTask<Boolean, Void, Character> {
        private OkHttpClient client;
        private boolean refresh;
        private String errorMsg;
        private ArrayList<Transaction> tempTransactionList;

        public fetchTransactionDataTask(OkHttpClient client, boolean refresh) {
            this.client = client;
            this.refresh = refresh;
        }

        @Override
        protected void onPreExecute() {
            // only show the loading view if we're loading the first page of
            // transactions or refreshing
            if (ta.page == 1 || refresh) {
                t_pb_ll.setVisibility(View.VISIBLE);
                transactionsLayout.setVisibility(View.GONE);
                ell.setVisibility(View.GONE);
            }
        }

        @Override
        protected Character doInBackground(Boolean... params) {
            Boolean recursing = params[0];

            String reqUrl = "https://utdirect.utexas.edu/hfis/transactions.WBX";
            HttpCookie screenSizeCookie = new HttpCookie("webBrowserSize", "B");
            screenSizeCookie.setDomain(".utexas.edu");
            ((CookieManager) client.getCookieHandler()).getCookieStore()
                   .add(URI.create(".utexas.edu"), screenSizeCookie);
            Request request = new Request.Builder()
                    .post(postdata.build())
                    .url(reqUrl)
                    .build();
            String pagedata = "";
            tempTransactionList = new ArrayList<>();
            try {
                Response response = client.newCall(request).execute();
                pagedata = response.body().string();
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch your transactions. Try refreshing.";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            if (pagedata.contains("<title>UT EID Login</title>")) {
                errorMsg = "You've been logged out of UTDirect, back out and log in again.";
                if (getActivity() != null) {
                    UTilitiesApplication mApp = (UTilitiesApplication) getActivity().getApplication();
                    if (!recursing) {
                        try {
                            mApp.getAuthCookie(UTD_AUTH_COOKIE_KEY).logout();
                            mApp.getAuthCookie(UTD_AUTH_COOKIE_KEY).login();
                        } catch (IOException e) {
                            errorMsg
                                    = "UTilities could not fetch your transaction data.  Try refreshing.";
                            cancel(true);
                            e.printStackTrace();
                            return null;
                        } catch (TempLoginException tle) {
                            /*
                            ooooh boy is this lazy. I'd rather not init SharedPreferences here
                            to check if persistent login is on, so we'll just catch the exception
                             */
                            Intent login = new Intent(getActivity(), LoginActivity.class);
                            login.putExtra("activity", getActivity().getIntent().getComponent()
                                    .getClassName());
                            login.putExtra("service", 'u');
                            getActivity().startActivity(login);
                            getActivity().finish();
                            errorMsg = "Session expired, please log in again";
                            cancel(true);
                            return null;
                        }
                        return doInBackground(true);
                    } else {
                      mApp.logoutAll();
                    }
                }
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

            if (balanceMatcher.find() && ta.page == 1) {
                balance = balanceMatcher.group(1);
            }
            while (reasonMatcher.find() && costMatcher.find() && dateMatcher.find()
                    && !this.isCancelled()) {
                Transaction tran = new Transaction(reasonMatcher.group(1).trim(), costMatcher
                        .group(1).replaceAll("\\s", ""), dateMatcher.group(1));
                tempTransactionList.add(tran);
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
                    postdata = new FormEncodingBuilder();
                    postdata.add("sNameFL", nameMatcher.group(1));
                    postdata.add("nexttransid", nextTransMatcher.group(1));
                    if (TransactionType.Bevo.equals(mType)) {
                        postdata.add("sRequestSw", "B");
                    } else if (TransactionType.Dinein.equals(mType)) {
                        postdata.add("rRequestSw", "B");
                    }
                    postdata.add("sStartDateTime", dateTimeMatcher.group(1));
                }
                return 'm';
            } else {
                return 'n';
            }
        }

        @Override
        protected void onPostExecute(Character result) {
            if (!this.isCancelled()) {
                transactionlist.addAll(tempTransactionList);
                ta.notifyDataSetChanged();
                ta.updateHeaders();
                ta.notifyDataSetChanged();
                int index = tlv.getFirstVisiblePosition();
                View v = tlv.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
                if (result == 'm') {
                    ta.notifyMayHaveMorePages();
                }
                if (result == 'n') {
                    ta.notifyNoMorePages();
                }
                if (!refresh) {
                    ((ListView) tlv).setSelectionFromTop(index, top);
                } else {
                    tlv.setSelection(0);
                }

                if (transactionlist.isEmpty()) {
                    balanceLabelView.setText(getResources().getText(R.string.balance_tabs_no_balance));
                } else {
                    balanceView.setText(balance);
                }

                t_pb_ll.setVisibility(View.GONE);
                ell.setVisibility(View.GONE);
                transactionsLayout.setVisibility(View.VISIBLE);
                balanceLabelSeparatorView.setVisibility(transactionlist.isEmpty() ? View.GONE
                                                                                  : View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled(Character nullIfError) {
            if (nullIfError == null) {
                if (ta.page == 1) { // if the first page fails just hide
                                    // everything
                    // etv off center, not sure if worth hiding the balance
                    // stuff to get it centered
                    etv.setText(errorMsg);
                    t_pb_ll.setVisibility(View.GONE);
                    transactionsLayout.setVisibility(View.GONE);
                    ell.setVisibility(View.VISIBLE);
                } else { // on later pages we should let them see what's already
                         // loaded
                         // got an NPE here, seems like a race condition where
                         // cancel is called externally
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                    ta.notifyNoMorePages();
                    ta.notifyDataSetChanged();
                }
            }
        }
    }
}
