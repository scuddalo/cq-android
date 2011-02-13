package com.cq.seek;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cq.model.Profile;
import com.cq.sqlite.CacheDBAdapter;
import com.cq.tool.LocationTool;

public class ContactDetailsActivity extends Activity {

  ImageView photoView;
  TextView  nameView, phoneView, emailView, addressView;
  ImageButton seekIndvBtn, addIndvToTierBtn;
  ScreenWrapperUtil<ContactDetailsActivity> screenWrapper;
  
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_details);


    screenWrapper = new ScreenWrapperUtil<ContactDetailsActivity>(this);
    screenWrapper.setShowBackBtnInsteadOfHomeBtn(getIntent().getExtras().getBoolean("replaceHomeBtnWithBackBtn"));
    screenWrapper.setupScreenWrapperViews();
    
    setupPage();
    
    Profile profile = null;
    CacheDBAdapter cache = new CacheDBAdapter(this);
    cache.open();
    int profileId = getIntent().getIntExtra("selectedProfileId", -1);
    if(profileId != -1 && (profile = cache.getProfile(profileId)) != null) {
      populatePage(profile);
    }
    cache.close();
  }

  private void setupPage () {
    photoView = (ImageView) findViewById(R.id.cdPhoto);
    nameView = (TextView) findViewById(R.id.cdName);
    phoneView = (TextView) findViewById(R.id.cdPhoneText);
    emailView = (TextView) findViewById(R.id.cdEmailText);
    addressView = (TextView) findViewById(R.id.cdLocationText);
    seekIndvBtn = (ImageButton) findViewById(R.id.cdSeekBtn);
    addIndvToTierBtn = (ImageButton) findViewById(R.id.cdAddToTierBtn);
  }

  void populatePage (final Profile profile) {
    if(profile != null) {
      photoView.setImageBitmap(profile.getPhotoLongVersion(this, -1, -1));
      nameView.setText(profile.getDisplayName());
      phoneView.setText(profile.getCellNumber());
      emailView.setText(profile.getEmail());
      addressView.setText(LocationTool.getLocality(profile.getLocation().getGeoPoint(), this));
      seekIndvBtn.setOnClickListener(new View.OnClickListener()
      {
        public void onClick (View v) {
          Intent goToSeek = new Intent(ContactDetailsActivity.this, WhoToSeekActivity.class);
          ArrayList<String> tmp = new ArrayList<String>();
          tmp.add("" + profile.getId());
          goToSeek.putExtra(WhoToSeekActivity.ProfileIdsToSeekKey, tmp.toArray(new String[1]));
          startActivity(goToSeek);
        }
      });
    }
  }

}
