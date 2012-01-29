package com.cq.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.cq.tool.XmlTool;

public class SeekRequest implements Base {

  private static final long serialVersionUID = -8924907035455709897L;

  Seek seek;
  Profile seekedProfile;
  Integer id;
  Boolean accepted;
  Message message;

  public static List<SeekRequest> constructFromXml (Document doc) {
    List<SeekRequest> seekRequests = new ArrayList<SeekRequest>();

    if (doc != null) {
      NodeList children = doc.getElementsByTagName("seek-request");
      int len = children == null ? 0 : children.getLength();
      for(int ii = 0; ii < len; ii++) {
        Element seekRequestNode = (Element) children.item(ii);
        SeekRequest seekRequest = SeekRequest.constructFromXml(seekRequestNode);
        seekRequests.add(seekRequest);
      }
    }

    return seekRequests;
  }

  public static SeekRequest constructFromXml (Element node) {
    SeekRequest seekRequest = null;

    if (node != null) {
      seekRequest = new SeekRequest();

      String idStr = XmlTool.getSimpleElementText(node, "id");
      idStr = (idStr == null || idStr.trim().length() == 0) ? "0" : idStr.trim();
      seekRequest.id = new Integer(idStr);

      Element seekElement = XmlTool.getFirstElement(node, "seek");
      seekRequest.seek = Seek.constructFromXml(seekElement);

      Element seekedProfileElement = XmlTool.getFirstElement(node, "seeked-profile");
      seekRequest.seekedProfile = Profile.constructFromXml(seekedProfileElement);
      
      Element msgElement = XmlTool.getFirstElement(node, "message");
      seekRequest.message = Message.constructFromXml(msgElement);

      seekRequest.accepted = new Boolean(XmlTool.getSimpleElementText(node, "is-accepted"));
    }

    return seekRequest;
  }

  public Seek getSeek () {
    return seek;
  }

  public void setSeek (Seek seek) {
    this.seek = seek;
  }

  public Profile getSeekedProfile () {
    return seekedProfile;
  }

  public void setSeekedProfile (Profile seekedProfile) {
    this.seekedProfile = seekedProfile;
  }

  public Integer getId () {
    return id;
  }

  public void setId (Integer id) {
    this.id = id;
  }

  public Boolean getAccepted () {
    return accepted;
  }

  public void setAccepted (Boolean a) {
    this.accepted = a;
  }
  
  public Message getMessage () {
    return message;
  }

  public void setMessage (Message message) {
    this.message = message;
  }

}
