package com.nasageek.utexasutilities;

public abstract class TaggedAsyncTask<Params, Progress, Result> extends
        AsyncTask<Params, Progress, Result> {

    private final String tag;

    public TaggedAsyncTask(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
