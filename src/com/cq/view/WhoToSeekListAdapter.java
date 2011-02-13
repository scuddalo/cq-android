package com.cq.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cq.model.Profile;
import com.cq.seek.R;
import com.cq.sqlite.CacheDBAdapter;
import com.cq.tool.GeoTools;

public class WhoToSeekListAdapter extends ArrayAdapter<Profile> {

  public List<Profile> profiles;
  Context context;

  public WhoToSeekListAdapter(Context context, int textViewResourceId, ArrayList<Profile> profiles) {
    super(context, textViewResourceId, profiles);
    this.profiles = profiles;
    this.context = context;
  }

  @Override
  public View getView (int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.who_to_seek_row, null);

      holder = new ViewHolder();
      holder.photoView = (ImageView) convertView.findViewById(R.id.who_to_seek_row_photo);
      holder.nameView = (TextView) convertView.findViewById(R.id.who_to_seek_row_name);
      holder.distanceView = (TextView) convertView.findViewById(R.id.who_to_seek_row_distance_txt);
      holder.tierImage = (ImageView) convertView.findViewById(R.id.who_to_seek_tier_img);
      holder.isSelectedImage = (ImageView) convertView.findViewById(R.id.who_to_seek_row_selected_img);

      convertView.setTag(holder);
    }
    else {
      holder = (ViewHolder) convertView.getTag();
    }

    Profile profile = profiles.get(position);
    if (profile != null) {
      holder.nameView.setText(profile.displayNameOrLogin());

      BitmapDrawable drawable = new BitmapDrawable(profile.getPhotoLongVersion(context, 50, 50));
      holder.photoView.setImageDrawable(drawable);

      if (profile.getLocation() != null) {
        String prefFileName = context.getText(R.string.pref_file_name).toString();
        SharedPreferences prefs = context.getSharedPreferences(prefFileName, Activity.MODE_PRIVATE);
        String tmp = prefs.getString("profileId", null);
        String[] profileIdDetails = tmp.split("-");
        int profileId = Integer.parseInt(profileIdDetails[0]);
        CacheDBAdapter cache = new CacheDBAdapter (context);
        cache.open();
        Profile currentUserProfile = cache.getProfile(profileId);
        cache.close();
        String str = GeoTools.printableDistanceInKm(currentUserProfile.getLocation().getGeoPoint(), profile.getLocation().getGeoPoint());
        holder.distanceView.setText(str);
      }

      holder.tierImage.setImageResource(getTierImageResourceId(profile.getTier()));

    }

    return convertView;
  }

  public class ViewHolder {
    ImageView photoView;
    TextView nameView, distanceView;
    ImageView tierImage, isSelectedImage;
  }

  public int getTierImageResourceId (int tier) {
    int result;
    switch(tier) {
      case 1:
        result = R.drawable.cq_tier1_icon;
        break;
      case 2:
        result = R.drawable.cq_tier2_icon;
        break;
      default:
        result = R.drawable.cq_tier3_icon;
    }

    return result;
  }

}
