package com.cq.seek;

import java.util.ArrayList;

import org.w3c.dom.Document;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.cq.model.Message;
import com.cq.model.Profile;
import com.cq.model.Seek;
import com.cq.model.SeekRequest;
import com.cq.tool.IntentTool;
import com.cq.tool.LocationTool;
import com.cq.tool.RequestTool;
import com.cq.view.ManageSeekRequestsAdapter;
import com.google.android.maps.GeoPoint;

public class ManageSeekRequestActivity extends ListActivity {

  TableLayout seekManagementTable;
  ArrayList<SeekRequest> seekRequests;
  ManageSeekRequestsAdapter adapter;
  Runnable viewSeekRequests;
  Integer seekId;
  ProgressDialog progressDialog;
  TextView header, messageTxt, messageHeaderTxt;
  ImageButton newSeekBtn;
  ScreenWrapperUtil<ManageSeekRequestActivity> screenWrapper;

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.manage_seek);

    screenWrapper = new ScreenWrapperUtil<ManageSeekRequestActivity>(this);
    screenWrapper.setShowBackBtnInsteadOfHomeBtn(false);
    screenWrapper.setupScreenWrapperViews();
    screenWrapper.seekBtn.setPressed(true);
    screenWrapper.seekBtn.setEnabled(false);
    
    header = (TextView) findViewById(R.id.ManageSeek_Header);
    messageTxt = (TextView) findViewById(R.id.ManageSeek_MessageBodyTxt);
    messageHeaderTxt = (TextView) findViewById(R.id.ManageSeek_MsgHeaderTxt);
    newSeekBtn = (ImageButton) findViewById(R.id.newSeekBtn);
     
    newSeekBtn.setOnClickListener(IntentTool.createOnclickListener(this, WhoToSeekActivity.class, null));

    //init collections
    seekRequests = new ArrayList<SeekRequest>();

    //get input seek_id
    seekId = getIntent().getIntExtra("seekId", -1);
    /**
     * Seek activeSeek = null; try { byte[] activeSeekBytes = getIntent
     * ().getByteArrayExtra ("activeSeek"); ByteArrayInputStream bis = new
     * ByteArrayInputStream (activeSeekBytes); ObjectInputStream ois = new
     * ObjectInputStream (bis); activeSeek = (Seek) ois.readObject (); } catch
     * (Exception e) { e.printStackTrace (); }
     **/

    //set adapter for this listActivity
    adapter = new ManageSeekRequestsAdapter(this, R.layout.manage_seek_row, seekRequests);
    setListAdapter(adapter);

    //Button newSeekBtn = (Button) findViewById(R.id.ManageSeek_NewSeek);
    //newSeekBtn.setOnClickListener(IntentTool.createOnclickListener(this, WhoToSeekActivity.class, null));

    //Button doneBtn = (Button) findViewById(R.id.ManageSeek_DoneBtn);
    //doneBtn.setOnClickListener(IntentTool.createOnclickListener(this, HomeActivity.class, null));

    viewSeekRequests = new Runnable()
    {
      public void run () {
        getSeekRequestsForSeek();
      }
    };

    Thread thread = new Thread(null, viewSeekRequests, "MagentoBackground");
    thread.start();
    

    showProgressDialog("Loading your active seek. Please wait ...");
  }

  void showProgressDialog (String str) {
    progressDialog = ProgressDialog.show(this, "", str, true);
  }

  GeoPoint seekPoint = null;
  Seek seek;

  void getSeekRequestsForSeek () {
    if (seekId != -1) {
      // 1) get the profile-id string from prefFile
      String prefFileName = getText(R.string.pref_file_name).toString();
      SharedPreferences prefs = getSharedPreferences(prefFileName, MODE_PRIVATE);

      String url = getText(R.string.server_url) + getText(R.string.seek_requests_for_seek_url).toString().replaceAll("#\\{seek_id\\}", seekId.toString());
      Document doc = RequestTool.getInstance(prefs).makeGetRequest(url, null, 200);
      seekRequests = (ArrayList<SeekRequest>) SeekRequest.constructFromXml(doc);
      if (seekRequests != null && seekRequests.size() > 0) {
        seek = seekRequests.get(0).getSeek();
        Profile seekOwner = seek.getOwner();
        com.cq.model.Location seekLoc = seekOwner.getLocation();
        seekPoint = seekLoc != null ? seekLoc.getGeoPoint() : null;
      }
    }

    runOnUiThread(afterGettingSeekRequests);
  }

  private Runnable afterGettingSeekRequests = new Runnable()
  {
    public void run () {
      Message msg = null;
      if (seekRequests != null && seekRequests.size() > 0) {
        adapter.notifyDataSetChanged();
        for(SeekRequest r : seekRequests) {
          if (msg == null) msg = r.getMessage(); //get any of the message, as the text will be the same.
          adapter.add(r);
        }
      }

      if (seekPoint != null) {
        header.append(" from: " + LocationTool.getLocality(seekPoint, ManageSeekRequestActivity.this));
      }
      
      if(seek != null && seek.getOwner() != null) {
        messageHeaderTxt.setText(seek.getOwner().displayNameOrLogin() + " said ...");
        messageHeaderTxt.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(seek.getOwner().getPhotoLongVersion(ManageSeekRequestActivity.this, 25, 25)), null, null, null);
      }
      
      if(msg != null) {
        messageTxt.setText(msg.getContent());
      }
      
      if(progressDialog.isShowing()) progressDialog.dismiss();
      adapter.notifyDataSetChanged();
    }
  };

}
