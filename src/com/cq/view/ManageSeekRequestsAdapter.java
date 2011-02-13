package com.cq.view;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cq.model.Profile;
import com.cq.model.SeekRequest;
import com.cq.seek.R;
import com.cq.sqlite.CacheDBAdapter;
import com.cq.tool.GeoTools;
import com.cq.tool.LocationTool;

public class ManageSeekRequestsAdapter extends ArrayAdapter<SeekRequest> {

  Context context;
  List<SeekRequest> seekRequests;

  public ManageSeekRequestsAdapter(Context context, int textViewResourceId, ArrayList<SeekRequest> seekRequests) {
    super(context, textViewResourceId, seekRequests);
    this.context = context;
    this.seekRequests = seekRequests;
  }

  @Override
  public View getView (int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.manage_seek_row, null);

      holder = new ViewHolder();
      holder.nameText = (TextView) convertView.findViewById(R.id.manage_seek_profile_name_text);
      holder.locationText = (TextView) convertView.findViewById(R.id.manage_seek_location_text);
      holder.accepted = (ImageView) convertView.findViewById(R.id.manage_seek_row_accepted_img);
      holder.photoView = (ImageView) convertView.findViewById(R.id.manage_seek_row_photo);
      holder.callBtn = (ImageButton) convertView.findViewById(R.id.manage_seek_row_call_btn);
      holder.acceptedContainer = (LinearLayout) convertView.findViewById(R.id.manage_seek_row_accepted_container);

      convertView.setTag(holder);
    }
    else {
      holder = (ViewHolder) convertView.getTag();
    }

    SeekRequest seekRequest = seekRequests.get(position);
    if (seekRequest != null) {
      Profile seekedProfile = seekRequest.getSeekedProfile();
      if (seekedProfile != null) {
        holder.nameText.setText(seekedProfile.displayNameOrLogin());

        BitmapDrawable drawable = new BitmapDrawable(seekedProfile.getPhotoLongVersion(context, 25, 25));
        holder.photoView.setImageDrawable(drawable);


        String prefFileName = (String) context.getText(R.string.pref_file_name);
        SharedPreferences prefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
        String tmp = prefs.getString("profileId", null);
        String[] profileIdDetails = tmp.split("-");
        int profileId = Integer.parseInt(profileIdDetails[0]);
        CacheDBAdapter cache = new CacheDBAdapter (context);
        cache.open();
        Profile currentUserProfile = cache.getProfile(profileId);
        cache.close();
        String str = GeoTools.printableDistanceInKm(currentUserProfile.getLocation().getGeoPoint(), seekedProfile.getLocation().getGeoPoint());
        holder.locationText.setText(str);
      }

      if (seekRequest.getAccepted()) {
        holder.acceptedContainer.setVisibility(View.VISIBLE);
      }
      else {
        holder.acceptedContainer.setVisibility(View.INVISIBLE);
      }
    }

    return convertView;
  }

  public class ViewHolder {
    ImageView photoView;
    TextView nameText;
    TextView locationText;
    ImageView accepted;
    ImageButton callBtn;
    LinearLayout acceptedContainer;
  }

}
