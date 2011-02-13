package com.cq.seek;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cq.model.Profile;
import com.cq.model.Seek;
import com.cq.sqlite.CacheDBAdapter;
import com.cq.tool.RequestTool;
import com.cq.tool.StringTool;
import com.cq.view.WhoToSeekListAdapter;

public class WhoToSeekActivity extends ListActivity {
  public static final String MapLocationKey = "mapLocations";
  public static final String ProfileIdsToSeekKey = "profileIdsToSeek"; 
  WhoToSeekListAdapter adapter;
  ArrayList<Profile> profiles;
  Runnable viewProfiles, createSeek;
  ProgressDialog progressDialog;
  PopupWindow composeMsgPopup;
  View composeMessagePopupContent;
  String message;
  LayoutInflater viewInflate;
  ViewSwitcher composeMsgViewSwitcher;
  TextView composeMsgScreenToFieldTxt;
  EditText composeMsgScreenTxt;
  

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.who_to_seek_screen);
    viewInflate = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

    composeMessagePopupContent = viewInflate.inflate(R.layout.compose_msg, null);

    profiles = new ArrayList<Profile>();

    adapter = new WhoToSeekListAdapter(this, R.layout.who_to_seek_row, profiles);
    setListAdapter(adapter);

    composeMsgViewSwitcher = (ViewSwitcher) findViewById(R.id.who_to_seek_screen_ListComposeViewSwitcher);
    composeMsgScreenToFieldTxt = (TextView) findViewById(R.id.composeMsgToTxt);
    composeMsgScreenTxt = (EditText) findViewById(R.id.composeMsgText);
    
    // setOnClickListeners for all the buttons ...
    // back button
    ImageButton backBtn = (ImageButton) findViewById(R.id.WhoToSeek_CancelBtn);
    backBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick (View v) {
        finish();
      }
    });

    ListView view = getListView();
    view.setOnItemClickListener(profileClickListener);

    //compose button
    final ImageButton composeBtn = (ImageButton) findViewById(R.id.WhoToSeek_ComposeBtn);
    composeBtn.setOnClickListener(new OnClickListener()
    {
      public void onClick (View arg0) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(composeMsgScreenTxt.getApplicationWindowToken(), 0);
        
        StringBuffer b = null;
        for(Profile p : selectedProfiles) {
          if(b == null) {
            b = new StringBuffer();
          }
          else {
            b.append(", ");
          }
          b.append(p.displayNameOrLogin());
        }
        composeMsgScreenToFieldTxt.setText(b.toString());
        composeMsgViewSwitcher.showNext();
      }
    });

    final ImageButton cancelComposeMsgButton = (ImageButton) findViewById(R.id.cancelComposedMsgBtn);
    cancelComposeMsgButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick (View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(composeMsgScreenTxt.getApplicationWindowToken(), 0);
        composeMsgViewSwitcher.showPrevious();
      }
    });
    
    final ImageButton sendFromComposeMsgButton = (ImageButton) findViewById(R.id.sendComposedMsgBtn);
    sendFromComposeMsgButton.setOnClickListener(new View.OnClickListener() {
      public void onClick (View v) {
        message = composeMsgScreenTxt.getText().toString();
        Thread thread = new Thread(null, createSeek, "MagentoBackground");
        thread.start();
        
        composeMsgViewSwitcher.showPrevious();
        showProgressDialog("Sending seek messages. Please wait...");
        
      }
    });
    //...and finally send button
    /**
    Button sendBtn = (Button) findViewById(R.id.WhoToSeek_SendBtn);
    sendBtn.setOnClickListener(new OnClickListener()
    {

      public void onClick (View arg0) {
        Thread thread = new Thread(null, createSeek, "MagentoBackground");
        thread.start();
        showProgressDialog("Sending seek messages. Please wait...");
      }

    });
  **/
    viewProfiles = new Runnable()
    {
      public void run () {
        String[] profileIdsToSeek = getIntent().getStringArrayExtra(ProfileIdsToSeekKey);
        //either populate 'profiles' collection with profileIds passed in ....
        if(profileIdsToSeek != null && profileIdsToSeek.length > 0) {
          CacheDBAdapter cache = new CacheDBAdapter(WhoToSeekActivity.this);
          cache.open();
          profiles = new ArrayList<Profile>();
          for(String id : profileIdsToSeek) {
            Profile p = cache.getProfile(Integer.parseInt(id));
            if(p != null) profiles.add(p);
          }
        }
        else { // or with whoever is arround you.
          whosAround();
        }
        
        runOnUiThread(returnRes);
      }
    };

    createSeek = new Runnable()
    {
      public void run () {
        createSeek();
      }
    };

    Thread thread = new Thread(null, viewProfiles, "MagentoBackground");
    thread.start();

    progressDialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
  }

  void showProgressDialog (String str) {
    progressDialog = ProgressDialog.show(this, "", str, true);
  }

  Seek createdSeek;

  void createSeek () {
    if (selectedProfiles != null && selectedProfiles.toArray().length > 0) {
      Map<String, String> params = new HashMap<String, String>();
      List<String> ids = new ArrayList<String>();
      for(Profile p : selectedProfiles) {
        ids.add(Integer.toString(p.getId()));
      }

      params.put("seeked_profile_ids", StringTool.join(ids, ","));
      params.put("message", message);

      StringBuilder sb = new StringBuilder();
      sb.append(getString(R.string.server_url));
      sb.append(getString(R.string.seek_url));

      String prefFileName = getText(R.string.pref_file_name).toString();
      SharedPreferences prefs = getSharedPreferences(prefFileName, MODE_PRIVATE);
      Document doc = RequestTool.getInstance(prefs).makePostRequest(sb.toString(), params, 200);
      List<Seek> seeks = Seek.constructFromXml(doc);
      createdSeek = (seeks != null && seeks.size() > 0) ? seeks.get(0) : null;
    }

    runOnUiThread(afterCreateSeek);

  }

  void whosAround () {
    // 1) get the profile-id string from prefFile
    String prefFileName = getText(R.string.pref_file_name).toString();
    SharedPreferences prefs = getSharedPreferences(prefFileName, MODE_PRIVATE);
    String profileIdStr = prefs.getString("profileId", null);

    profiles = new ArrayList<Profile>();

    if (profileIdStr != null) {
      // 2) make url http://<host>/people/<profile-id>/whos_around.xml
      String host = getText(R.string.server_url).toString();
      String url = host + "/people/" + profileIdStr + "/location/whos_around.xml";
      RequestTool reqTool = RequestTool.getInstance(prefs);
      Document doc = reqTool.makeGetRequest(url, null, 200);
      profiles = (ArrayList<Profile>) Profile.constructFromXml(doc);
    }
  }

  private Runnable returnRes = new Runnable()
  {
    public void run () {
      if (profiles != null && profiles.size() > 0) {
        adapter.notifyDataSetChanged();
        for(Profile p : profiles) {
          adapter.add(p);
        }
      }
      progressDialog.dismiss();
      adapter.notifyDataSetChanged();
    }
  };

  private Runnable afterCreateSeek = new Runnable()
  {

    public void run () {
      if (createdSeek != null) {
        Intent goToManageSeek = new Intent(WhoToSeekActivity.this, ManageSeekRequestActivity.class);
        goToManageSeek.putExtra("seekId", createdSeek.getId());

        WhoToSeekActivity.this.startActivity(goToManageSeek);
      }

      progressDialog.dismiss();
    }

  };

  Set<Profile> selectedProfiles = new HashSet<Profile>();
  AdapterView.OnItemClickListener profileClickListener = new AdapterView.OnItemClickListener()
  {

    public void onItemClick (AdapterView<?> arg0, View v, int position, long id) {
      Profile selectedProfile = profiles.get(position);
      ImageView isSelectedImg = (ImageView) v.findViewById(R.id.who_to_seek_row_selected_img);
      if (selectedProfiles.contains(selectedProfile)) {
        selectedProfiles.remove(selectedProfile);
        isSelectedImg.setVisibility(View.INVISIBLE);
      }
      else {
        selectedProfiles.add(selectedProfile);
        isSelectedImg.setVisibility(View.VISIBLE);
      }
    }

  };
}
