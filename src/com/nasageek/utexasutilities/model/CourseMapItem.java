
package com.nasageek.utexasutilities.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class CourseMapItem implements Serializable, Parcelable {

    private static final long serialVersionUID = 1L;

    private String name, viewUrl, contentId, linkType;
    private boolean blackboardItem;

    public static Parcelable.Creator<CourseMapItem> CREATOR = new Parcelable.Creator<CourseMapItem>() {

        @Override
        public CourseMapItem createFromParcel(Parcel source) {
            return new CourseMapItem(source);
        }

        @Override
        public CourseMapItem[] newArray(int size) {
            return new CourseMapItem[size];
        }

    };

    public CourseMapItem(String name, String viewUrl, String contentId, String linkType) {
        this.name = name;
        this.viewUrl = viewUrl;
        this.contentId = contentId;
        this.linkType = linkType;
    }

    public CourseMapItem(Parcel source) {
        name = source.readString();
        viewUrl = source.readString();
        contentId = source.readString();
        linkType = source.readString();
    }

    public String getName() {
        return name;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public String getContentId() {
        return contentId;
    }

    public String getLinkType() {
        return linkType;
    }

    public boolean isBlackboardItem() {
        return blackboardItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(viewUrl);
        dest.writeString(contentId);
        dest.writeString(linkType);

    }
}
