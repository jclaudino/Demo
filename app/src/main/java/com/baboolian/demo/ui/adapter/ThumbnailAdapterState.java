package com.baboolian.demo.ui.adapter;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ThumbnailAdapterState implements Parcelable {

    private int selectedFilePosition;
    private List<Integer> selectedFilePositions;

    public ThumbnailAdapterState(int selectedFilePosition, List<Integer> selectedFilePositions) {
        this.selectedFilePosition = selectedFilePosition;
        this.selectedFilePositions = selectedFilePositions;
    }

    protected ThumbnailAdapterState(Parcel in) {
        selectedFilePosition = in.readInt();
        selectedFilePositions = new ArrayList<>();
        in.readList(selectedFilePositions, Integer.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(selectedFilePosition);
        dest.writeList(selectedFilePositions);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ThumbnailAdapterState> CREATOR = new Creator<ThumbnailAdapterState>() {
        @Override
        public ThumbnailAdapterState createFromParcel(Parcel in) {
            return new ThumbnailAdapterState(in);
        }

        @Override
        public ThumbnailAdapterState[] newArray(int size) {
            return new ThumbnailAdapterState[size];
        }
    };

    public int getSelectedFilePosition() {
        return selectedFilePosition;
    }

    public List<Integer> getSelectedFilePositions() {
        return selectedFilePositions;
    }
}
