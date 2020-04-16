package pmf.android.movienfo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Theater implements Parcelable {

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lon")
    private double lon;

    public Theater(){

    }

    public Theater(String id, String name, double lat, double lon){
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    protected Theater(Parcel in){
        id = in.readString();
        name = in.readString();
        lat = in.readDouble();
        lon = in.readDouble();
    }

    public static final Creator<Theater> CREATOR = new Creator<Theater>() {
        @Override
        public Theater createFromParcel(Parcel in) {
            return new Theater(in);
        }

        @Override
        public Theater[] newArray(int size) {
            return new Theater[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
