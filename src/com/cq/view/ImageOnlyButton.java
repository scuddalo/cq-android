package com.cq.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;

import com.cq.seek.R;
import com.cq.tool.StringTool;

public class ImageOnlyButton extends ImageButton {

  int imageResourceNotFocused, imageResourceFocused, imageResourcePressed,
      imageHeight, imageWidth;

  private boolean isButtonPressed;

  public ImageOnlyButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(attrs);
  }

  public ImageOnlyButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public ImageOnlyButton(Context context) {
    super(context);
    throw new RuntimeException("Valid image resource IDs must be passed to this class via the XML parameters: cq:resourceNotFocused & cq:resourceFocused.");
  }

  private void init (AttributeSet attrs) {
    TypedArray a = getContext().obtainStyledAttributes(attrs, com.cq.seek.R.styleable.ImageOnlyButton);
    String notFocusedColorStr = a.getString(R.styleable.ImageOnlyButton_resourceNotFocused);
    String focusedColorStr = a.getString(R.styleable.ImageOnlyButton_resourceFocused);
    String pressedColorStr = a.getString(R.styleable.ImageOnlyButton_resourcePressed);

    String tmp = a.getString(R.styleable.ImageOnlyButton_imageHeight);
    imageHeight = StringTool.isNullOrEmpty(tmp) ? -1 : Integer.parseInt(tmp);
    tmp = a.getString(R.styleable.ImageOnlyButton_imageWidth);
    imageWidth = StringTool.isNullOrEmpty(tmp) ? -1 : Integer.parseInt(tmp);

    if (notFocusedColorStr != null && focusedColorStr != null && pressedColorStr != null) {
      imageResourceFocused = a.getResourceId(R.styleable.ImageOnlyButton_resourceFocused, -1);
      imageResourceNotFocused = a.getResourceId(R.styleable.ImageOnlyButton_resourceNotFocused, -1);
      imageResourcePressed = a.getResourceId(R.styleable.ImageOnlyButton_resourcePressed, -1);
    }

    if (imageResourceFocused == -1 || imageResourceNotFocused == -1 || imageResourcePressed == -1) {
      throw new RuntimeException("Valid image resource IDs must be passed to this class via the XML parameters: cq:resourceNotFocused, cq:resourceFocused, & cq:resourcePressed.");
    }

    setBackgroundDrawable(null);
  }

  /**
   * Capture mouse press events to update text state.
   */
  @Override
  public boolean onTouchEvent (MotionEvent event) {
    Log.d("TextOnlyButton", event.getAction() + "");
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      //  Request a redraw to update the button color
      isButtonPressed = true;
      invalidate();
    }
    else if (event.getAction() == MotionEvent.ACTION_UP) {
      isButtonPressed = false;

      //  Requesting focus doesn't work for some reason.  If you find a solution to setting 
      //  the focus, please let me know so I can update the tutorial
      requestFocus();

      //  Request a redraw to update the button color
      invalidate();
    }
    return super.onTouchEvent(event);
  }

  @Override
  public void onDraw (Canvas canvas) {

    if (isButtonPressed) {
      setImageResource(imageResourcePressed);
    }
    else if (isFocused()) {
      //  Since this Button now has no background.  We must swap out the image to display 
      //	one that indicates it has focus.
      setImageResource(imageResourceFocused);
    }
    else {
      setImageResource(imageResourceNotFocused);
    }
    super.onDraw(canvas);
  }
}
