package com.cq.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;

import com.cq.model.Profile;
import com.cq.seek.ProfileActivity;
import com.cq.seek.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class MapOverlay extends com.google.android.maps.Overlay {
  private GeoPoint point;
  private Profile profile;
  private MapActivity homeActivity;

  public MapOverlay(MapActivity activity, Profile p) {
    homeActivity = activity;
    profile = p;

    // calculate the geo point from the
    // profile objects location
    int lat = (int) (p.getLocation().getLatitude().doubleValue() * 1e6);
    int lng = (int) (p.getLocation().getLongitude().doubleValue() * 1e6);
    point = new GeoPoint(lat, lng);
  }

  @Override
  public boolean draw (Canvas canvas, MapView mapView, boolean shadow, long when) {
    super.draw(canvas, mapView, shadow);

    // ---translate the GeoPoint to screen pixels---
    Point screenPts = new Point();
    mapView.getProjection().toPixels(point, screenPts);

    // ---add the marker---
    Bitmap bmp = BitmapFactory.decodeResource(homeActivity.getResources(), R.drawable.pushpin_red);
    canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 20, null);
    return true;
  }

  @Override
  public boolean onTap (GeoPoint p, MapView mapView) {
    Intent goToProfileIntent = new Intent(homeActivity, ProfileActivity.class);
    goToProfileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //        goToProfileIntent.putExtra ("profile", profile);
    homeActivity.startActivity(goToProfileIntent);
    return true;
  }
}