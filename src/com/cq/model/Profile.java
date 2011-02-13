package com.cq.model;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cq.seek.R;
import com.cq.sqlite.CacheDBAdapter.DBColumn;
import com.cq.tool.ImageTool;
import com.cq.tool.RequestTool;
import com.cq.tool.XmlTool;

/*
 * bean to store profile attributes <profile> <cell-carrier
 * nil="true"></cell-carrier> <cell-number nil="true"></cell-number> <created-at
 * type="datetime">2009-05-16T13:33:28-07:00</created-at> <description
 * nil="true"></description> <display-name nil="true"></display-name>
 * <email>tester@cq.com</email> <id type="integer">1</id> <location-id
 * type="integer">31</location-id> <status nil="true"></status> <updated-at
 * type="datetime">2009-05-24T10:45:19-07:00</updated-at> <user-id
 * type="integer">1</user-id> <location> <address nil="true"></address>
 * <created-at type="datetime">2009-05-24T10:45:19-07:00</created-at> <id
 * type="integer">31</id> <latitude type="decimal">37.788638</latitude>
 * <longitude type="decimal">-122.189773</longitude> <updated-at
 * type="datetime">2009-05-24T10:45:19-07:00</updated-at> </location> <user>
 * <created-at type="datetime">2009-05-16T13:33:28-07:00</created-at>
 * <crypted-password>18b5b1368c0adb726899fa97303f002f8e257b94</crypted-password>
 * <email-verification nil="true"></email-verification> <email-verified
 * type="boolean">false</email-verified> <id type="integer">1</id> <is-admin
 * type="boolean">false</is-admin> <login>tester</login>
 * <remember-token>64a9ee0af4acd06d1ecdc0ad1cb61ad9f667b552</remember-token>
 * <remember-token-expires-at
 * type="datetime">2019-05-16T13:33:56-07:00</remember-token-expires-at>
 * <salt>a8ce3796ac626e0010154b9578dd69c86fb98716</salt> <updated-at
 * type="datetime">2009-05-16T13:33:56-07:00</updated-at> </user> </profile>
 */
public class Profile implements Base {

  public Profile() {

  }

