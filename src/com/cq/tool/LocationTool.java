package com.cq.tool;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class LocationTool {

  final static String Tag = LocationTool.class.toString();

  public static String getLocality (GeoPoint p, Context ctx) {
    String location = "";
    try {
      Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
      List<Address> addresses = geocoder.getFromLocation(p.getLatitudeE6() * 1e-6, p.getLongitudeE6() * 1e-6, 1);
      Address address = (addresses != null && addresses.size() > 0) ? addresses.get(0) : null;
      StringBuffer result = new StringBuffer();
      if(address != null) {
        result.append(address.getAddressLine(0) != null ? address.getAddressLine(0) : "");
        result.append(", ");
        result.append(address.getAddressLine(1) != null ? address.getAddressLine(1) : address.getLocality());
      }
      location = result.toString();
    }
    catch(IOException e) {
      Log.d(Tag, "Geocoder failed: " + e);
    }

    return location;
  }
}