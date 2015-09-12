
package com.nasageek.utexasutilities.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.AsyncTask;
import com.nasageek.utexasutilities.MyPair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.activities.NutritionInfoActivity;
import com.nasageek.utexasutilities.adapters.StickyHeaderAdapter;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuFragment extends Fragment {
    private OkHttpClient httpclient;
    private List<MyPair<String, List<food>>> listOfLists;
    private AmazingListView mlv;
    private LinearLayout m_pb_ll;
    private TextView metv;
    private LinearLayout mell;
    private fetchMenuTask fetchMTask;
    private String restId;
    private MenuAdapter mAdapter;

    public MenuFragment() {
    }

    public static MenuFragment newInstance(String title, String restId) {
        MenuFragment f = new MenuFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("restId", restId);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vg = inflater.inflate(R.layout.menu_fragment_layout, container, false);

        m_pb_ll = (LinearLayout) vg.findViewById(R.id.menu_progressbar_ll);
        mlv = (AmazingListView) vg.findViewById(R.id.menu_listview);
        metv = (TextView) vg.findViewById(R.id.tv_failure);
        mell = (LinearLayout) vg.findViewById(R.id.menu_error);

        if (restId.equals("0")) {
            return vg;
        }
        updateView(restId, false);
        return vg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        restId = getArguments().getString("restId");
        httpclient = UTilitiesApplication.getInstance(getActivity()).getHttpClient();
        listOfLists = new ArrayList<>();
        mAdapter = new MenuAdapter(listOfLists);
    }

    public void updateView(String restId, Boolean update) {
        this.restId = restId;

        mlv.setAdapter(mAdapter);
        mlv.setPinnedHeaderView(getActivity().getLayoutInflater().inflate(
                R.layout.menu_header_item_view, mlv, false));
        mlv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                String url = "http://129.116.62.55/foodpro/"
                        + ((food) (arg0.getItemAtPosition(arg2))).nutritionLink;

                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
                if (sp.getBoolean("embedded_browser", true)) {

                    Intent i = new Intent(getActivity(), NutritionInfoActivity.class);
                    i.putExtra("url", url);
                    i.putExtra("title", ((food) arg0.getItemAtPosition(arg2)).name);
                    startActivity(i);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }

            }
        });

        if (listOfLists.size() == 0 || update) {
            listOfLists.clear();
            fetchMTask = new fetchMenuTask(httpclient);
            Utility.parallelExecute(fetchMTask, restId,
                    this.getArguments().getString("title"), mlv);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetchMTask != null) {
            fetchMTask.cancel(true);
        }
    }

    private class fetchMenuTask extends AsyncTask<Object, Integer, String> {
        private OkHttpClient client;
        private String meal;
        private String errorMsg;
        private List<MyPair<String, List<food>>> tempListOfLists;

        public fetchMenuTask(OkHttpClient client) {
            this.client = client;
        }

        @Override
        protected void onPreExecute() {
            m_pb_ll.setVisibility(View.VISIBLE);
            mell.setVisibility(View.GONE);
            mlv.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(Object... params) {
            ArrayList<String> categories = new ArrayList<>();
            List<food> foodList = new ArrayList<>();
            tempListOfLists = new ArrayList<>();
            meal = (String) params[1];
            String location;

            // Special case for JCM, which combines Lunch and Dinner
            if (restId.equals("05") && (meal.equals("Lunch") || meal.equals("Dinner"))) {
                location = "http://129.116.62.55/foodpro/pickMenu.asp?locationNum=" + params[0]
                        + "&mealName=Lunch/Dinner";
            } else {
                location = "http://129.116.62.55/foodpro/pickMenu.asp?locationNum=" + params[0]
                        + "&mealName=" + meal;
            }


            Request request = new Request.Builder()
                    .url(location)
                    .build();
            String pagedata = "";

            try {
                Response response = client.newCall(request).execute();
                pagedata = response.body().string();
            } catch (IOException e) {
                errorMsg = "UTilities could not fetch this menu";
                e.printStackTrace();
                cancel(true);
                return null;
            }

            if (pagedata.contains("No Data Available")) {
                foodList.add(new food("", ""));
                listOfLists.add(new MyPair<>("No Food Offered at this Time", foodList));
                return meal;
            } else {
                // have to leave in the lookahead so the regex matches don't
                // overlap
                Pattern catPattern = Pattern
                        .compile(
                                "<div class=\'pickmenucolmenucat\'.*?(?=<div class='pickmenucolmenucat'|</html>)",
                                Pattern.DOTALL);
                Matcher catMatcher = catPattern.matcher(pagedata);
                while (catMatcher.find()) {
                    String categoryData = catMatcher.group();
                    foodList = new ArrayList<>();

                    Pattern catNamePattern = Pattern.compile(">-- (.*?) --<");
                    Matcher catNameMatcher = catNamePattern.matcher(categoryData);
                    if (catNameMatcher.find()) {
                        categories.add(catNameMatcher.group(1));
                    } else {
                        categories.add("Unknown Category");
                    }

                    Pattern nutritionLinkPattern = Pattern.compile("a href=\'(.*?)\'");
                    Matcher nutritionLinkMatcher = nutritionLinkPattern.matcher(categoryData);

                    // This pattern is glitchy on a Nexus S 4G running CM10.1
                    // nightly
                    // Seems to activate Pattern.DOTALL by default. Set flags to
                    // 0 to try and mitigate?
                    Pattern foodPattern = Pattern.compile("<a href=.*?\">(\\w.*?)</a>", 0);
                    Matcher foodMatcher = foodPattern.matcher(categoryData);

                    while (foodMatcher.find() && nutritionLinkMatcher.find()) {
                        foodList.add(new food(foodMatcher.group(1), nutritionLinkMatcher.group(1)));
                    }
                    tempListOfLists.add(new MyPair<>(catNameMatcher.group(1), foodList));
                    if (isCancelled()) {
                        return "";
                    }
                }
            }
            return meal;
        }

        @Override
        protected void onPostExecute(String result) {
            listOfLists.addAll(tempListOfLists);
            mAdapter.notifyDataSetChanged();
            mlv.setVisibility(View.VISIBLE);
            m_pb_ll.setVisibility(View.GONE);
            mell.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            metv.setText(errorMsg);
            mell.setVisibility(View.VISIBLE);
            mlv.setVisibility(View.GONE);
            m_pb_ll.setVisibility(View.GONE);
        }
    }

    class food {
        String name;
        String nutritionLink;

        public food(String name, String nutritionLink) {
            this.name = name;
            this.nutritionLink = nutritionLink;
        }

        public String getName() {
            return name;
        }

        public String getLink() {
            return nutritionLink;
        }
    }

    class MenuAdapter extends StickyHeaderAdapter<food> {

        public MenuAdapter(List<MyPair<String, List<food>>> all) {
            super(all);
        }

        @Override
        public View getAmazingView(int position, View convertView, ViewGroup parent) {
            View res = convertView;
            if (res == null) {
                res = getActivity().getLayoutInflater().inflate(R.layout.menu_item_view, parent,
                        false);
            }

            TextView lName = (TextView) res.findViewById(R.id.lName);

            food f = getItem(position);
            lName.setText(f.name);
            return res;
        }

        @Override
        public void configurePinnedHeader(View header, int position, int alpha) {
            TextView lSectionHeader = (TextView) header;
            lSectionHeader.setText(getSections()[getSectionForPosition(position)]);
        }
    }
}
