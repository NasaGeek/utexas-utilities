package com.nasageek.utexasutilities.model;

public class LoadFailedEvent {
    public String tag;
    public CharSequence errorMessage;

    public LoadFailedEvent(String tag, CharSequence msg) {
        this.tag = tag;
        errorMessage = msg;
    }
}

