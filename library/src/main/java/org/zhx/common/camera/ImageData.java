package org.zhx.common.camera;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


public class ImageData implements Parcelable {
    private long id;
    private String displayName;
    private long dateAdded;
    private Uri contentUri;
    private byte[] datas;

    public ImageData() {
    }

    public ImageData(Uri contentUri, byte[] datas) {
        this.contentUri = contentUri;
        this.datas = datas;
    }

    public ImageData(Uri contentUri) {
        this.contentUri = contentUri;

    }

    protected ImageData(Parcel in) {
        id = in.readLong();
        displayName = in.readString();
        dateAdded = in.readLong();
        datas = in.createByteArray();
        contentUri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(displayName);
        dest.writeLong(dateAdded);
        dest.writeByteArray(datas);
        dest.writeParcelable(contentUri, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageData> CREATOR = new Creator<ImageData>() {
        @Override
        public ImageData createFromParcel(Parcel in) {
            return new ImageData(in);
        }

        @Override
        public ImageData[] newArray(int size) {
            return new ImageData[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }
}
