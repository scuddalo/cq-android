package com.cq.seek;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.w3c.dom.Document;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.cq.model.Seek;
import com.cq.tool.IntentTool;
import com.cq.tool.RequestTool;

public class ScreenWrapperUtil<T extends Activity> {

  T context;
  
  final String Tag = ScreenWrapperUtil.class.toString();
  
  ImageButton homeBtn, preferencesBtn, seekBtn, msgsBtn, addToTiersBtn;
  
  boolean showBackBtnInsteadOfHomeBtn = false;

  
  public ScreenWrapperUtil(T ctx) {
    context = ctx;
  }
  
  public void setShowBackBtnInsteadOfHomeBtn(boolean val) {
    showBackBtnInsteadOfHomeBtn = val;
  }

  void setupScreenWrapperViews() {
    setupHomeBtn();
    preferencesBtn = (ImageButton) context.findViewById(R.id.prefBtn);
    seekBtn = (ImageButton) context.findViewById(R.id.seekBtn);
    seekBtn.setOnClickListener(seekYouButtonListener);
    
    msgsBtn = (ImageButton) context.findViewById(R.id.msgBtn);
    msgsBtn.setOnClickListener(IntentTool.createOnclickListener(context, MessagesListActivity.class, null));

    addToTiersBtn = (ImageButton) context.findViewById(R.id.addToTiersBtn);
  }
  
  void setupHomeBtn () {
    homeBtn = (ImageButton) context.findViewById(R.id.homeBtn);
    if(showBackBtnInsteadOfHomeBtn) {
      homeBtn.setImageResource(R.drawable.cq_back_btn);
    }
    
    homeBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick (View v) {
        if(showBackBtnInsteadOfHomeBtn) {
          context.finish();
        }
        else {
          context.startActivity(new Intent(context, HomeActivity.class));
        }
      }
    });
  }

  View.OnClickListener seekYouButtonListener = new View.OnClickListener()
  {
    public void onClick (View v) {
      SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.pref_file_name), Activity.MODE_PRIVATE);
      String profileIdString = prefs.getString("profileId", "");
      String[] tokens = profileIdString.split("-");
      String profileId = tokens[0];
      String activeSeekUrl = context.getString(R.string.server_url) + context.getString(R.string.active_seek_for_profile).replaceAll("#\\{profile_id_num\\}", profileId);
      Document doc = RequestTool.getInstance(prefs).makeGetRequest(activeSeekUrl, null, 200);
      List<Seek> seeks = Seek.constructFromXml(doc);
      Seek activeSeek = seeks != null && seeks.size() > 0 ? seeks.get(0) : null;
      Intent result = null;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos;

      if (activeSeek != null) {
        try {
          result = new Intent(context, ManageSeekRequestActivity.class);
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
        result = new Intent(context, WhoToSeekActivity.class);
      }

      context.startActivity(result);
    }
  };
}
