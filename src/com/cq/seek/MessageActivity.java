package com.cq.seek;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cq.model.Profile;
import com.cq.sqlite.CacheDBAdapter;
import com.cq.tool.RequestTool;

public class MessageActivity extends Activity {
  
  ScreenWrapperUtil<MessageActivity> screenWrapper;
  ProgressDialog dialog;
  
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.message);
    
    screenWrapper = new ScreenWrapperUtil<MessageActivity>(this);
    screenWrapper.setShowBackBtnInsteadOfHomeBtn(false);
    screenWrapper.setupScreenWrapperViews();
    

    CacheDBAdapter cache = new CacheDBAdapter(this);
    cache.open();
    Profile  owner = null;
    int profileId = getIntent().getIntExtra("ownerId", -1);
    if(profileId != -1) {
      owner = cache.getProfile(profileId);
    }
    cache.close();
    
    if(owner != null) {
      TextView msgHeaderTxt = (TextView) findViewById(R.id.message_header_txt);
      msgHeaderTxt.setText(owner.displayNameOrLogin() + " says...");
      BitmapDrawable drawable = new BitmapDrawable(owner.getPhotoLongVersion(this, 25, 25));
      msgHeaderTxt.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }
    
    String msg = getIntent().getStringExtra("message");
    if(msg != null) {
      TextView messageTxt = (TextView) findViewById(R.id.Message_MessageBodyTxt);
      messageTxt.setText(msg);
    }
    
    final int seekRequestId = getIntent().getIntExtra("seekRequestId", -1);
    final Profile profile = owner;
    if(seekRequestId != -1 && profile != null) {
      ImageButton acceptButton = (ImageButton) findViewById(R.id.message_accept_btn);
      acceptButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick (View v) {
          String str = "Accepting seek from " + profile.displayNameOrLogin();
          dialog = ProgressDialog.show(MessageActivity.this, "", str, true);
          Thread t = new Thread(null, new AcceptRejectRunnable(seekRequestId, true, null), "MagentoBackground");
          t.start();
        }
      });
      ImageButton rejectButton = (ImageButton) findViewById(R.id.message_reject_btn);
      rejectButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick (View v) {
          String str = "Rejecting seek from " + profile.displayNameOrLogin();
          ProgressDialog.show(MessageActivity.this, "", str, true);
          Thread t = new Thread(null, new AcceptRejectRunnable(seekRequestId, false, null), "MagentoBackground");
          t.start();
        }
      });
      
    }
  }

  public class AcceptRejectRunnable implements Runnable {
    Integer seekRequestId;
    boolean accept;
    String msg;

    public AcceptRejectRunnable(int seekRequestId, boolean accept, String msg) {
      this.seekRequestId = new Integer(seekRequestId);
      this.accept = accept;
      this.msg = msg;
    }

    public void run () {
      String host = getText(R.string.server_url).toString();
      String urlReq = host + (accept ? getText(R.string.accept_seek_url) : getText(R.string.reject_seek_url)).toString();

      String prefFileName = (String) getText(R.string.pref_file_name);
      RequestTool requestTool = RequestTool.getInstance(getSharedPreferences(prefFileName, Context.MODE_PRIVATE));

      // setup params
      Map<String, String> paramsMap = new HashMap<String, String>();
      paramsMap.put("seek_request_ids", seekRequestId.toString());
      paramsMap.put("message", msg);

      // make the request
      requestTool.makePostRequest(urlReq, paramsMap, true, 200);
      if(dialog != null) dialog.dismiss();
      finish();
    }
  }
}
