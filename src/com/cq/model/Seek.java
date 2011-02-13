package com.cq.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.cq.tool.XmlTool;

public class Seek implements Base {

  private static final long serialVersionUID = -6291473728654523703L;

  Integer id;
  Boolean isActive;
  Message message;
  Profile owner;
  Set<Profile> seekedProfiles;
  Location location;

  public static List<Seek> constructFromXml (Document doc) {
    List<Seek> seeks = new ArrayList<Seek>();

    if (doc != null) {
      NodeList children = doc.getElementsByTagName("seek");
      int len = children == null ? 0 : children.getLength();
      for(int ii = 0; ii < len; ii++) {
        Element seekNode = (Element) children.item(ii);
        Seek seek = Seek.constructFromXml(seekNode);
        seeks.add(seek);
      }
    }

    return seeks;
  }

  public static Seek constructFromXml (Element node) {
    Seek seek = null;
    if (node != null) {
      seek = new Seek();

      String idStr = XmlTool.getSimpleElementText(node, "id");
      idStr = (idStr == null || idStr.trim().length() == 0) ? "0" : idStr.trim();
      seek.id = new Integer(idStr);

      String isActiveStr = XmlTool.getSimpleElementText(node, "is-active");
      isActiveStr = (isActiveStr == null || isActiveStr.trim().length() == 0) ? "false" : isActiveStr.trim();
      seek.isActive = Boolean.valueOf(isActiveStr);

      Element ownerElement = XmlTool.getFirstElement(node, "owner");
      seek.owner = Profile.constructFromXml(ownerElement);

      Element msgElement = XmlTool.getFirstElement(node, "message");
      seek.message = Message.constructFromXml(msgElement);

      Element seekRequestsElement = XmlTool.getFirstElement(node, "seek-requests");
      if (seekRequestsElement != null) {
        NodeList seekRequestElements = seekRequestsElement.getElementsByTagName("seek-request");
        Set<Profile> seekedProfiles = new HashSet<Profile>();
        for(int ii = 0; ii < seekRequestElements.getLength(); ii++) {
          Element seekRequestElement = (Element) seekRequestElements.item(ii);
          NodeList seekedProfileElements = seekRequestElement.getElementsByTagName("seeked-profile");
          for(int jj = 0; jj < seekedProfileElements.getLength(); jj++) {
            Profile seekedProfile = Profile.constructFromXml((Element) seekedProfileElements.item(jj));
            seekedProfiles.add(seekedProfile);
          }
        }
        seek.seekedProfiles = seekedProfiles;
      }
    }

    return seek;
  }

  public Integer getId () {
    return id;
  }

  public void setId (Integer id) {
    this.id = id;
  }

  public Boolean isActive () {
    return isActive;
  }

  public void setIsActive (Boolean isActive) {
    this.isActive = isActive;
  }

  public Message getMessage () {
    return message;
  }

  public void setMessage (Message message) {
    this.message = message;
  }

  public Profile getOwner () {
    return owner;
  }

  public void setOwner (Profile owner) {
    this.owner = owner;
  }

  public Set<Profile> getSeekedProfiles () {
    return seekedProfiles;
  }

  public void setSeekedProfiles (Set<Profile> seekedProfiles) {
    this.seekedProfiles = seekedProfiles;
  }

}
