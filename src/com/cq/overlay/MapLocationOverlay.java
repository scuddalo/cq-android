package com.cq.overlay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.opengl.Visibility;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cq.seek.ContactDetailsActivity;
import com.cq.seek.HomeActivity;
import com.cq.seek.R;
import com.cq.seek.ScreenWrapper;
import com.cq.tool.GeoTools;
import com.cq.tool.ImageTool;
import com.cq.tool.LocationTool;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapLocationOverlay extends Overlay {

  final String Tag = MapLocationOverlay.class.toString();

  // Store these as global instances so we don't keep reloading every time
  private Bitmap bubbleIcon;

  private HomeActivity mapLocationViewer;

  private Paint innerPaint, borderPaint, textPaint;

  // The currently selected Map Location...if any is selected. This tracks
  // whether an information
  // window should be displayed & where...i.e. whether a user 'clicked' on a
  // known map location
  private MapLocation selectedMapLocation;
  GeoPoint popupWindowPosition;

  Bitmap photo;
  List<MapLocation> mapLocations = new ArrayList<MapLocation>();
  String locality;

  public MapLocationOverlay(HomeActivity mapLocationViewer, List<MapLocation> mapLocs) {

    this.mapLocationViewer = mapLocationViewer;
    this.mapLocations = mapLocs;

    bubbleIcon = BitmapFactory.decodeResource(mapLocationViewer.getResources(), R.drawable.cq_red_map_pin);

  }

  @Override
  public boolean onTap (GeoPoint p, MapView mapView) {

    // Store whether prior popup was displayed so we can call invalidate() &
    // remove it if necessary.
    boolean isRemovePriorPopup = selectedMapLocation != null;

    // Next test whether a new popup should be displayed
    selectedMapLocation = getHitMapLocation(mapView, p);
    if (isRemovePriorPopup || selectedMapLocation != null) {
      hideAllPopups();

      Rect rect = null;

      Set<Entry<RectF, Rect>> result = infoWindowRectangle(mapView).entrySet();
      if (result.size() > 0) {
        for(Entry<RectF, Rect> entry : result) {
          rect = entry.getValue();
          mapView.invalidate(rect);
          break;
        }
      }
      else {
        mapView.invalidate();
      }

      photo = null;
      locality = null;
    }

    // Lastly return true if we handled this onTap()
    return selectedMapLocation != null;
  }

  @Override
  public void draw (Canvas canvas, MapView mapView, boolean shadow) {
    drawMapLocations(canvas, mapView, shadow);
    //drawInfoWindow (canvas, mapView, shadow);
    drawPopupWindow(canvas, mapView);
  }

  /**
   * Test whether an information balloon should be displayed or a prior balloon
   * hidden.
   */
  private MapLocation getHitMapLocation (MapView mapView, GeoPoint tapPoint) {

    // Track which MapLocation was hit...if any
    MapLocation hitMapLocation = null;

    RectF hitTestRecr = new RectF();
    Point screenCoords = new Point();
    Iterator<MapLocation> iterator = mapLocations.iterator();
    while(iterator.hasNext()) {
      MapLocation testLocation = iterator.next();

      // Translate the MapLocation's lat/long coordinates to screen
      // coordinates
      mapView.getProjection().toPixels(testLocation.getPoint(), screenCoords);

      // Create a 'hit' testing Rectangle w/size and coordinates of our
      // icon
      // Set the 'hit' testing Rectangle with the size and coordinates of
      // our on screen icon
      hitTestRecr.set(-bubbleIcon.getWidth() / 2, -bubbleIcon.getHeight(), bubbleIcon.getWidth() / 2, 0);
      hitTestRecr.offset(screenCoords.x, screenCoords.y);

      // Finally test for a match between our 'hit' Rectangle and the
      // location clicked by the user
      mapView.getProjection().toPixels(tapPoint, screenCoords);
      if (hitTestRecr.contains(screenCoords.x, screenCoords.y)) {
        hitMapLocation = testLocation;
        break;
      }
    }

    // Lastly clear the newMouseSelection as it has now been processed
    tapPoint = null;

    return hitMapLocation;
  }

  private void drawMapLocations (Canvas canvas, MapView mapView, boolean shadow) {
    Iterator<MapLocation> iterator = mapLocations.iterator();
    Point screenCoords = new Point();
    while(iterator.hasNext()) {
      MapLocation location = iterator.next();
      mapView.getProjection().toPixels(location.getPoint(), screenCoords);
      canvas.drawBitmap(bubbleIcon, screenCoords.x - bubbleIcon.getWidth() / 2, screenCoords.y - bubbleIcon.getHeight(), null);
    }
  }

  int INFO_WINDOW_WIDTH = 200;
  int INFO_WINDOW_HEIGHT = 30;

  private Map<RectF, Rect> infoWindowRectangle (MapView mapView) {
    RectF infoWindowRectF = null;
    Rect infoWindowRect = null;
    Map<RectF, Rect> result = new HashMap<RectF, Rect>();

    if (selectedMapLocation != null) {
      // First determine the screen coordinates of the selected
      // MapLocation
      Point selDestinationOffset = new Point();
      mapView.getProjection().toPixels(selectedMapLocation.getPoint(), selDestinationOffset);

      // Setup the info window with the right size & location
      infoWindowRectF = new RectF(0, 0, INFO_WINDOW_WIDTH, INFO_WINDOW_HEIGHT);
      infoWindowRect = new Rect(0, 0, INFO_WINDOW_WIDTH, INFO_WINDOW_HEIGHT);

      int infoWindowOffsetX = selDestinationOffset.x - INFO_WINDOW_WIDTH / 2;
      int infoWindowOffsetY = selDestinationOffset.y - INFO_WINDOW_HEIGHT - bubbleIcon.getHeight();
      infoWindowRectF.offset(infoWindowOffsetX, infoWindowOffsetY);
      infoWindowRect.offset(infoWindowOffsetX, infoWindowOffsetY);
      result.put(infoWindowRectF, infoWindowRect);
    }

    return result;
  }

  private void drawPopupWindow (Canvas canvas, MapView mapView) {
    if (selectedMapLocation != null) {
      //mapView.getController ().setCenter (selectedMapLocation.getPoint ());

      // 1) Use the mapview's Projection object to convert a geo-location to screen 
      // coordinates
      Point selectedDestinationOffset = new Point();
      mapView.getProjection().toPixels(selectedMapLocation.getPoint(), selectedDestinationOffset);

      // 2) Use you're overlay's marker's image height to find it's top 
      int left = selectedDestinationOffset.x;
      int top = selectedDestinationOffset.y - (bubbleIcon.getHeight()/2);

      // 3) Use the Projection again to covert that screen location back to 
      // geo-locations
      // Note: populate it in an ivar so that onclick handlers can get to the 
      // last remembered popupWindowPosition
      popupWindowPosition = mapView.getProjection().fromPixels(left, top);

      // 4) Use MapView.LayoutParams to set your view's location to that specific 
      // point, aligned by bottom-center
      setupMapPopup(mapView); // ...so that its cached
      MapView.LayoutParams layoutPrarams = new  MapView.LayoutParams(MapPopup.Width, 70, popupWindowPosition, MapView.LayoutParams.BOTTOM_CENTER);
      mapPopup.container.setLayoutParams(layoutPrarams);
      showCompactPopup();
      populatePopupViewWithDetailsFromSelectedLocation();
    }
  }

  MapPopup mapPopup;
  public void setupMapPopup(MapView mapView) {
    if(mapPopup == null) {
      mapPopup = new MapPopup();
      
      mapPopup.mapView = mapView;
      
      mapPopup.container = (RelativeLayout) View.inflate(mapLocationViewer, R.layout.map_popup, null);
      mapPopup.firstRowImage = (ImageView) mapPopup.container.findViewById(R.id.mapPopupPhoto);
      mapPopup.firstRowLocationTxt = (TextView) mapPopup.container.findViewById(R.id.mapPopupCompactModeLocationDetailText);
      mapPopup.firstRowSwitchModeBtn = (ImageButton) mapPopup.container.findViewById(R.id.mapPopupCompactModeExpandButton);
      mapPopup.firstRowSwitchModeBtn.setOnClickListener(new View.OnClickListener()
      {
        
        public void onClick (View v) {
          Intent ii = new Intent(mapLocationViewer, ContactDetailsActivity.class);
          ii.putExtra("selectedProfileId", selectedMapLocation.getProfile().getId());
          ii.putExtra("replaceHomeBtnWithBackBtn", true);
          mapLocationViewer.startActivity(ii);
        }
      });
      
      mapView.addView(mapPopup.container);
    }
  }

  /**
   * hides the popup view switcher and its direct children
   * <p>
   * assumes that setupPopup is called prior to this.
   */
  public void hideAllPopups() {
    if(mapPopup != null) {
      mapPopup.container.setVisibility(View.GONE);
    }
  }
  
  /**
   * shows the popup view switcher 
   * <p>
   * assumes that setupPopup is called prior to this.
   */
  public void showCompactPopup() {
    if(mapPopup != null) {
      mapPopup.container.setVisibility(View.VISIBLE);
    }
  }
  
  
  /**
   * populates the selected iVars of MapPopups (depending on the input <code>compactMode</code>)
   * with the details from selectedMapLocation  
   * @param compactMode boolean 
   */
  void populatePopupViewWithDetailsFromSelectedLocation() {
    if(selectedMapLocation != null && mapPopup != null) {
      if(photo == null) {
        photo = selectedMapLocation.getProfile().getPhotoLongVersion(mapLocationViewer, -1, -1);
      }
      mapPopup.firstRowImage.setImageBitmap(photo);
      if(locality == null) {
        locality = selectedMapLocation.getName() + " at " + LocationTool.getLocality(selectedMapLocation.getPoint(), mapLocationViewer.getApplicationContext());
      }
      mapPopup.firstRowLocationTxt.setText(locality);
    }
  }
  
  /**
   * Convenience class to group & cache views in the map_popup.xml layout
   * @author santoash
   *
   */
  class MapPopup {
    public static final int Width = 200;
    MapView mapView;
    
    RelativeLayout container;
    ImageView firstRowImage;
    TextView firstRowLocationTxt;
    ImageButton firstRowSwitchModeBtn;
  }
}