package com.cq.seek;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.cq.model.Profile;
import com.cq.sqlite.CacheDBAdapter;
import com.cq.tool.ImageTool;
import com.cq.tool.RequestTool;

/**
 * @author santoash
 */
public class LoginActivity extends Activity {

  private static final String TAG = "LoginScreen";
  private final Handler mHandler = new Handler();

  private boolean isAuthenticated;

  private EditText txtUsername;
  private EditText txtPassword;
  //private CheckBox cbRem;
  private ImageButton btnSign;

  @Override
  public void onCreate (Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.login_screen);
    Log.i(TAG, "onCreate() called!");
    btnSign = (ImageButton) this.findViewById(R.id.btnSign);
    txtUsername = (EditText) this.findViewById(R.id.txtUsername);
    txtPassword = (EditText) this.findViewById(R.id.txtPassword);
    txtPassword.setOnKeyListener(new View.OnKeyListener()
    {

      public boolean onKey (View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
          InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
          v.findFocus();
          return true;
        }
        return false;
      }
    });

    //cbRem = (CheckBox) this.findViewById (R.id.cbRem);
    btnSign.setOnClickListener(mSignListener);

  }

  public ProgressDialog progressDialog;
  private OnClickListener mSignListener = new OnClickListener()
  {
    public void onClick (View v) {

      progressDialog = ProgressDialog.show(LoginActivity.this, "", "Loading. Please wait...", true);
      progressDialog.show();
      txtUsername.setEnabled(false);
      txtPassword.setEnabled(false);
      //  cbRem.setEnabled (false);
      btnSign.setEnabled(false);

      Thread t = new Thread()
      {

        @Override
        public void run () {
          isAuthenticated = clickSign();
          mHandler.post(mUpdateResults);
        }
      };
      t.start();
    }
  };

  private boolean clickSign () {
    String username = txtUsername.getText().toString();
    String password = txtPassword.getText().toString();
    String host = getText(R.string.server_url).toString();
    String urlReq = host + getText(R.string.url_login).toString();

    Log.i(TAG, "username: " + username);
    Log.i(TAG, "urlReq: " + urlReq);

    String prefFileName = (String) getText(R.string.pref_file_name);
    RequestTool requestTool = RequestTool.getInstance(this.getSharedPreferences(prefFileName, MODE_PRIVATE));

    // setup params
    Map<String, String> paramsMap = new HashMap<String, String>();
    paramsMap.put("login", username);
    paramsMap.put("password", password);

    // make the request
    Document doc = requestTool.makePostRequest(urlReq, paramsMap, true, 200);
    if (doc != null) {
      // store the details in the sharedPreferences so
      // other activities can use it.
      putUserDetailsIntoPreferences(username, password, doc);
      return true;
    }
    return false;
  }

  void putUserDetailsIntoPreferences (String username, String password, Document doc) {
    if (doc != null) {
      NodeList profiles = doc.getElementsByTagName("profile");
      Element profileEl = (Element) profiles.item(0);
      Profile profile = Profile.constructFromXml(profileEl);
      
      CacheDBAdapter cache = new CacheDBAdapter(this);
      cache.open();
      cache.insertProfileDetails(profile);
      cache.close();

      String prefFileName = (String) getText(R.string.pref_file_name);
      SharedPreferences pref = this.getSharedPreferences(prefFileName, MODE_PRIVATE);
      SharedPreferences.Editor editor = pref.edit();
      String profileIdStr = profile.getId() + "-" + username;

      // TODO: have to change how the backend expects profile id
      // profile id format (I think) is <profile_id>-<username>
      editor.putString("profileId", profileIdStr);

      editor.putString("username", username);
      editor.putString("password", password);

      String photoFileName = profileIdStr + "." + profile.photoFileExtension();
      Bitmap photo = BitmapFactory.decodeFile("/data/data/com.cq/files/" + photoFileName);

      // making sure you have the photo and you have the photo file name 
      if (photo == null && profile.getPhotoFileName() != null && profile.getPhotoFileName().length() > 0) {
        editor.putString("photoFileName", photoFileName);
        String fileUrl = getString(R.string.server_url) + "/images/" + "/thumb/" + profileIdStr + "." + profile.photoFileExtension();
        photo = RequestTool.getInstance(pref).downloadImage(fileUrl);
        photo = ImageTool.resize(photo, 50, 50);
        try {
          FileOutputStream fos = getApplicationContext().openFileOutput(photoFileName, MODE_PRIVATE);
          BufferedOutputStream bos = new BufferedOutputStream(fos);
          photo.compress(CompressFormat.JPEG, 75, bos);
          bos.flush();
          bos.close();
        }
        catch(FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          Log.w("no profile picture found", e);
        }
        catch(IOException e) {
          e.printStackTrace();
          Log.w("error writeing photo", e);
        }
      }

      if (profile.getStatus() == null) {
        profile.setStatus("");
      }

      editor.putString("status", profile.getStatus());

      editor.commit();
    }
  }

  // Create runnable for posting
  final Runnable mUpdateResults = new Runnable()
  {
    public void run () {

      if (!isAuthenticated) {
        txtUsername.setEnabled(true);
        txtPassword.setEnabled(true);
        //                cbRem.setEnabled (true);
        btnSign.setEnabled(true);
        progressDialog.dismiss();
        new AlertDialog.Builder(LoginActivity.this).setMessage("Login failed. Please try again.").setNeutralButton("OK", new DialogInterface.OnClickListener()
        {
          public void onClick (DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }).show();
      }
      else {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);
        Intent loginIntent = new Intent(LoginActivity.this, HomeActivity.class);
        //                Intent loginIntent = new Intent (LoginActivity.this,
        //                        MessagesListActivity.class);
        LoginActivity.this.startActivity(loginIntent);
      }
    }
  };

}
