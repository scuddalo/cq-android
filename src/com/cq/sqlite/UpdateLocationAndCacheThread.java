package com.cq.sqlite;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;

import com.cq.model.Profile;
import com.cq.overlay.MapLocation;
import com.cq.overlay.MapLocationOverlay;
import com.cq.seek.HomeActivity;
import com.cq.seek.R;
import com.cq.tool.RequestTool;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class UpdateLocationAndCacheThread extends AsyncTask<Location, Void, List<Profile>> {

  WeakReference<HomeActivity> activity;
  WeakReference<MapView> mapView;
  Location location;

  public UpdateLocationAndCacheThread(HomeActivity a, MapView m) {
    activity = new WeakReference<HomeActivity>(a);
    mapView = new WeakReference<MapView>(m);
  }

  @Override
  protected List<Profile> doInBackground (Location... params) {
    location = params[0];
    List<Profile> profiles = new ArrayList<Profile>();
    if (location != null) {
      String lat = Double.toString(location.getLatitude());
      String lng = Double.toString(location.getLongitude());

      // 1) make url
      String host = activity.get().getText(R.string.server_url).toString();
      String urlReq = host + activity.get().getText(R.string.update_loc_url).toString();
      String prefFileName = activity.get().getText(R.string.pref_file_name).toString();

      // 2) get the shared preferences and make params
      SharedPreferences prefs = activity.get().getSharedPreferences(prefFileName, Activity.MODE_PRIVATE);

      SharedPreferences.Editor editor = prefs.edit();
      editor.putString("lat", lat);
      editor.putString("lng", lng);
      editor.commit();

      // 3) make req and deal with the response
      if (prefs.getString("profileId", null) != null && lat != null && lng != null) {
        String profileIdFromPrefs = prefs.getString("profileId", null);
        Map<String, String> reqParams = new HashMap<String, String>();
        reqParams.put("format", "xml");
        reqParams.put("profile_id", profileIdFromPrefs);
        reqParams.put("lat", lat);
        reqParams.put("long", lng);
        reqParams.put("login", prefs.getString("username", null));
        reqParams.put("password", prefs.getString("password", null));

        RequestTool reqTool = RequestTool.getInstance(prefs);
        Document doc = reqTool.makePostRequest(urlReq, reqParams, 200);
        profiles = Profile.constructFromXml(doc);

        //finally, cache the profiles 
        CacheDBAdapter cache = new CacheDBAdapter(activity.get());
        cache.open();
        for(Profile p : profiles) {
          cache.insertProfileDetails(p);
        }
        cache.close();
      }
    }

    return profiles;
  }

  @Override
  protected void onPreExecute () {
    HomeActivity homeActivity = activity.get();
    homeActivity.showRefreshInProgress();
    CacheDBAdapter cache = new CacheDBAdapter(homeActivity);
    List<Profile> profiles = cache.open().getAllProfiles();
    if (profiles != null) {
      // addOverlay (profiles);
    }
    cache.close();
  }

  @Override
  protected void onPostExecute (List<Profile> profiles) {
    if(profiles != null && profiles.size() == 0) {
      Toast.makeText(activity.get(), "No connections found close to you", Toast.LENGTH_SHORT);
    }
    addOverlay(profiles);
    activity.get().showRefreshMapBtn();
  }

  void addOverlay (List<Profile> profiles) {
    int lat = (int) (location.getLatitude() * 1e6);
    int lang = (int) (location.getLongitude() * 1e6);
    GeoPoint point = new GeoPoint(lat, lang);
    mapView.get().getController().animateTo(point);
    List<Overlay> overlays = mapView.get().getOverlays();
    
    overlays.clear();
    List<MapLocation> mapLocations = new ArrayList<MapLocation>();
    for(Profile profile : profiles) {
      mapLocations.add(new MapLocation(profile, activity.get()));
    }

    overlays.add(new MapLocationOverlay(activity.get(), mapLocations));
  }

}
