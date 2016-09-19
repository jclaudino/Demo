package com.baboolian.demo.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaStoreFile implements Parcelable {

    private static final String TAG = MediaStoreFile.class.getSimpleName();

    public static final Creator<MediaStoreFile> CREATOR = new Creator<MediaStoreFile>() {
        @Override
        public MediaStoreFile createFromParcel(Parcel in) {
            return new MediaStoreFile(in);
        }

        @Override
        public MediaStoreFile[] newArray(int size) {
            return new MediaStoreFile[size];
        }
    };

    private long id;
    private String dataUri;
    private int mediaType;
    private int width;
    private int height;
    private boolean selected;

    public MediaStoreFile(long id, String dataUri, int mediaType, int width, int height) {
        this.id = id;
        this.dataUri = dataUri;
        this.mediaType = mediaType;
        this.width = width;
        this.height = height;
        this.selected = false;
    }

    protected MediaStoreFile(Parcel in) {
        id = in.readLong();
        dataUri = in.readString();
        mediaType = in.readInt();
        width = in.readInt();
        height = in.readInt();
        selected = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(dataUri);
        parcel.writeInt(mediaType);
        parcel.writeInt(width);
        parcel.writeInt(height);
        parcel.writeByte((byte) (selected ? 1 : 0));
    }

    public long getId() {
        return id;
    }

    public String getDataUri() {
        return dataUri;
    }

    public int getMediaType() {
        return mediaType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof MediaStoreFile)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return getDataUri().equals(((MediaStoreFile) other).getDataUri());
    }

    @Override
    public String toString() {
        return "id: " + id + " dataUri: " + dataUri  + " mediaType: " + mediaType;
    }
}
