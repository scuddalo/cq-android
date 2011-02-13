package com.cq.overlay;

import java.io.Serializable;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cq.model.Profile;
import com.cq.seek.R;
import com.cq.tool.LocationTool;
import com.cq.tool.RequestTool;
import com.google.android.maps.GeoPoint;

/** Class to hold our location information */
public class MapLocation implements Serializable {

  private static final long serialVersionUID = -3207306486148764944L;
  private GeoPoint point;
  private Profile profile;
  private transient Activity mapActivity;
  private String locality;
  private Bitmap photo;

  final String Tag = MapLocation.class.toString();

  public MapLocation(Profile p, Activity mapActivity) {
    profile = p;
    this.mapActivity = mapActivity;

    double latitude = p.getLocation().getLatitude().doubleValue();
    double longitude = p.getLocation().getLongitude().doubleValue();
    point = new GeoPoint((int) (latitude * 1e6), (int) (longitude * 1e6));
  }

  public int getTier () {
    int result = 3;
    if (profile != null) {
      result = profile.getTier();
    }
    return result;
  }

  public GeoPoint getPoint () {
    return point;
  }

  public String getName () {
    return profile.displayNameOrLogin();
  }

  public String getLocality () {
    if (locality == null && mapActivity != null) {
      locality = LocationTool.getLocality(getPoint(), mapActivity.getApplicationContext());
    }

    return locality;
  }

  public void setLocality (String s) {
    this.locality = s;
  }
  
  public void setPhoto (Bitmap p) {
    this.photo = p;
  }

  public SharedPreferences prefs () {
    if (mapActivity != null) {
      String prefFileName = mapActivity.getText(R.string.pref_file_name).toString();
      return mapActivity.getSharedPreferences(prefFileName, Activity.MODE_PRIVATE);
    }

    return null;
  }

  @Override
  public int hashCode () {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((point == null) ? 0 : point.hashCode());
    result = prime * result + ((profile == null) ? 0 : profile.hashCode());
    return result;
  }

  @Override
  public boolean equals (Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MapLocation other = (MapLocation) obj;
    if (point == null) {
      if (other.point != null) return false;
    }
    else if (!point.equals(other.point)) return false;
    if (profile == null) {
      if (other.profile != null) return false;
    }
    else if (!profile.equals(other.profile)) return false;
    return true;
  }

  public int describeContents () {
    return 0;
  }

  public Profile getProfile () {
    return profile;
  }
}
