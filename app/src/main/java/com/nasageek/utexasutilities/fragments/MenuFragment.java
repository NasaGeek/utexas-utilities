
package com.nasageek.utexasutilities.fragments;

import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.MyBus;
import com.nasageek.utexasutilities.MyPair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.TaggedAsyncTask;
import com.nasageek.utexasutilities.UTilitiesApplication;
import com.nasageek.utexasutilities.Utility;
import com.nasageek.utexasutilities.activities.NutritionInfoActivity;
import com.nasageek.utexasutilities.adapters.StickyHeaderAdapter;
import com.nasageek.utexasutilities.model.LoadFailedEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuFragment extends Fragment implements AdapterView.OnItemClickListener {
    private List<MyPair<String, List<Food>>> listOfLists = new ArrayList<>();
    private AmazingListView foodListView;
    private LinearLayout progressLayout;
    private TextView errorTextView;
    private View errorLayout;
    private String restId;
    private String title;
    private String TASK_TAG;
    private final UTilitiesApplication mApp = UTilitiesApplication.getInstance();

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

        progressLayout = (LinearLayout) vg.findViewById(R.id.menu_progressbar_ll);
        foodListView = (AmazingListView) vg.findViewById(R.id.menu_listview);
        errorTextView = (TextView) vg.findViewById(R.id.tv_failure);
        errorLayout = vg.findViewById(R.id.menu_error);

        if (restId.equals("0")) {
            return vg;
        }
        updateView(restId, false);
        return vg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restId = savedInstanceState.getString("restid");
            listOfLists = (ArrayList) savedInstanceState.getSerializable("listoflists");
        } else {
            restId = getArguments().getString("restId");
        }
        title = getArguments().getString("title");
        TASK_TAG = getClass().getSimpleName() + restId + title;
    }

    public void updateView(String restId, Boolean update) {
        this.restId = restId;
        foodListView.setPinnedHeaderView(getActivity().getLayoutInflater().inflate(
                R.layout.menu_header_item_view, foodListView, false));
        foodListView.setOnItemClickListener(this);
        if (listOfLists.size() == 0 || update) {
            if (mApp.getCachedTask(TASK_TAG) == null) {
                FetchMenuTask fetchMTask = new FetchMenuTask(TASK_TAG);
                prepareToLoad();
                Utility.parallelExecute(fetchMTask, restId, title);
            }
        } else {
            setupAdapter();
        }
    }

    private void setupAdapter() {
        MenuAdapter mAdapter = new MenuAdapter(getActivity(), listOfLists);
        foodListView.setAdapter(mAdapter);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("listoflists", (ArrayList) listOfLists);
        outState.putString("restid", restId);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String url = "http://hf-food.austin.utexas.edu/foodpro/"
                + ((Food) (parent.getItemAtPosition(position))).nutritionLink;

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        if (sp.getBoolean("embedded_browser", true)) {
            Intent i = new Intent(getActivity(), NutritionInfoActivity.class);
            i.putExtra("url", url);
            i.putExtra("title", ((Food) parent.getItemAtPosition(position)).name);
            startActivity(i);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    static class FetchMenuTask extends TaggedAsyncTask<String, Integer, List<MyPair<String,List<Food>>>> {
        private String errorMsg;
        private final String FETCH_URL =
                "http://hf-food.austin.utexas.edu/foodpro/pickMenu.asp?locationNum=%s&mealName=%s";

        public FetchMenuTask(String tag) {
            super(tag);
        }

        @Override
        protected List<MyPair<String, List<Food>>> doInBackground(String... params) {
            List<Food> foodList;
            List<MyPair<String, List<Food>>> tempListOfLists = new ArrayList<>();
            OkHttpClient client = UTilitiesApplication.getInstance().getHttpClient();
            String restId = params [0];
            String meal = params[1];
            String location;

            // Special case for JCM, which combines Lunch and Dinner
            if (restId.equals("05") && (meal.equals("Lunch") || meal.equals("Dinner"))) {
                location = String.format(FETCH_URL, restId, "Lunch/Dinner");
            } else {
                location = String.format(FETCH_URL, restId, meal);
            }

            Request request = new Request.Builder()
                    .url(location)
                    .build();
            String pagedata;

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
                errorMsg = "No food offered at this time";
                cancel(true);
                return null;
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
                    String category;
                    foodList = new ArrayList<>();

                    Pattern catNamePattern = Pattern.compile(">-- (.*?) --<");
                    Matcher catNameMatcher = catNamePattern.matcher(categoryData);
                    if (catNameMatcher.find()) {
                        category = catNameMatcher.group(1);
                    } else {
                        category = "Unknown Category";
                    }

                    Pattern nutritionLinkPattern = Pattern.compile("a href=\'(.*?)\'");
                    Matcher nutritionLinkMatcher = nutritionLinkPattern.matcher(categoryData);

                    // This pattern is glitchy on a Nexus S 4G running CM10.1 nightly
                    // Seems to activate Pattern.DOTALL by default. Set flags to
                    // 0 to try and mitigate?
                    Pattern foodPattern = Pattern.compile("<a href=.*?\">(\\w.*?)</a>", 0);
                    Matcher foodMatcher = foodPattern.matcher(categoryData);

                    while (foodMatcher.find() && nutritionLinkMatcher.find()) {
                        foodList.add(new Food(foodMatcher.group(1), nutritionLinkMatcher.group(1)));
                    }
                    tempListOfLists.add(new MyPair<>(category, foodList));
                    if (isCancelled()) {
                        return null;
                    }
                }
            }
            return tempListOfLists;
        }

        @Override
        protected void onPostExecute(List<MyPair<String, List<Food>>> listOfLists) {
            super.onPostExecute(listOfLists);
            MyBus.getInstance().post(new LoadSucceededEvent(getTag(), listOfLists));
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            MyBus.getInstance().post(new LoadFailedEvent(getTag(), errorMsg));
        }
    }

    @Subscribe
    public void loadFailed(LoadFailedEvent event) {
        if (TASK_TAG.equals(event.tag)) {
            errorTextView.setText(event.errorMessage);
            progressLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            foodListView.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void loadSucceeded(LoadSucceededEvent event) {
        if (TASK_TAG.equals(event.tag)) {
            listOfLists.clear();
            listOfLists.addAll(event.listOfLists);
            setupAdapter();
            foodListView.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.GONE);
        }
    }

     private void prepareToLoad() {
        progressLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        foodListView.setVisibility(View.GONE);
    }

    static class LoadSucceededEvent {
        public String tag;
        public List<MyPair<String, List<Food>>> listOfLists;

        public LoadSucceededEvent(String tag, List<MyPair<String, List<Food>>> listOfLists) {
            this.tag = tag;
            this.listOfLists = listOfLists;
        }
    }

    static class Food implements Serializable {
        String name;
        String nutritionLink;

        public Food(String name, String nutritionLink) {
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

    static class MenuAdapter extends StickyHeaderAdapter<Food> {

        public MenuAdapter(Context con, List<MyPair<String, List<Food>>> all) {
            super(con, all);
        }

        @Override
        public View getAmazingView(int position, View convertView, ViewGroup parent) {
            View res = convertView;
            if (res == null) {
                res = LayoutInflater.from(mContext).inflate(R.layout.menu_item_view, parent, false);
            }

            TextView lName = (TextView) res.findViewById(R.id.lName);

            Food f = getItem(position);
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
