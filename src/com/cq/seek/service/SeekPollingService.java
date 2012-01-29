package com.cq.seek.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cq.listeners.SeekPollListener;
import com.cq.seek.HomeActivity;
import com.cq.seek.R;
import com.cq.tool.RequestTool;
import com.cq.tool.XmlTool;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SeekPollingService extends Service {

  public static HomeActivity MainActivity;
  static SeekPollListener listener;
  static boolean firstPoll = false;
  
  public static void setListener(SeekPollListener l) {
    listener = l;
  }
  
  public static void setActitvity(HomeActivity activity) {
    MainActivity = activity;
  }
  
  @Override
  public IBinder onBind (Intent intent) {
    return null;
  }

  private Timer timer = new Timer();

  public void onCreate () {
    super.onCreate();
    startService();
    //Toast.makeText(MainActivity, "My Service Created", Toast.LENGTH_LONG).show();
  }

  public static int index = 1;
  public int lastRecordedNumberOfMails = -1;
  private void startService() {
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run () {
        listener.processPollResult(fetchUnreads());
      }
    }, 0, (15 * 60 * 1000) );
  }
  final String tag = "SeekPollingService";
  public int[] fetchUnreads() {
    if( MainActivity == null ) return new int[]{0};
    int[] result = new int[3];
    final SharedPreferences sharedPrefs = MainActivity.sharedPrefs();
    String profileId = sharedPrefs.getString ("profileId", null);
    String url = SeekPollingService.this.getString(R.string.server_url) + SeekPollingService.this.getString(R.string.unreads_for_profile).replaceAll("#\\{profile_id_num\\}", profileId);
    Map<String, String> params = new HashMap<String, String>();
    if(firstPoll) {
      firstPoll = false;
      params.put("ignoreLastActivity", "1");
    }
    Document doc = RequestTool.getInstance(sharedPrefs).makeGetRequest(url, params, 200);
    NodeList children = doc.getElementsByTagName("new-seek-requests");
    int len = children == null ? 0 : children.getLength();
    int res = 0;
    if(len > 0) {
      Element node = (Element) children.item(0);
     res = Integer.parseInt(XmlTool.getSimpleElementText(node));
    }
    
    result[SeekPollListener.NewSeekRequests] = res;
    
    res = 0;
    children = doc.getElementsByTagName("unread-seek-requests");
    len = children == null ? 0 : children.getLength();
    if(len > 0) {
      Element node = (Element) children.item(0);
      res = Integer.parseInt(XmlTool.getSimpleElementText(node));
    }
    result[SeekPollListener.UnreadSeekRequests]  = res;
    
    res = 0;
    children = doc.getElementsByTagName("new-seek-responses");
    len = children == null ? 0 : children.getLength();
    if(len > 0) {
      Element node = (Element) children.item(0);
      res =Integer.parseInt(XmlTool.getSimpleElementText(node));
    }
    result[SeekPollListener.NewSeekResponses] = res;
    
    Log.i(tag, "###############seekupdates: " + result[SeekPollListener.NewSeekRequests]  + "" + result[SeekPollListener.UnreadSeekRequests] + "" + result[SeekPollListener.NewSeekResponses]);
    return result;
  }
  
  @Override
  public int onStartCommand (Intent intent, int flags, int startId) {
    return START_STICKY;
  }
  
  @Override
  public void onDestroy () {
    super.onDestroy();
    timer.cancel();
    Toast.makeText(MainActivity, "My Service Stopped", Toast.LENGTH_LONG).show();
  }
}
