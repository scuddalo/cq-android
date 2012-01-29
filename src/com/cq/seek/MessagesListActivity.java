package com.cq.seek;

import java.util.ArrayList;

import org.w3c.dom.Document;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.cq.model.Profile;
import com.cq.model.Seek;
import com.cq.model.SeekRequest;
import com.cq.tool.RequestTool;
import com.cq.view.MessagesListAdapter;

public class MessagesListActivity extends ListActivity {

  public static final String TAG = "MessagesListActivity";
  LayoutInflater viewInflater;
  ArrayList<SeekRequest> seekRequests;
  MessagesListAdapter adapter;
  Runnable getMessages, afterGetMessages;
  ProgressDialog progressDialog;
  ScreenWrapperUtil<MessagesListActivity> screenWrapper;
  
  public static final int USER_ACCEPTED = 2;
  public static final int USER_REJECTED = 3;
  public static final int NO_ACTION = 4;
  private static final int ACCEPT_REJECT_MSG = 5;

  @Override
  protected void onResume () {
    super.onResume();
    Log.i(TAG, "list resumed ######################################## ");
    adapter.notifyDataSetChanged();
  }
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.i(TAG, "list created ######################################## ");
    setContentView(R.layout.messages);
    viewInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);


    screenWrapper = new ScreenWrapperUtil<MessagesListActivity>(this);
    screenWrapper.setShowBackBtnInsteadOfHomeBtn(false);
    screenWrapper.setupScreenWrapperViews();
    screenWrapper.msgsBtn.setPressed(true);
    screenWrapper.msgsBtn.setEnabled(false);
    
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
      //mark the seekrequest as read in the bg
      SeekRequest seekRequest = seekRequests.get(position);
      
      seekRequest.getMessage().setIsRead(true);
      MarkSeekRequestAsRead markAsRead = new MarkSeekRequestAsRead(this, request);
      Thread thread = new Thread(null, markAsRead, "MagentoBackground");
      thread.start();
      
      Intent viewMsg = new Intent(this, MessageActivity.class);
      viewMsg.putExtra("ownerId", owner.getId());
      viewMsg.putExtra("message", (request.getMessage() != null) ? request.getMessage().getContent() : "");
      viewMsg.putExtra("seekRequestId", request.getId());
      viewMsg.putExtra("replaceHomeBtnWithBackBtn", true);
      viewMsg.putExtra("listPosition", position);
      startActivityForResult(viewMsg, ACCEPT_REJECT_MSG);
    }
  }
  
  @Override
  protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    switch(requestCode) {
      case ACCEPT_REJECT_MSG:
	      int position = data.getIntExtra("listPosition", -1);
	      Log.i(TAG, "############# onActivityResult.position: " + position);
	      if(position != -1) {
	        SeekRequest  req = seekRequests.get(position);
	        req.setAccepted((resultCode == USER_ACCEPTED));
	        setListAdapter(adapter);
	      }
	      adapter.notifyDataSetChanged();
        break;

      default:
        break;
    }
  }
  
  public static class MarkSeekRequestAsRead implements Runnable {
    Context context;
    SeekRequest seekRequest;
    public MarkSeekRequestAsRead(Context c,  SeekRequest seekReq) {
      seekRequest = seekReq;
      context = c;
    }
    public void run () {
	    // 1) get the profile-id string from prefFile
	    String prefFileName = context.getText(R.string.pref_file_name).toString();
	    SharedPreferences prefs = context.getSharedPreferences(prefFileName, MODE_PRIVATE);
	    seekRequest.getMessage().setIsRead(true);
      String url = context.getText(R.string.server_url) + context.getText(R.string.mark_seek_request_as_read).toString().replaceAll("#\\{seek_request_id\\}", "" + seekRequest.getId());
      RequestTool.getInstance(prefs).makePostRequest(url, null, 200);
    }
  }
}
