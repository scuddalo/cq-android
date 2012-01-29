package com.cq.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class RequestTool {
  private static RequestTool instance = null;
  static final String Tag = RequestTool.class.toString();
  private SharedPreferences prefs;

  private RequestTool(SharedPreferences prefs) {
    this.prefs = prefs;
  }

  private RequestTool() {
  }

  public static RequestTool getInstance (SharedPreferences prefs) {
    if (instance == null) {
      instance = new RequestTool(prefs);
    }

    return instance;
  }

  public SharedPreferences getPrefs () {
    return prefs;
  }

  public void setPrefs (SharedPreferences prefs) {
    this.prefs = prefs;
  }

  @Override
  public Object clone () throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  /**
   * make a postRequest using the authentication details from the user
   * preferences most of the post calls in this app will use this version
   * 
   * @param url
   *          - url string to make request
   * @param params
   *          - params to post to the server
   * @param expectedStatusCode
   *          - expected status code from the server to sanity check when the
   *          server responds
   * @return {@link Document} xml from server
   */
  public Document makePostRequest (String url, Map<String, String> params, int expectedStatusCode) {
    return makePostRequest(url, params, false, expectedStatusCode);
  }

  /**
   * @param url
   *          url string to make request
   * @param params
   *          map of request param name and value
   * @param useParamsInsteadOfPreferencesForAuthenticationDetails
   *          boolean if true, getAuthenticatedClient() will look for
   *          username/pwd in the <code>params</code> rather than in the
   *          sharedPreferences
   * @return an array with first element containing {@link Document} from
   *         response and the second element with int statuscode
   */
  public Document makePostRequest (String url, Map<String, String> params, boolean useParamsInsteadOfPreferencesForAuthenticationDetails, int expectedStatusCode) {
    HttpResponse res = null;
    try {
      // 1) create client and setup credentials
      String username = (useParamsInsteadOfPreferencesForAuthenticationDetails) ? params.get("login") : null;
      String passwd = (useParamsInsteadOfPreferencesForAuthenticationDetails) ? params.get("password") : null;
      DefaultHttpClient client = getAuthenticatedClient(username, passwd);

      // 2) create a post method with url
      HttpPost method = new HttpPost(new URI(url));

      // 3) setup the http req params
      List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
      if (params != null) {
        for(Entry<String, String> entry : params.entrySet()) {
          reqParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
      }
      method.setEntity(new UrlEncodedFormEntity(reqParams, HTTP.UTF_8));

      // 4) make the execute the method and get the response
      res = client.execute(method);
      Log.d(Tag, "status: " + res.getStatusLine().getStatusCode());

      // 5) parse the response and make an xml out of it.
      HttpEntity entity = res.getEntity();
      Document doc = makeXml(entity);
      Log.d(Tag, "Response: " + entity.toString());
      if (res.getStatusLine().getStatusCode() == expectedStatusCode) {
        return doc;
      }
      else {
        return null;
      }
    }
    catch(Exception e) {
      Log.e(Tag, e.toString(), e);
      return null;
    }
  }

  public Document makeGetRequest (String url, Map<String, String> params, int expectedResponseCode) {
    // 1) create client and setup credentials
    DefaultHttpClient httpClient = getAuthenticatedClient(null, null);

    try {
      // 2) prepare query string
      StringBuffer sb = new StringBuffer(url);
      if (params != null) {
        sb.append("?");
        for(Entry<String, String> entry : params.entrySet()) {
          sb.append(entry.getKey() + "=" + entry.getValue());
        }
      }

      // 3) create a post method with url
      HttpGet method = new HttpGet(new URI(sb.toString()));
      HttpResponse res = httpClient.execute(method);

      // 5) parse the response and make an xml out of it.
      HttpEntity entity = res.getEntity();
      Document doc = makeXml(entity);

      Log.d(Tag, "Response: " + entity.toString());
      if (res.getStatusLine().getStatusCode() == expectedResponseCode) {
        return doc;
      }
      else {
        return null;
      }

    }
    catch(Exception e) {
      Log.i(Tag, "error making get request to " + url + ":" + e);
      e.printStackTrace();
      return null;
    }

  }

  /**
   * make an authenticated http client using either the username and passwd
   * supplied as args or the username/password from sharedPreferences if either
   * username or passwd is null then this method will looks for login details in
   * the shared preferences
   * 
   * @param username
   * @param passwd
   * @return
   */
  public DefaultHttpClient getAuthenticatedClient (String username, String passwd) {
    //create client and setup credentials
    if (username == null || passwd == null) {
      username = prefs.getString("username", null);
      passwd = prefs.getString("password", null);
    }

    DefaultHttpClient client = new DefaultHttpClient();
    client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, passwd));
    return client;
  }

  static Document makeXml (HttpEntity entity) {
    Document doc = null;

    if (entity != null) {
      try {
        InputStream inputstream = entity.getContent();
        // String responseStr = convertStreamToString (inputstream);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(inputstream);
        inputstream.close();

      }
      catch(ParserConfigurationException e) {
        Log.e(Tag, e.toString(), e);
        e.printStackTrace();
      }
      catch(IllegalStateException e) {
        Log.e(Tag, e.toString(), e);
        e.printStackTrace();
      }
      catch(IOException e) {
        Log.e(Tag, e.toString(), e);
        e.printStackTrace();
      }
      catch(SAXException e) {
        Log.e(Tag, e.toString(), e);
        e.printStackTrace();
      }
    }

    return doc;
  }

  static String convertStreamToString (InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();

    String line = null;
    try {
      while((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        is.close();
      }
      catch(IOException e) {
        e.printStackTrace();
      }
    }

    return sb.toString();
  }

  /**
   * Downloads a image over http and decodes it using {@link BitmapFactory} to
   * return a {@link Bitmap}
   * 
   * @param fileUrl
   *          - {@link String} url to download the image from
   * @return {@link Bitmap}
   */
  public Bitmap downloadImage (String fileUrl) {
    URL myFileUrl = null;
    Bitmap image = null;
    if (fileUrl != null && fileUrl.length() > 0) {
      try {
        myFileUrl = new URL(fileUrl);
      }
      catch(MalformedURLException e) {
        Log.w("wrong url to download a file: " + fileUrl, e);
      }

      try {
        HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
        conn.setDoInput(true);
        conn.connect();
        InputStream is = conn.getInputStream();
        image = BitmapFactory.decodeStream(is);
      }
      catch(Exception e) {
        Log.d(Tag, "downloading image using url: " + fileUrl);
      }
    }

    return image;
  }

}
