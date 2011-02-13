package com.cq.seek;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cq.model.Profile;
import com.cq.model.Seek;
import com.cq.overlay.MapLocation;
import com.cq.overlay.MapLocationOverlay;
import com.cq.sqlite.CacheDBAdapter;
import com.cq.sqlite.UpdateLocationAndCacheThread;
import com.cq.tool.IntentTool;
import com.cq.tool.LocationTool;
import com.cq.tool.RequestTool;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class HomeActivity extends MapActivity {

  final String Tag = HomeActivity.class.toString();

  LocationManager locationManager;
  SeekYouLocationListener locationListener;
  String best;
  MapController mapController;
  MapView mapView;
  List<Profile> friends;
  TransparentPanel popup;
  public Button popupBtn;
  public TextView inviteRespText;
  public EditText statusText;
  public TextView userLocationText;
  Geocoder geocoder;
  ViewSwitcher statusViewSwitcher;
  ViewSwitcher refreshBtnViewSwitcher;

  public HomeActivity() {
  }

  @Override
  public void onCreate (android.os.Bundle icicle) {
    super.onCreate(icicle);

    setContentView(R.layout.home_screen);

    ProgressDialog progressDialog = ProgressDialog.show(this, "", "Loading ...", true);
    progressDialog.show();

    String prefFileName = getText(R.string.pref_file_name).toString();
    final SharedPreferences prefs = getSharedPreferences(prefFileName, MODE_PRIVATE);

    //status text setup
    initStatusViewSwitcher();
    initRefreshBtnViewSwitcher();
    statusText = (EditText) findViewById(R.id.user_status);
    statusText.setText(prefs.getString("status", ""));
    statusText.setOnClickListener(new View.OnClickListener()
    {
      public void onClick (View v) {
        statusViewSwitcher.showNext();

        ImageButton shareBtn = (ImageButton) findViewById(R.id.shareStatusUpdateBtn);
        shareBtn.setOnClickListener(new View.OnClickListener()
        {
          public void onClick (View v) {
            final ViewSwitcher buttonsToProgressBarSwitcher = (ViewSwitcher) findViewById(R.id.buttonsOrProgressBarFlipperInStatusUpdate);
            Thread thread = new Thread(null, new Runnable()
            {
              public void run () {
                final EditText statusTxt = (EditText) findViewById(R.id.expandedStatusTxt);
                updateStatus(statusTxt.getText().toString());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("status", statusTxt.getText().toString());
                runOnUiThread(new Runnable()
                {
                  public void run () {
                    statusText.setText(statusTxt.getText().toString());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(statusText.getWindowToken(), 0);
                    buttonsToProgressBarSwitcher.showPrevious();
                    statusViewSwitcher.showPrevious();
                  }
                });
              }
            }, "MagentoBackground");

            
            buttonsToProgressBarSwitcher.showNext();
            thread.start();

          }
        });
      }
    });

    ImageButton expandedStatusCancelBtn = (ImageButton) findViewById(R.id.cancelStatusUpdateBtn);
    expandedStatusCancelBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick (View v) {
        //show compact mode
        statusViewSwitcher.showPrevious();
      }
    });

    String profileIdFromPrefs = prefs.getString ("profileId", null);
    String[] tmp = profileIdFromPrefs.split("-");
    CacheDBAdapter cache = new CacheDBAdapter(this);
    cache.open();
    Profile profile = cache.getProfile(Integer.parseInt(tmp[0]));
    cache.close();
    Bitmap bitmap = profile.getPhotoLongVersion(this, 40, 40); 
    ImageView imageView = (ImageView) findViewById(R.id.user_photo);
    imageView.setImageBitmap(bitmap);

    //setup the mapview and mapcontroller
    mapView = (MapView) findViewById(R.id.mapView);
    mapController = mapView.getController();

    mapView.setBuiltInZoomControls(true);

    mapView.setReticleDrawMode(MapView.ReticleDrawMode.DRAW_RETICLE_UNDER);

    Location loc = getLastKnownLocation();
    locationListener = new SeekYouLocationListener(this);
    locationManager.requestLocationUpdates(best, 6000, 100, locationListener);

    // update the current location and place markers for people
    // around.
    // and then animate to last known location
    int lat = 0, lang = 0;
    if (loc != null) {
      updateLocataion(loc);
      //           addOverlay (friends);

      lat = (int) (loc.getLatitude() * 1e6);
      lang = (int) (loc.getLongitude() * 1e6);
      GeoPoint point = new GeoPoint(lat, lang);

      if (lat > 0 && lang > 0) {
        mapController.animateTo(point);
      }
    }
    else {
      Log.i(Tag, "null location");
    }

    mapController.setZoom(12);
    mapView.invalidate();

    //set up onclick for seek button 
    ImageButton seekBtn = (ImageButton) this.findViewById(R.id.seekBtn);
    seekBtn.setOnClickListener(seekYouButtonListener);

    //set up onclick for msg button
    ImageButton msgBtn = (ImageButton) this.findViewById(R.id.msgBtn);
    msgBtn.setOnClickListener(IntentTool.createOnclickListener(this, MessagesListActivity.class, null));

    progressDialog.dismiss();
  }


  @Override
  protected boolean isRouteDisplayed () {
    return false;
  }

  private void dumpLocation (Location l) {
    if (l == null) {
      Log.i(Tag, "Null Location");
    }
    else {
      Log.i(Tag, "latitude: " + l.getLatitude() + " Longtitude: " + l.getLongitude());
    }
  }

  @Override
  protected void onResume () {
    super.onResume();
    // Start updates (doc recommends delay >= 60000 ms)
    locationManager.requestLocationUpdates(best, 15000, 1, locationListener);
  }

  @Override
  protected void onPause () {
    super.onPause();
    // Stop updates to save power while app paused
    locationManager.removeUpdates(locationListener);
  }

  void updateLocataion (Location location) {
    /**
     * // 1) make url String host = getText (R.string.server_url).toString ();
     * String urlReq = host + getText (R.string.update_loc_url).toString ();
     * String prefFileName = getText (R.string.pref_file_name).toString (); //
     * 2) get the shared preferences and make params SharedPreferences prefs =
     * getSharedPreferences (prefFileName, MODE_PRIVATE);
     * SharedPreferences.Editor editor = prefs.edit (); editor.putString ("lat",
     * lat.toString ()); editor.putString ("lng", lng.toString ());
     * editor.commit (); List<Profile> profiles = new ArrayList<Profile> (); //
     * 3) make req and deal with the response if (prefs.getString ("profileId",
     * null) != null && lat != null && lng != null) { String profileIdFromPrefs
     * = prefs.getString ("profileId", null); Map<String, String> reqParams =
     * new HashMap<String, String> (); reqParams.put ("format", "xml");
     * reqParams.put ("profile_id", profileIdFromPrefs); reqParams.put ("lat",
     * lat.toString ()); reqParams.put ("long", lng.toString ()); reqParams.put
     * ("login", prefs.getString ("username", null)); reqParams.put ("password",
     * prefs.getString ("password", null)); RequestTool reqTool =
     * RequestTool.getInstance (this .getSharedPreferences (prefFileName,
     * MODE_PRIVATE)); Document doc = reqTool.makePostRequest (urlReq,
     * reqParams, 200); profiles = Profile.constructFromXml (doc); }
     **/
    new UpdateLocationAndCacheThread(this, mapView).execute(location);

    userLocationText = (TextView) findViewById(R.id.user_location);
    GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1e6), (int) (location.getLongitude() * 1e6));
    String locationString = LocationTool.getLocality(point, getApplicationContext());
    userLocationText.setText((locationString != null && locationString.length() > 0 ? locationString : "<unknown>"));
  }

  public String invite (Profile toProfile) {
    if (toProfile != null) {
      // 1) get the profile-id string from prefFile
      String prefFileName = getText(R.string.pref_file_name).toString();
      SharedPreferences prefs = getSharedPreferences(prefFileName, MODE_PRIVATE);

      String toProfileIdStr = toProfile.getProfileIdNameString();

      // 2) make url http://<host>/people/<profile-id>/whos_around.xml
      String host = getText(R.string.server_url).toString();
      String url = host + getText(R.string.invite_url).toString().replaceAll("#\\{profile_id\\}", toProfileIdStr);

      RequestTool reqTool = RequestTool.getInstance(prefs);
      HashMap<String, String> params = new HashMap<String, String>();
      params.put("message_id", toProfileIdStr);
      Document doc = reqTool.makePostRequest(url, params, 200);
      if (doc != null) return "Invitation successfully sent to " + toProfileIdStr;
    }

    return "Request Failed";
  }

  private ArrayList<MapLocation> mapLocations;

  public ArrayList<MapLocation> getMapLocations () {
    return mapLocations;
  }

  void addOverlay (List<Profile> profiles) {
    List<Overlay> overlays = mapView.getOverlays();
    for(Profile profile : profiles) {
      Log.i(HomeActivity.class.toString(), profile.getUser().getLogin() + " is a " + ((profile.isFriend() ? "friend" : "stranger") + " at " + profile.getLocation()));
      if (profile.isFriend()) {
        if (mapLocations == null) {
          mapLocations = new ArrayList<MapLocation>();
        }
        mapLocations.add(new MapLocation(profile, this));
      }
    }

    overlays.add(new MapLocationOverlay(this, mapLocations));
  }

  ViewSwitcher mapPopupViewSwitcher;

  public ViewSwitcher mapPopup () {
    if (mapPopupViewSwitcher == null) {
      LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
      mapPopupViewSwitcher = (ViewSwitcher) inflater.inflate(R.layout.map_popup, null);
    }

    return mapPopupViewSwitcher;
  }

  public void updateStatus (String status) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("status", status);

    SharedPreferences prefs = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE);
    String profileIdString = prefs.getString("profileId", "");
    if (profileIdString != null) {
      String[] tokens = profileIdString.split("-");
      String profileId = tokens[0];
      String statusUpdateUrl = getString(R.string.server_url) + getString(R.string.update_status_url).replaceAll("#\\{profile_id_num\\}", profileId);
      ;
      RequestTool.getInstance(prefs).makePostRequest(statusUpdateUrl, params, 200);
    }
  }

  OnClickListener seekBtnOnClick1 = new OnClickListener()
  {

    public void onClick (View v) {
      Thread t = new Thread()
      {
        @Override
        public void run () {
          Intent gotoSeekActivity1 = new Intent(HomeActivity.this, WhoToSeekActivity.class);
          ArrayList<String> profileNames = new ArrayList<String>();
          ArrayList<Integer> profileIds = new ArrayList<Integer>();
          int ii = 0;
          for(Profile p : friends) {
            profileNames.add(p.getUser().getLogin());
            profileIds.add(p.getId());
            ii++;
          }
          gotoSeekActivity1.putStringArrayListExtra("profileNames", profileNames);
          gotoSeekActivity1.putIntegerArrayListExtra("profileIds", profileIds);
          HomeActivity.this.startActivity(gotoSeekActivity1);
        }
      };
      t.start();
    }

  };

  public OnClickListener getInviteBtnOnClickLstnr (final Profile selectedProfile) {
    return new OnClickListener()
    {
      public void onClick (View v) {
        popupBtn.setVisibility(View.GONE);
        String msg = HomeActivity.this.invite(selectedProfile);
        inviteRespText.setText(msg);
        inviteRespText.setVisibility(View.VISIBLE);
      }
    };
  }

  public class SeekYouLocationListener implements LocationListener {
    HomeActivity activity;

    public SeekYouLocationListener(HomeActivity a) {
      this.activity = a;
    }

    public void onLocationChanged (Location location) {
      dumpLocation(location);
      if (location != null) {
        // animate map to that point
        GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1e6), (int) (location.getLongitude() * 1e6));
        mapController.animateTo(point);

        // now update the user location and a list of who's around
        updateLocataion(location);
        //	            addOverlay (profiles);

        mapController.setZoom(12);
        mapView.invalidate();
      }
    }

    public void onProviderDisabled (String provider) {
      Log.d(Tag, "provider " + provider + " disabled");
    }

    public void onProviderEnabled (String provider) {
      Log.d(Tag, "provider " + provider + " enabled");
    }

    public void onStatusChanged (String provider, int status, Bundle extras) {
      Log.d(Tag, "\nProvider status changed: " + provider + ", status=" + status + ", extras=" + extras);
    }

  }

  View.OnFocusChangeListener statusFocusChangeWatcher = new View.OnFocusChangeListener()
  {

    public void onFocusChange (View v, boolean hasFocus) {
      if (hasFocus) {
        statusViewSwitcher.showNext();
      }

    }
  };

  View.OnClickListener seekYouButtonListener = new OnClickListener()
  {
    public void onClick (View v) {
      SharedPreferences prefs = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE);
      String profileIdString = prefs.getString("profileId", "");
      String[] tokens = profileIdString.split("-");
      String profileId = tokens[0];
      String activeSeekUrl = getString(R.string.server_url) + getString(R.string.active_seek_for_profile).replaceAll("#\\{profile_id_num\\}", profileId);
      Document doc = RequestTool.getInstance(prefs).makeGetRequest(activeSeekUrl, null, 200);
      List<Seek> seeks = Seek.constructFromXml(doc);
      Seek activeSeek = seeks != null && seeks.size() > 0 ? seeks.get(0) : null;
      Intent result = null;
      if (activeSeek != null) {
        try {
          result = new Intent(HomeActivity.this, ManageSeekRequestActivity.class);
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          ObjectOutputStream oos;
          oos = new ObjectOutputStream(bos);
          oos.writeObject(activeSeek);
          oos.flush();
          oos.close();
          bos.close();
          byte[] activeSeekBytes = bos.toByteArray();
          result.putExtra("activeSeek", activeSeekBytes);
          result.putExtra("seekId", activeSeek.getId());
        }
        catch(IOException e) {
          Log.e(Tag, "error serializing activeSeek", e);
          e.printStackTrace();
        }
      }
      else {
        result = new Intent(HomeActivity.this, WhoToSeekActivity.class);
      }

      HomeActivity.this.startActivity(result);
    }
  };

  /**
   * Convenience method to initialize viewSwither
   */
  void initStatusViewSwitcher () {
    statusViewSwitcher = (ViewSwitcher) findViewById(R.id.status_viewswitcher);
    statusViewSwitcher.setDisplayedChild(0);
  }
  

  void initRefreshBtnViewSwitcher () {
    refreshBtnViewSwitcher = (ViewSwitcher) findViewById(R.id.refresh_btn_viewswitcher);
    refreshBtnViewSwitcher.setDisplayedChild(0);
  }
  
  public void showRefreshMapBtn() {
    refreshBtnViewSwitcher.showPrevious();
  }
  
  public void showRefreshInProgress() {
    refreshBtnViewSwitcher.showNext();
  }
  
  public Location getLastKnownLocation() {
    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    Criteria criteria = new Criteria();
    best = locationManager.getBestProvider(criteria, true);
    Log.i(Tag, "Best provider is " + best);
    return locationManager.getLastKnownLocation(best);
  }
}
