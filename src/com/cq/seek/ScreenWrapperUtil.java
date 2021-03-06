package com.cq.seek;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.w3c.dom.Document;

import android.app.Activity;
import android.content.Context;
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
  boolean showGoOffGridBtnInsteadOfHomeBtn = false;

  public ScreenWrapperUtil(T ctx) {
    context = ctx;
  }

  public void setShowBackBtnInsteadOfHomeBtn (boolean val) {
    showBackBtnInsteadOfHomeBtn = val;
  }

  public void setShowGoOffGridBtnInsteadOfHomeBtn (boolean val) {
    showGoOffGridBtnInsteadOfHomeBtn = val;
  }

  void setupScreenWrapperViews () {
    setupHomeBtn();
    preferencesBtn = (ImageButton) context.findViewById(R.id.prefBtn);
    preferencesBtn.setEnabled(true);
    seekBtn = (ImageButton) context.findViewById(R.id.seekBtn);
    seekBtn.setOnClickListener(seekYouButtonListener);
    seekBtn.setEnabled(true);

    msgsBtn = (ImageButton) context.findViewById(R.id.msgBtn);
    msgsBtn.setOnClickListener(IntentTool.createOnclickListener(context, MessagesListActivity.class, null));
    msgsBtn.setEnabled(true);

    addToTiersBtn = (ImageButton) context.findViewById(R.id.addToTiersBtn);
    addToTiersBtn.setEnabled(true);
  }

  void setupHomeBtn () {
    homeBtn = (ImageButton) context.findViewById(R.id.homeBtn);
    homeBtn.setEnabled(true);
    if (showGoOffGridBtnInsteadOfHomeBtn) {
      String prefFileName = context.getText(R.string.pref_file_name).toString();
      SharedPreferences prefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
      boolean isInvisible = prefs.getBoolean(SeekYouConstants.Invisible, false);
      if (isInvisible) {
        homeBtn.setImageResource(R.drawable.cq_invisible_btn_down);
      }
      else {
        homeBtn.setImageResource(R.drawable.cq_invisible_btn);
      }
    }
    else if (showBackBtnInsteadOfHomeBtn) {
      homeBtn.setImageResource(R.drawable.cq_back_btn);
    }

    homeBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick (View v) {
        if (showGoOffGridBtnInsteadOfHomeBtn) {
          String prefFileName = context.getText(R.string.pref_file_name).toString();
          SharedPreferences prefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
          boolean isInvisible = prefs.getBoolean(SeekYouConstants.Invisible, false);

          StringBuilder sb = new StringBuilder();
          sb.append(context.getString(R.string.server_url));
          //toggle the state
          if (isInvisible) {
            sb.append(context.getString(R.string.go_online_url));
          }
          else {
            sb.append(context.getString(R.string.go_offline_url));
          }
          String profileId = prefs.getString(SeekYouConstants.ProfileId, null);
          if (profileId != null) {
            String url = sb.toString().replaceAll("#\\{profile_id_num\\}", profileId);
            Document doc = RequestTool.getInstance(prefs).makePostRequest(url, null, 200);
            if (doc != null) {
              ((ImageButton) v).setImageResource(isInvisible ? R.drawable.cq_invisible_btn_down : R.drawable.cq_invisible_btn);
              SharedPreferences.Editor editor = prefs.edit();
              editor.putBoolean(SeekYouConstants.Invisible, !isInvisible);
              editor.commit();
            }
          }
        }
        else if (showBackBtnInsteadOfHomeBtn) {
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
