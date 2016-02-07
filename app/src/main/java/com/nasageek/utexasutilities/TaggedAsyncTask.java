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

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        UTilitiesApplication.getInstance().cacheTask(tag, this);
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        UTilitiesApplication.getInstance().removeCachedTask(tag);
    }

    @Override
    protected void onCancelled(Result result) {
        super.onCancelled(result);
        UTilitiesApplication.getInstance().removeCachedTask(tag);
    }
}
