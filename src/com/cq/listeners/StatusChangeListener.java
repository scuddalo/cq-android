package com.cq.listeners;

import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;

import com.cq.tool.RequestTool;

public class StatusChangeListener implements TextWatcher {

  private String statusUpdateUrl;
  private SharedPreferences prefs;

  public StatusChangeListener(String statusUpdateUrl, SharedPreferences prefs) {
    this.statusUpdateUrl = statusUpdateUrl;
    this.prefs = prefs;
  }

  public void afterTextChanged (Editable s) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("status", s.toString());

    RequestTool.getInstance(prefs).makePostRequest(statusUpdateUrl, params, 200);
  }

  public void beforeTextChanged (CharSequence s, int start, int count, int after) {
    // intentially not implemented
  }

  public void onTextChanged (CharSequence s, int start, int before, int count) {
    // intentially not implemented
  }

}
