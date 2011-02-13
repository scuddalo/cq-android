package com.cq.seek;

import android.content.Context;
import android.content.Intent;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {

  public C2DMReceiver(String senderId) {
    super(senderId);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void onError (Context context, String errorId) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onMessage (Context context, Intent intent) {
    // TODO Auto-generated method stub

  }

}
