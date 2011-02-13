package com.cq.model;

import java.math.BigDecimal;
import java.util.Date;

import org.w3c.dom.Element;

import com.cq.tool.StringTool;
import com.cq.tool.XmlTool;
import com.google.android.maps.GeoPoint;

/*
 * represents the following structure
 * 
 * <location> <address nil="true"></address> <created-at
 * type="datetime">2009-05-24T10:45:19-07:00</created-at> <id
 * type="integer">31</id> <latitude type="decimal">37.788638</latitude>
 * <longitude type="decimal">-122.189773</longitude> <updated-at
 * type="datetime">2009-05-24T10:45:19-07:00</updated-at> </location>
 */
public class Location implements Base {
  private static final long serialVersionUID = -8272629459607687539L;
  final static String Tag = Location.class.toString();
  String address;
  Date createdAt;
  int id;
  BigDecimal latitude;
  BigDecimal longitude;
  Date updatedAt;

  public static Location constructFromXml (Element node) {
    if (node != null && "location".equals(node.getNodeName())) {
      Location loc = new Location();

      loc.address = XmlTool.getSimpleElementText(node, "address");
      String idStr = XmlTool.getSimpleElementText(node, "id");
      idStr = (idStr == null || idStr.trim().length() == 0) ? "-1" : idStr.trim();
      loc.id = new Integer(idStr);
      String latStr = XmlTool.getSimpleElementText(node, "latitude");
      loc.latitude = new BigDecimal(StringTool.getNonNullString(latStr, "0"));
      String lngStr = XmlTool.getSimpleElementText(node, "longitude");
      loc.longitude = new BigDecimal(StringTool.getNonNullString(lngStr, "0"));
      return loc;
    }

    return null;
  }

  @Override
  public String toString () {
    return "{" + "address: " + address + "," + "createdAt: " + createdAt + "," + "id: " + id + "," + "latitude: " + latitude + "," + "longitude: " + longitude + "," + "updatedAt: " + updatedAt + "," + "}";
  }

  public String latAndLangStr () {
    return (latitude != null ? latitude.toEngineeringString() : "") + "," + (longitude != null ? longitude.toEngineeringString() : "");
  }

  public String getAddress () {
    return address;
  }

  public void setAddress (String address) {
    this.address = address;
  }

  public Date getCreatedAt () {
    return createdAt;
  }

  public void setCreatedAt (Date createdAt) {
    this.createdAt = createdAt;
  }

  public int getId () {
    return id;
  }

  public void setId (int id) {
    this.id = id;
  }

  public BigDecimal getLatitude () {
    return latitude;
  }

  public void setLatitude (BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude () {
    return longitude;
  }

  public void setLongitude (BigDecimal longitude) {
    this.longitude = longitude;
  }

  public Date getUpdatedAt () {
    return updatedAt;
  }

  public void setUpdatedAt (Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public GeoPoint getGeoPoint () {
    if (latitude != null && longitude != null) {
      return new GeoPoint((int) (latitude.doubleValue() * 1e6), (int) (longitude.doubleValue() * 1e6));
    }

    return null;
  }

}
