package com.baboolian.demo.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

public class Album implements Parcelable {

    private static final String TAG = Album.class.getSimpleName();

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    private String name;
    private String fileIds;
    private boolean selected;

    public Album(String name, String fileIds) {
        this.name = name;
        this.fileIds = fileIds;
    }

    protected Album(Parcel in) {
        name = in.readString();
        fileIds = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(fileIds);
    }

    public String getName() {
        return name;
    }

    public String getFileIds() {
        return fileIds;
    }

    public int getFileCount() {
        int fileCount = 0;
        try {
            JSONArray fileIdsJSONArray = new JSONArray(fileIds);
            fileCount = fileIdsJSONArray.length();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse file ids into a JSONArray", e);
        }
        return fileCount;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Album)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return getName().equals(((Album) other).getName());
    }

    @Override
    public String toString() {
        return "Album Name: " + name + " contains " + getFileCount() + " files";
    }
}
