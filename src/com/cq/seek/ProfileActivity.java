package com.cq.seek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cq.model.Location;
import com.cq.model.Profile;
import com.cq.tool.StringTool;

public class ProfileActivity extends Activity {

  public ProfileActivity() {
  }

  @Override
  public void onCreate (Bundle icicle) {
    // usual activity stuff
    super.onCreate(icicle);
    setContentView(R.layout.profile);

    // get the extras that has been passed on
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    // get the data from the extras.
    // if they don't have profile data, then throw RunTimeException
    String name = "";
    String location = "";
    String status = "";
    Profile p = null;

    if (extras != null) {
      try {
        p = (Profile) extras.get("profile");
        name = StringTool.getNonNullString(p.getDisplayName(), p.getUser().getLogin());
        Location l = p.getLocation();
        location = (l == null) ? "" : l.getLatitude() + "," + p.getLocation().getLongitude();
        status = p.getStatus();

      }
      catch(ClassCastException ex) {
        throw new RuntimeException("Tried casting an object from extras bundle and got this error!");
      }
    }

    // now that we have all profile details (hopefully). lets display it.
    TextView nameText = (TextView) findViewById(R.id.cqName);
    nameText.setText(name);
    TextView locationText = (TextView) findViewById(R.id.cqLocation);
    locationText.setText(location);
    TextView statusText = (TextView) findViewById(R.id.cqStatus);
    statusText.setText(status);

    Button backButton = (Button) findViewById(R.id.cqProfileBackBtn);
    backButton.setOnClickListener(new OnClickListener()
    {

      public void onClick (View v) {
        Intent goBackToHomeScreenIntent = new Intent(ProfileActivity.this, HomeActivity.class);
        ProfileActivity.this.startActivity(goBackToHomeScreenIntent);
      }

    });
  }
}
