package com.cq.seek;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.w3c.dom.Document;

import com.cq.model.Seek;
import com.cq.tool.IntentTool;
import com.cq.tool.RequestTool;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public abstract class ScreenWrapper extends Activity {

  final String Tag = ScreenWrapper.class.toString();
  
  ImageButton homeBtn, preferencesBtn, seekBtn, msgsBtn, addToTiersBtn;
  
  boolean showBackBtnInsteadOfHomeBtn = false;
  
  public void setShowBackBtnInsteadOfHomeBtn(boolean val) {
    showBackBtnInsteadOfHomeBtn = val;
  }

  void setupScreenWrapperViews() {
    setupHomeBtn();
    preferencesBtn = (ImageButton) findViewById(R.id.prefBtn);
    seekBtn = (ImageButton) findViewById(R.id.seekBtn);
    seekBtn.setOnClickListener(seekYouButtonListener);
    
    msgsBtn = (ImageButton) findViewById(R.id.msgBtn);
    msgsBtn.setOnClickListener(IntentTool.createOnclickListener(this, MessagesListActivity.class, null));

    addToTiersBtn = (ImageButton) findViewById(R.id.addToTiersBtn);
  }
  
  void setupHomeBtn () {
    homeBtn = (ImageButton) findViewById(R.id.homeBtn);
    if(showBackBtnInsteadOfHomeBtn) {
      homeBtn.setImageResource(R.drawable.cq_back_btn);
    }
    
    homeBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick (View v) {
        finish();
      }
    });
  }
  
  View.OnClickListener seekYouButtonListener = new View.OnClickListener()
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
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos;

      if (activeSeek != null) {
        try {
          result = new Intent(ScreenWrapper.this, ManageSeekRequestActivity.class);
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
        result = new Intent(ScreenWrapper.this, WhoToSeekActivity.class);
      }

      ScreenWrapper.this.startActivity(result);
    }
  };


}
