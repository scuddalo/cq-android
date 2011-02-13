package com.cq.seek;

import java.util.ArrayList;

import org.w3c.dom.Document;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.cq.model.Profile;
import com.cq.model.Seek;
import com.cq.model.SeekRequest;
import com.cq.tool.IntentTool;
import com.cq.tool.RequestTool;
import com.cq.view.MessagesListAdapter;

public class MessagesListActivity extends ListActivity {

  LayoutInflater viewInflater;
  ArrayList<SeekRequest> seekRequests;
  MessagesListAdapter adapter;
  Runnable getMessages, afterGetMessages;
  ProgressDialog progressDialog;
  ScreenWrapperUtil<MessagesListActivity> screenWrapper;
  

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.messages);
    viewInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);


    screenWrapper = new ScreenWrapperUtil<MessagesListActivity>(this);
    screenWrapper.setShowBackBtnInsteadOfHomeBtn(false);
    screenWrapper.setupScreenWrapperViews();
    screenWrapper.msgsBtn.setPressed(true);
    
    seekRequests = new ArrayList<SeekRequest>();

    adapter = new MessagesListAdapter(this, R.layout.messages_row, seekRequests);
    setListAdapter(adapter);

//    Button doneBtn = (Button) findViewById(R.id.Messages_DoneBtn);
//    doneBtn.setOnClickListener(IntentTool.createOnclickListener(this, HomeActivity.class, null));

    getMessages = new Runnable()
    {

      public void run () {
        getSeekRequests();
      }

    };

    afterGetMessages = new Runnable()
    {

      public void run () {
        displaySeekMessages();
      }

    };

    Thread thread = new Thread(null, getMessages, "MagentoBackground");
    thread.start();

    showProgressDialog("Loading your active seek. Please wait ...");
  }

  void getSeekRequests () {
    // 1) get the profile-id string from prefFile
    String prefFileName = getText(R.string.pref_file_name).toString();
    SharedPreferences prefs = getSharedPreferences(prefFileName, MODE_PRIVATE);

    String profileIdStr = prefs.getString("profileId", null);
    String profileId = profileIdStr != null ? profileIdStr.split("-")[0] : null;
    if (profileId != null) {
      String url = getText(R.string.server_url) + getText(R.string.seek_requests_for_profile_url).toString().replaceAll("#\\{profile_id_num\\}", profileId);
      Document doc = RequestTool.getInstance(prefs).makeGetRequest(url, null, 200);
      seekRequests = (ArrayList<SeekRequest>) SeekRequest.constructFromXml(doc);
    }

    runOnUiThread(afterGetMessages);
  }

  void displaySeekMessages () {
    if (seekRequests != null && seekRequests.size() > 0) {
      adapter.notifyDataSetChanged();
      for(SeekRequest r : seekRequests) {
        adapter.add(r);
      }
    }
    hideProgressDialog();
    adapter.notifyDataSetChanged();
  }

  public void showProgressDialog (String str) {
    progressDialog = ProgressDialog.show(this, "", str, true);
  }

  public void hideProgressDialog () {
    progressDialog.dismiss();
  }
  
  @Override
  protected void onListItemClick (ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    
    SeekRequest request = adapter.getItem(position);
    Seek seek = request.getSeek();
    Profile owner = null;
    if(seek != null && (owner = seek.getOwner()) != null) {
      Intent viewMsg = new Intent(this, MessageActivity.class);
      viewMsg.putExtra("ownerId", owner.getId());
      viewMsg.putExtra("message", (seek.getMessage() != null) ? seek.getMessage().getContent() : "");
      viewMsg.putExtra("seekRequestId", request.getId());
      viewMsg.putExtra("replaceHomeBtnWithBackBtn", true);
      startActivity(viewMsg);
    }
  }
}
