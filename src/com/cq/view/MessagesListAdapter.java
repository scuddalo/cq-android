package com.cq.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cq.model.Message;
import com.cq.model.Profile;
import com.cq.model.Seek;
import com.cq.model.SeekRequest;
import com.cq.seek.R;

public class MessagesListAdapter extends ArrayAdapter<SeekRequest>  {
  public static final String TAG = "MessagesListAdapter";
  ArrayList<SeekRequest> seekRequests;
  Context context;

  public MessagesListAdapter(Context context, int textViewResourceId, ArrayList<SeekRequest> seekRequests) {
    super(context, textViewResourceId, seekRequests);
    this.seekRequests = seekRequests;
    this.context = context;

  }

  @Override
  public View getView (final int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.messages_row, null);

      holder = new ViewHolder();
      holder.messagePreviewText = (TextView) convertView.findViewById(R.id.messages_row_preview_txt);
      holder.messageFromText = (TextView) convertView.findViewById(R.id.message_row_from_txt);
      holder.expandBtn = (ImageButton) convertView.findViewById(R.id.messages_row_expand_btn);
      holder.unreadDot = (ImageView) convertView.findViewById(R.id.unread_red_dot);
      holder.acceptedImg  = (ImageView) convertView.findViewById(R.id.cq_accepted_img);

      convertView.setTag(holder);
    }
    else {
      holder = (ViewHolder) convertView.getTag();
    }

    SeekRequest seekRequest = seekRequests.get(position);
    
    if (seekRequest != null && seekRequest.getSeek() != null) {
      Seek seek = seekRequest.getSeek();
      final Profile seekOwner = seek.getOwner();

      if (seekOwner != null) {
        // first, name and photo...
        holder.messageFromText.setText(seekOwner.displayNameOrLogin());
        BitmapDrawable drawable = new BitmapDrawable(seekOwner.getPhotoLongVersion(context, 30, 30));
        holder.messageFromText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        // third message ...
        Message seekMsg = null;
        if ((seekMsg = seekRequest.getMessage()) != null) {
          holder.messagePreviewText.setText(seekMsg.getContent());
          
          // display read status of seekMsg 
          // make sure at any point in time, only of the
          // accepted / unread img is shown
          if (!seekMsg.isRead()) {
            holder.unreadDot.setVisibility(View.VISIBLE);
          }
          else {
            holder.unreadDot.setVisibility(View.INVISIBLE);
          }
          
          //display if seekmsg is accepted or not
          if(seekRequest.getAccepted()) {
            holder.acceptedImg.setVisibility(View.VISIBLE);
            //set the unreadDot to Gone to save real estate
            holder.unreadDot.setVisibility(View.GONE);
          }
          else {
              // we set it to gone to save the real estate
              holder.acceptedImg.setVisibility(View.GONE);
           }
        }

        holder.expandBtn.setOnClickListener(new View.OnClickListener()
        {
          public void onClick (View v) {
            
          }
        });
        // finally, the accept and reject buttons
//        holder.acceptBtn.setOnClickListener(new OnClickListener()
//        {
//          public void onClick (View v) {
//            Thread t = new Thread(null, new AcceptRejectRunnable(position, true, null), "MagentoBackground");
//            t.start();
//            ((MessagesListActivity) context).showProgressDialog("Accepting seek from " + seekOwner.displayNameOrLogin());
//          }
//        });
//
//        holder.rejectBtn.setOnClickListener(new OnClickListener()
//        {
//          public void onClick (View v) {
//            Thread t = new Thread(null, new AcceptRejectRunnable(position, false, null), "MagentoBackground");
//            t.start();
//            ((MessagesListActivity) context).showProgressDialog("Rejecting seek from " + seekOwner.displayNameOrLogin());
//          }
//        });
//
//        // choice has been made before and request has been accepted
//        if (seekRequest.getAccepted() != null && seekRequest.getAccepted()) {
//          holder.acceptBtn.setVisibility(View.INVISIBLE);
//          holder.rejectBtn.setVisibility(View.VISIBLE);
//        }
//        else if (seekRequest.getAccepted() != null && !seekRequest.getAccepted()) {
//          holder.acceptBtn.setVisibility(View.VISIBLE);
//          holder.rejectBtn.setVisibility(View.INVISIBLE);
//        }
      }
    }

    return convertView;
  }

  class ViewHolder {
    public ImageButton expandBtn;
    public ImageView unreadDot, acceptedImg;
    public TextView messagePreviewText, messageFromText;
  }

  public int getTierImageResourceId (int tier) {
    int result;
    switch(tier) {
      case 1:
        result = R.drawable.tier_1;
        break;
      case 2:
        result = R.drawable.tier_2;
        break;
      default:
        result = R.drawable.tier_3;
    }

    return result;
  }

  
}
