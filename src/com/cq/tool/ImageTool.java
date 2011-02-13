package com.cq.tool;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public final class ImageTool {
  public static Bitmap resize (Bitmap bitmap, int newHeight, int newWidth) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();

    // calculate the scale - in this case = 0.4f 
    float scaleWidth = ((float) newWidth) / width;
    float scaleHeight = ((float) newHeight) / height;

    // createa matrix for the manipulation 
    Matrix matrix = new Matrix();

    // resize the bit map 
    matrix.postScale(scaleWidth, scaleHeight);

    // recreate the new Bitmap 
    Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

    return resizedBitmap;
  }
}
