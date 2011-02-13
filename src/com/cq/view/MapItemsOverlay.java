package com.cq.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.animation.Animation;

import com.cq.model.Profile;
import com.cq.seek.HomeActivity;
import com.cq.tool.StringTool;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapItemsOverlay extends ItemizedOverlay<OverlayItem> {
  List<OverlayItem> items = new ArrayList<OverlayItem>();
  List<Profile> profiles = new ArrayList<Profile>();
  Drawable marker = null;
  HomeActivity activity = null;
  Animation animShow, animHide;
  Geocoder geocoder = null;
  Profile selectedProfile = null;

  private static final String Tag = MapItemsOverlay.class.toString();

  //
  //    public MapItemsOverlay(Drawable defaultMarker)
  //    {
  //        super (defaultMarker);
  //        
  //        this.marker = defaultMarker;
  //    }

  public MapItemsOverlay(Drawable defaultMarker, List<Profile> profiles, HomeActivity activity) {
    super(defaultMarker);

    this.marker = defaultMarker;
    this.profiles = profiles;
    for(Profile p : profiles) {
      int lat = (int) (p.getLocation().getLatitude().doubleValue() * 1e6);
      int lng = (int) (p.getLocation().getLongitude().doubleValue() * 1e6);
      GeoPoint point = new GeoPoint(lat, lng);
      String display = StringTool.getNonNullString(p.getDisplayName(), p.getUser().getLogin());
      String status = (p.getStatus() != null) ? display + " - " + p.getStatus() : display;
      items.add(new OverlayItem(point, display, status));
    }

    this.activity = activity;
    this.geocoder = new Geocoder(activity.getApplicationContext(), Locale.getDefault());

    populate();

  }

  void showPopup (int position) {
    selectedProfile = null;

    /**
     * // get the corresponding items from the collections selectedProfile =
     * profiles.get (position); OverlayItem item = items.get (position); final
     * TransparentPanel popup = (TransparentPanel) activity .findViewById
     * (R.id.popup_window); // startout with popup initially hidden
     * popup.setVisibility (View.GONE); animShow = AnimationUtils.loadAnimation
     * (activity, R.anim.popup_show); animHide = AnimationUtils.loadAnimation
     * (activity, R.anim.popup_show); final TextView profileName = (TextView)
     * activity.findViewById (R.id.popup_profile_name); GeoPoint point =
     * item.getPoint (); String location = getLocality (point); String
     * displayStr = item.getSnippet () + ((location != null) ? " at " +
     * location: ""); profileName.setText (displayStr); // show the btn and hide
     * the response text first. activity.popupBtn.setVisibility (View.VISIBLE);
     * activity.inviteRespText.setVisibility (View.GONE); if
     * (selectedProfile.isFriend ()) { //set appropriate btn text
     * activity.popupBtn.setText ("Seek"); //create extras for intent
     * ArrayList<String> friendsDetails = new ArrayList<String> ();
     * friendsDetails.add (selectedProfile.getId () + "," +
     * selectedProfile.getUser ().getLogin () + "," +
     * selectedProfile.getDistance ()); Bundle extras = new Bundle ();
     * extras.putStringArrayList ("friendsDetails", friendsDetails); //setup
     * onclicklistener activity.popupBtn.setOnClickListener
     * (IntentTool.createOnclickListener ( activity, SeekActivity.class,
     * extras)); } else { // set teh appropriate text and setup onclicklstnr
     * activity.popupBtn.setText ("Invite");
     * activity.popupBtn.setOnClickListener (activity.getInviteBtnOnClickLstnr
     * (selectedProfile)); } popup.setVisibility(View.VISIBLE);
     * popup.startAnimation( animShow );
     **/
  }

  @Override
  protected OverlayItem createItem (int ii) {
    return items.get(ii);
  }

  @Override
  public void draw (Canvas canvas, MapView mapView, boolean shadow) {
    super.draw(canvas, mapView, shadow);
    boundCenterBottom(marker);
  }

  @Override
  public int size () {
    return items.size();
  }

  @Override
  protected boolean onTap (int ii) {
    // display a toast
    //String displayStr = items.get (ii).getSnippet () + ((location != null) ? " at " + location: "");
    //Toast.makeText (activity, displayStr,
    //        Toast.LENGTH_LONG).show ();

    // show popup
    showPopup(ii);

    // show profileActivity
    //        Intent goToProfileIntent = new Intent (activity, ProfileActivity.class);
    //        goToProfileIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
    //        goToProfileIntent.putExtra ("profile", activity.friends.get (ii));
    //        activity.startActivity (goToProfileIntent);

    return true;
  }

  String getLocality (GeoPoint p) {
    String location = "";
    try {
      List<Address> addresses = geocoder.getFromLocation(p.getLatitudeE6() * 1e-6, p.getLongitudeE6() * 1e-6, 1);
      Address address = (addresses != null && addresses.size() > 0) ? addresses.get(0) : null;
      location = address.getLocality();
    }
    catch(IOException e) {
      Log.d(Tag, "Geocoder failed: " + e);
    }

    return location;
  }

}
