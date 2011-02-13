package com.cq.model;

import java.util.Date;

import org.w3c.dom.Element;

import com.cq.tool.StringTool;
import com.cq.tool.XmlTool;

/*
 * represents the following structure
 * 
 * <user> <created-at type="datetime">2009-05-16T13:33:28-07:00</created-at>
 * <crypted-password>18b5b1368c0adb726899fa97303f002f8e257b94</crypted-password>
 * <email-verification nil="true"></email-verification> <email-verified
 * type="boolean">false</email-verified> <id type="integer">1</id> <is-admin
 * type="boolean">false</is-admin> <login>tester</login>
 * <remember-token>64a9ee0af4acd06d1ecdc0ad1cb61ad9f667b552</remember-token>
 * <remember-token-expires-at
 * type="datetime">2019-05-16T13:33:56-07:00</remember-token-expires-at>
 * <salt>a8ce3796ac626e0010154b9578dd69c86fb98716</salt> <updated-at
 * type="datetime">2009-05-16T13:33:56-07:00</updated-at> </user>
 */
public class User implements Base {
  private static final long serialVersionUID = -7896196139508364232L;
  int id;
  Date createdAt;
  String cryptedPassword;
  String emailVerification;
  boolean emailVerified;
  boolean isAdmin;
  String login;
  String rememberToken;
  Date rememberTokenExpiresAt;
  String salt;
  Date updatedAt;

  public static User constructFromXml (Element node) {
    if (node != null && "user".equals(node.getNodeName())) {
      User user = new User();

      String idStr = XmlTool.getSimpleElementText(node, "id");
      user.id = new Integer(StringTool.getNonNullString(idStr, "-1"));
      user.cryptedPassword = XmlTool.getSimpleElementText(node, "crypted-password");
      user.emailVerification = XmlTool.getSimpleElementText(node, "email-verification");
      user.emailVerified = new Boolean(StringTool.getNonNullString(XmlTool.getSimpleElementText(node, "email-verified"), "false"));
      user.isAdmin = new Boolean(StringTool.getNonNullString(XmlTool.getSimpleElementText(node, "is-admin"), "false"));
      user.rememberToken = XmlTool.getSimpleElementText(node, "remember-token");
      // TODO: rememberTokenExpiresAt should be taken care of . we should
      // change the date format
      // to something java friendly on the rails side
      user.salt = XmlTool.getSimpleElementText(node, "salt");
      user.login = XmlTool.getSimpleElementText(node, "login");
      return user;
    }

    return null;
  }

  @Override
  public String toString () {
    return "{" + "id: " + id + "," + "createdAt: " + createdAt + "," + "cryptedPassword: " + cryptedPassword + "," + "emailVerification: " + emailVerification + "," + "emailVerified: " + emailVerified + "," + "isAdmin: " + isAdmin + "," + "login: " + login + "," + "rememberToken: " + rememberToken + "," + "rememberTokenExpiresAt: " + rememberTokenExpiresAt + "," + "salt: " + salt + "," + "updatedAt: " + updatedAt + "," + "}";
  }

  public int getId () {
    return id;
  }

  public void setId (int id) {
    this.id = id;
  }

  public Date getCreatedAt () {
    return createdAt;
  }

  public void setCreatedAt (Date createdAt) {
    this.createdAt = createdAt;
  }

  public String getCryptedPassword () {
    return cryptedPassword;
  }

  public void setCryptedPassword (String cryptedPassword) {
    this.cryptedPassword = cryptedPassword;
  }

  public String getEmailVerification () {
    return emailVerification;
  }

  public void setEmailVerification (String emailVerification) {
    this.emailVerification = emailVerification;
  }

  public boolean isEmailVerified () {
    return emailVerified;
  }

  public void setEmailVerified (boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public boolean isAdmin () {
    return isAdmin;
  }

  public void setAdmin (boolean isAdmin) {
    this.isAdmin = isAdmin;
  }

  public String getLogin () {
    return login;
  }

  public void setLogin (String login) {
    this.login = login;
  }

  public String getRememberToken () {
    return rememberToken;
  }

  public void setRememberToken (String rememberToken) {
    this.rememberToken = rememberToken;
  }

  public Date getRememberTokenExpiresAt () {
    return rememberTokenExpiresAt;
  }

  public void setRememberTokenExpiresAt (Date rememberTokenExpiresAt) {
    this.rememberTokenExpiresAt = rememberTokenExpiresAt;
  }

  public String getSalt () {
    return salt;
  }

  public void setSalt (String salt) {
    this.salt = salt;
  }

  public Date getUpdatedAt () {
    return updatedAt;
  }

  public void setUpdatedAt (Date updatedAt) {
    this.updatedAt = updatedAt;
  }

}
