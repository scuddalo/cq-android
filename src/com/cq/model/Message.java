package com.cq.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.cq.tool.XmlTool;

/**
 * Represents the following xml <message>
 * <content>zxczxczxczxczxzxczxczxczc</content> <created-at
 * type="datetime">2009-06-28T09:39:22-07:00</created-at> <from-profile-id
 * type="integer">8</from-profile-id> <id type="integer">9</id> <is-accepted
 * type="boolean" nil="true"></is-accepted> <read type="boolean">false</read>
 * <to-profile-id type="integer">1</to-profile-id> <updated-at
 * type="datetime">2009-06-28T09:39:22-07:00</updated-at> </message>
 * 
 * @author santoash
 */
public class Message implements Base {

  private static final long serialVersionUID = 8034275993969487215L;
  String content;
  String createdAt, updatedAt;
  int id;
  boolean isAccepted, read;
  Profile fromProfile, toProfile;

  public Message() {
  }

  @Override
  public String toString () {
    if (content != null) {
      return content.substring(0, Math.min(30, content.length())) + "...";
    }

    return "<empty msg>";
  }

  public static List<Message> constructFromXml (Document doc) {
    List<Message> messages = new ArrayList<Message>();

    if (doc != null) {
      NodeList children = doc.getElementsByTagName("message");
      int len = children == null ? 0 : children.getLength();
      for(int ii = 0; ii < len; ii++) {
        Element messageNode = (Element) children.item(ii);
        Message message = Message.constructFromXml(messageNode);
        messages.add(message);
      }
    }

    return messages;

  }

  public static Message constructFromXml (Element node) {
    Message msg = null;

    if (node != null) {
      msg = new Message();
      msg.createdAt = XmlTool.getSimpleElementText(node, "created-at");
      msg.updatedAt = XmlTool.getSimpleElementText(node, "updated-at");
      msg.read = Boolean.parseBoolean(XmlTool.getSimpleElementText(node, "read"));
      msg.isAccepted = Boolean.parseBoolean(XmlTool.getSimpleElementText(node, "is-accepted"));
      msg.id = Integer.parseInt(XmlTool.getSimpleElementText(node, "id"));
      msg.content = XmlTool.getSimpleElementText(node, "content");
      Element fromProfileElement = XmlTool.getFirstElement(node, "from-profile");
      if (fromProfileElement != null) {
        msg.fromProfile = Profile.constructFromXml(fromProfileElement);
      }

      Element toProfileElement = XmlTool.getFirstElement(node, "to-profile");
      if (toProfileElement != null) {
        msg.toProfile = Profile.constructFromXml(toProfileElement);
      }
    }

    return msg;

  }

  public String getContent () {
    return content;
  }

  public String getCreatedAt () {
    return createdAt;
  }

  public String getUpdatedAt () {
    return updatedAt;
  }

  public int getId () {
    return id;
  }

  public boolean isAccepted () {
    return isAccepted;
  }

  public boolean isRead () {
    return read;
  }
  
  public void setIsRead(boolean v) {
    read = v;
  }

  public Profile getFromProfile () {
    return fromProfile;
  }

  public Profile getToProfileId () {
    return toProfile;
  }

}
