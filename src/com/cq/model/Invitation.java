package com.cq.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.cq.tool.XmlTool;

/**
 * <invitation> <created-at
 * type="datetime">2009-07-16T09:04:25-07:00</created-at> <from-profile-id
 * type="integer">2</from-profile-id> <id type="integer">10</id> <status
 * type="boolean">false</status> <to-profile-id type="integer">1</to-profile-id>
 * <updated-at type="datetime">2009-07-16T09:04:25-07:00</updated-at>
 * <from-profile> <cell-carrier nil="true"></cell-carrier> <cell-number
 * nil="true"></cell-number> <created-at
 * type="datetime">2009-05-16T14:47:09-07:00</created-at> <description
 * nil="true"></description> <display-name nil="true"></display-name>
 * <email>tester2@cq.com</email> <id type="integer">2</id> <location-id
 * type="integer">37</location-id> <status nil="true"></status> <updated-at
 * type="datetime">2009-05-24T11:05:14-07:00</updated-at> <user-id
 * type="integer">2</user-id> <user> <created-at
 * type="datetime">2009-05-16T14:47:09-07:00</created-at>
 * <crypted-password>0e4079d36096a083b86a56f46b625b9dc6b58986</crypted-password>
 * <email-verification nil="true"></email-verification> <email-verified
 * type="boolean">false</email-verified> <id type="integer">2</id> <is-admin
 * type="boolean">false</is-admin> <login>tester2</login> <remember-token
 * nil="true"></remember-token> <remember-token-expires-at type="datetime"
 * nil="true"></remember-token-expires-at>
 * <salt>4b72bc4c12ce81d8b6d45efcb14e3d2f7bf43c90</salt> <updated-at
 * type="datetime">2009-05-16T14:47:09-07:00</updated-at> </user> <location>
 * <address nil="true"></address> <created-at
 * type="datetime">2009-05-26T20:48:09-07:00</created-at> <id
 * type="integer">37</id> <latitude type="decimal">41.907401</latitude>
 * <longitude type="decimal">-87.632096</longitude> <updated-at
 * type="datetime">2009-05-26T20:48:09-07:00</updated-at> </location>
 * </from-profile> </invitation>
 * 
 * @author santoash
 */
public class Invitation implements Base {
  private static final long serialVersionUID = -6527508557621177725L;
  public String createdAt;
  public Profile fromProfile;
  public Profile toProfile;
  public boolean status;
  public int id;

  public static Invitation constructFromXml (Element node) {
    Invitation invitation = null;
    if (node != null) {
      invitation = new Invitation();
      invitation.createdAt = XmlTool.getSimpleElementText(node, "created-at");
      invitation.id = Integer.parseInt(XmlTool.getSimpleElementText(node, "id"));

      Element fromProfileElement = XmlTool.getFirstElement(node, "from-profile");
      if (fromProfileElement != null) {
        invitation.fromProfile = Profile.constructFromXml(fromProfileElement);
      }
    }

    return invitation;

  }

  public static List<Invitation> constructFromXml (Document doc) {

    List<Invitation> invitations = new ArrayList<Invitation>();

    if (doc != null) {
      NodeList children = doc.getElementsByTagName("invitation");
      int len = children == null ? 0 : children.getLength();
      for(int ii = 0; ii < len; ii++) {
        Element invitationNode = (Element) children.item(ii);
        Invitation invitation = constructFromXml(invitationNode);
        invitations.add(invitation);
      }
    }

    return invitations;
  }
}
