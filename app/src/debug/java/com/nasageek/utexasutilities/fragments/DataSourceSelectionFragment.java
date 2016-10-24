package com.nasageek.utexasutilities.fragments;

import android.app.Dialog;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.nasageek.utexasutilities.MyBus;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okio.Buffer;
import okio.Okio;

public class DataSourceSelectionFragment extends DialogFragment {

    private String filePath;
    private String webUrl;

    public static DataSourceSelectionFragment newInstance(String path, String webUrl) {
        DataSourceSelectionFragment dssf = new DataSourceSelectionFragment();
        Bundle args = new Bundle();
        args.putString("path", path);
        args.putString("webUrl", webUrl);
        dssf.setArguments(args);
        return dssf;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        filePath = getArguments().getString("path");
        webUrl = getArguments().getString("webUrl");
        // TODO: is it necessary to shutDown the MockWebServer?
        // This is just debug code anyway...
        MockWebServer mockServer = new MockWebServer();
        String[] tmpHtmlFiles;
        AssetManager assets = getActivity().getAssets();
        try {
            tmpHtmlFiles = assets.list(filePath);
        } catch (IOException ioe) {
            tmpHtmlFiles = new String[]{"Failed"};
        }
        List<String> tmp = new ArrayList<>(Arrays.asList(tmpHtmlFiles));
        tmp.add(0, "Web");
        final String[] htmlFiles = tmp.toArray(new String[tmp.size()]);
        return new AlertDialog.Builder(getActivity())
                .setTitle("Set Data Source")
                .setItems(htmlFiles, (dialog, which) -> {
                    String reqUrl;
                    if (which == 0) {
                        reqUrl = webUrl;
                    } else {
                        CountDownLatch cdl = new CountDownLatch(1);
                        Buffer htmlBuffer = new Buffer();
                        try {
                            String[] files = assets.list(filePath);
                            htmlBuffer.writeAll(Okio.source(assets
                                    .open(filePath + "/" + files[which - 1])));
                            new Thread(() -> {
                                mockServer.enqueue(new MockResponse().setBody(htmlBuffer));
                                // Anything else the client tries to load from us should just fail.
                                // 20 chosen arbitrarily.
                                MockResponse badResponse = new MockResponse().setResponseCode(404);
                                for (int i = 0; i < 20; i++) {
                                    mockServer.enqueue(badResponse);
                                }
                                try {
                                    mockServer.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                cdl.countDown();
                            }).start();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        try {
                            cdl.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        reqUrl = mockServer.url("").toString();
                    }
                    MyBus.getInstance().post(new DataSourceSelectedEvent(reqUrl));
                })
                .create();
    }

    public static class DataSourceSelectedEvent {
        public String url;

        public DataSourceSelectedEvent(String url) {
            this.url = url;
        }
    }
}
