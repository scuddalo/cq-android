package com.cq.tool;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class IntentTool {
  public static <T extends Activity> OnClickListener createOnclickListener (final T src, final Class<? extends Activity> target, final Bundle extras) {
    return new OnClickListener()
    {

      public void onClick (View v) {
        ProgressDialog progressDialog = ProgressDialog.show(src, "", "Loading ...", true);
        progressDialog.show();
        Intent goFromSrcToTaget = new Intent(src, target);
        if (extras != null) {
          goFromSrcToTaget.putExtras(extras);
        }
        src.startActivity(goFromSrcToTaget);
        progressDialog.dismiss();
      }

    };
  }
}
