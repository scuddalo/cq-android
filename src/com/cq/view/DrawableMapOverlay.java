package com.cq.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class DrawableMapOverlay extends Overlay {

  private static final double MAX_TAP_DISTANCE_KM = 3;
  // Rough approximation - one degree = 50 nautical miles
  private static final double MAX_TAP_DISTANCE_DEGREES = MAX_TAP_DISTANCE_KM * 0.5399568 * 50;

  private final GeoPoint geoPoint;
  private final Context context;
  private final int drawable;
  private Canvas canvas;
  private Bitmap markerImage;

  /**
   * @param context
   *          the context in which to display the overlay
   * @param geoPoint
   *          the geographical point where the overlay is located
   * @param drawable
   *          the ID of the desired drawable
   */
  public DrawableMapOverlay(Context context, GeoPoint geoPoint, int drawable) {
    this.context = context;
    this.geoPoint = geoPoint;
    this.drawable = drawable;
  }

  @Override
  public boolean draw (Canvas canvas, MapView mapView, boolean shadow, long when) {
    super.draw(canvas, mapView, shadow);

    // hold on to the canvas for later use
    this.canvas = canvas;

    // Convert geo coordinates to screen pixels
    Point screenPoint = new Point();
    mapView.getProjection().toPixels(geoPoint, screenPoint);

    // Read the image
    markerImage = BitmapFactory.decodeResource(context.getResources(), drawable);

    // Draw it, centered around the given coordinates
    canvas.drawBitmap(markerImage, screenPoint.x - markerImage.getWidth() / 2, screenPoint.y - markerImage.getHeight() / 2, null);
    return true;
  }

  @Override
  public boolean onTap (GeoPoint p, MapView mapView) {
    // Handle tapping on the overlay here
    if (canvas == null) {
      canvas = new Canvas();
    }

    drawInfoWindow(canvas, mapView, false);

    return true;
  }

  private void drawInfoWindow (Canvas canvas, MapView mapView, boolean shadow) {
    // First determine the screen coordinates of the selected MapLocation
    Point selDestinationOffset = new Point();
    mapView.getProjection().toPixels(geoPoint, selDestinationOffset);

    // Setup the info window with the right size & location
    int INFO_WINDOW_WIDTH = 125;
    int INFO_WINDOW_HEIGHT = 25;
    RectF infoWindowRect = new RectF(0, 0, INFO_WINDOW_WIDTH, INFO_WINDOW_HEIGHT);
    int infoWindowOffsetX = selDestinationOffset.x - INFO_WINDOW_WIDTH / 2;
    int infoWindowOffsetY = selDestinationOffset.y - INFO_WINDOW_HEIGHT - markerImage.getHeight();
    infoWindowRect.offset(infoWindowOffsetX, infoWindowOffsetY);

    // Draw inner info window
    canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());

    // Draw border for info window
    canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());

    // Draw the MapLocation's name
    int TEXT_OFFSET_X = 10;
    int TEXT_OFFSET_Y = 15;
    canvas.drawText("hi there!!", infoWindowOffsetX + TEXT_OFFSET_X, infoWindowOffsetY + TEXT_OFFSET_Y, getTextPaint());
  }

  Paint innerPaint, borderPaint, textPaint;

  public Paint getInnerPaint () {
    if (innerPaint == null) {
      innerPaint = new Paint();
      innerPaint.setARGB(225, 75, 75, 75); // gray
      innerPaint.setAntiAlias(true);
    }
    return innerPaint;
  }

  public Paint getBorderPaint () {
    if (borderPaint == null) {
      borderPaint = new Paint();
      borderPaint.setARGB(255, 255, 255, 255);
      borderPaint.setAntiAlias(true);
      borderPaint.setStyle(Style.STROKE);
      borderPaint.setStrokeWidth(2);
    }
    return borderPaint;
  }

  public Paint getTextPaint () {
    if (textPaint == null) {
      textPaint = new Paint();
      textPaint.setARGB(255, 255, 255, 255);
      textPaint.setAntiAlias(true);
    }
    return textPaint;
  }
}