  @Override
  public int hashCode () {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public boolean equals (Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Profile other = (Profile) obj;
    if (id != other.id) return false;
    return true;
  }

  private static final long serialVersionUID = -8725890838055057779L;
  String cellCarrier;
  String cellNumber;
  Date createdAt;
  String description;
  String displayName;
  String email;
  int id;
  String status;
  Date updatedAt;
  User user;
  Location location;
  boolean isFriend;
  BigDecimal distance;
  int tier;
  String photoFileName;
  String photoContentType;
  Bitmap photo;

  @Override
  public String toString () {
    return "{" + "cellCarrier: " + cellCarrier + "," + "cellNumber: " + cellNumber + "," + "createdAt: " + createdAt + ", " + "description: " + description + "," + "displayName: " + displayName + "," + "email: " + email + "," + "id: " + id + "," + "status: " + status + "," + "updatedAt: " + updatedAt + "," + "user => " + user.toString() + "," + "location =>" + location.toString() + "," + "isFriend" + isFriend + "}";
  }

  public static List<Profile> constructFromXml (Document doc) {
    List<Profile> profiles = new ArrayList<Profile>();

    if (doc != null) {
      NodeList children = doc.getElementsByTagName("profile");
      int len = children == null ? 0 : children.getLength();
      for(int ii = 0; ii < len; ii++) {
        Element profileNode = (Element) children.item(ii);
        Profile profile = Profile.constructFromXml(profileNode);
        profiles.add(profile);
      }
    }

    return profiles;
  }

  public static Profile constructFromXml (Element node) {
    if (node != null) {
      Profile profile = new Profile();

      profile.cellCarrier = XmlTool.getSimpleElementText(node, "cell-carrier");
      profile.cellNumber = XmlTool.getSimpleElementText(node, "cell-number");
      profile.description = XmlTool.getSimpleElementText(node, "description");
      profile.displayName = XmlTool.getSimpleElementText(node, "display-name");
      profile.email = XmlTool.getSimpleElementText(node, "email");
      profile.isFriend = new Boolean(XmlTool.getSimpleElementText(node, "is-friends-with-current-profile"));
      if (XmlTool.getFirstElement(node, "distance-from-current-profile") != null) {
        profile.distance = new BigDecimal(XmlTool.getSimpleElementText(node, "distance-from-current-profile"), new MathContext(2));
      }
      else {
        profile.distance = BigDecimal.ZERO;
      }
      if (XmlTool.getFirstElement(node, "current-user-tier") != null) {
        profile.tier = Integer.parseInt(XmlTool.getSimpleElementText(node, "current-user-tier"));
      }
      else {
        profile.tier = 0;
      }
      String idStr = XmlTool.getSimpleElementText(node, "id");
      idStr = (idStr == null || idStr.trim().length() == 0) ? "0" : idStr.trim();
      profile.id = new Integer(idStr);

      Element locElement = XmlTool.getFirstElement(node, "location");
      if (locElement != null) {
        profile.location = Location.constructFromXml(locElement);
      }

      Element userElement = XmlTool.getFirstElement(node, "user");
      if (userElement != null) {
        profile.user = User.constructFromXml(userElement);
      }

      profile.photoFileName = XmlTool.getSimpleElementText(node, "photo-file-name");
      profile.photoContentType = XmlTool.getSimpleElementText(node, "photo-content-type");
      profile.status = XmlTool.getSimpleElementText(node, "status");

      return profile;
    }

    return null;
  }

  public static Profile constructProfile (Cursor crsr) {
    Profile p = null;
    if (crsr != null) {
      p = new Profile();
      p.setId(crsr.getInt(DBColumn.ProfileId.ordinal()));
      p.setDisplayName(crsr.getString(DBColumn.ProfileName.ordinal()));
      p.setEmail(crsr.getString(DBColumn.Email.ordinal()));
      p.setCellNumber(crsr.getString(DBColumn.CellNumber.ordinal()));
      p.setStatus(crsr.getString(DBColumn.Status.ordinal()));
      p.setTier(crsr.getInt(DBColumn.Tier.ordinal()));
      p.setPhotoFileName(crsr.getString(DBColumn.PhotoFileName.ordinal()));
      p.setPhotoContentType(crsr.getString(DBColumn.PhotoContentType.ordinal()));
      byte[] bb = crsr.getBlob(DBColumn.PhotoBlob.ordinal());
      if (bb != null) {
        p.setPhoto(BitmapFactory.decodeByteArray(bb, 0, bb.length));
      }

      User u = new User();
      u.setId(crsr.getInt(DBColumn.UserId.ordinal()));
      u.setLogin(crsr.getString(DBColumn.UserName.ordinal()));
      p.setUser(u);
      String latStr = crsr.getString(DBColumn.LocationX.ordinal());
      String lngStr = crsr.getString(DBColumn.LocationY.ordinal());
      
      if(latStr != null && latStr.trim().length() > 0 && lngStr != null && lngStr.trim().length() > 0) {
        Location loc = new Location();
        loc.setLatitude(new BigDecimal(latStr));
        loc.setLongitude(new BigDecimal(lngStr));
        p.setLocation(loc);
      }
    }

    return p;
  }

  public boolean isFriend () {
    return isFriend;
  }

  public void setFriend (boolean isFriend) {
    this.isFriend = isFriend;
  }

  public String getCellCarrier () {
    return cellCarrier;
  }

  public void setCellCarrier (String cellCarrier) {
    this.cellCarrier = cellCarrier;
  }

  public String getCellNumber () {
    return cellNumber;
  }

  public void setCellNumber (String cellNumber) {
    this.cellNumber = cellNumber;
  }

  public Date getCreatedAt () {
    return createdAt;
  }

  public void setCreatedAt (Date createdAt) {
    this.createdAt = createdAt;
  }

  public String getDescription () {
    return description;
  }

  public void setDescription (String description) {
    this.description = description;
  }

  public String getDisplayName () {
    return displayName;
  }

  public void setDisplayName (String displayName) {
    this.displayName = displayName;
  }

  public String getEmail () {
    return email;
  }

  public void setEmail (String email) {
    this.email = email;
  }

  public int getId () {
    return id;
  }

  public void setId (int id) {
    this.id = id;
  }

  public String getStatus () {
    return status;
  }

  public void setStatus (String status) {
    this.status = status;
  }

  public Date getUpdatedAt () {
    return updatedAt;
  }

  public void setUpdatedAt (Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public User getUser () {
    return user;
  }

  public void setUser (User user) {
    this.user = user;
  }

  public Location getLocation () {
    return location;
  }

  public void setLocation (Location location) {
    this.location = location;
  }

  public String getProfileIdNameString () {
    return id + "-" + user.login;
  }

  public BigDecimal getDistance () {
    return distance;
  }

  public void setDistance (BigDecimal distance) {
    this.distance = distance;
  }

  public int getTier () {
    return tier;
  }

  public void setTier (int tier) {
    this.tier = tier;
  }

  public Bitmap getPhoto () {
    return photo;
  }

  public void setPhoto (Bitmap photo) {
    this.photo = photo;
  }

  public String getPhotoFileName () {
    return photoFileName;
  }

  public void setPhotoFileName (String photoFileName) {
    this.photoFileName = photoFileName;
  }

  public String getPhotoContentType () {
    return photoContentType;
  }

  public void setPhotoContentType (String photoContentType) {
    this.photoContentType = photoContentType;
  }

  // defaults to jpg
  public String photoFileExtension () {
    String type = "jpg";
    if (photoContentType != null) {
      String[] array = photoContentType.split("/");
      if (array != null && array.length > 1) {
        type = array[1];
        if ("jpeg".equalsIgnoreCase(type)) {
          type = "jpg";
        }
        if ("octet-stream".equalsIgnoreCase(type)) {
          type = "png";
        }
      }
    }

    return type;
  }

  public String displayNameOrLogin () {
    String result = getUser().getLogin();
    if (getDisplayName() != null && getDisplayName().length() > 0) {
      result = getDisplayName();
    }

    return result;
  }

  public String getPhotoFileUrl () {
    return "/images/" + "/thumb/" + getProfileIdNameString() + "." + photoFileExtension();
  }

  public Bitmap getPhotoFromDevice () {
    return BitmapFactory.decodeFile("/data/data/com.cq/files/" + getProfileIdNameString() + "." + photoFileExtension());
  }

  public Bitmap getPhotoLongVersion (Context context, int width, int height) {
    // first, make sure that the profile doesn't have photo attached to it.
    photo = getPhoto();
    if (photo == null && getPhotoFileName() != null) {
      // second make sure the device doesn't have this photo file
      photo = BitmapFactory.decodeFile("/data/data/com.cq/files/" + getProfileIdNameString() + "." + photoFileExtension());
      if (photo == null && context != null) {
        // third, download the file
        String fileUrl = context.getString(R.string.server_url) + getPhotoFileUrl();
        String prefFileName = context.getText(R.string.pref_file_name).toString();
        SharedPreferences prefs = context.getSharedPreferences(prefFileName, Activity.MODE_PRIVATE);
        photo = RequestTool.getInstance(prefs).downloadImage(fileUrl);

      }
    }

    //finally, assign the default one
    if (photo == null) {
      photo = BitmapFactory.decodeFile("/data/data/com.cq/files/default_photo.jpg");
    }

    if(height != -1 && width != -1) {
      photo = ImageTool.resize(photo, height, width);
    }
    return photo;
  }

  public int describeContents () {
    return 0;
  }

  /**
   * String cellCarrier; String cellNumber; Date createdAt; String description;
   * String displayName; String email; int id; String status; Date updatedAt;
   * User user; Location location; boolean isFriend; BigDecimal distance; int
   * tier; String photoFileName; String photoContentType; Bitmap photo;
   * 
   * @return
   */
  public ContentValues contentValues () {
    ContentValues values = new ContentValues();
    values.put(DBColumn.ProfileId.toString(), id);
    values.put(DBColumn.Email.toString(), email);
    values.put(DBColumn.CellNumber.toString(), cellNumber);
    values.put(DBColumn.ProfileName.toString(), displayName);
    values.put(DBColumn.UserId.toString(), user.id);
    values.put(DBColumn.UserName.toString(), user.login);
    values.put(DBColumn.LocationX.toString(), location != null ? location.latitude.toPlainString() : "");
    values.put(DBColumn.LocationY.toString(), location != null ? location.longitude.toPlainString() : "");
    values.put(DBColumn.Status.toString(), status);
    values.put(DBColumn.Tier.toString(), tier);
    values.put(DBColumn.PhotoFileName.toString(), photoFileName);
    values.put(DBColumn.PhotoContentType.toString(), photoContentType);
    if (photo != null) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
      values.put(DBColumn.PhotoBlob.toString(), out.toByteArray());
    }
    else {
      values.putNull(DBColumn.PhotoBlob.toString());
    }

    return values;
  }

}
