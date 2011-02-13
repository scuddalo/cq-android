package com.cq.tool;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import android.util.Log;

public class XmlTool {
  private static String Tag = XmlTool.class.getName();

  public static String getSimpleElementText (Element node) {

    StringBuffer sb = new StringBuffer();

    if (node != null) {
      NodeList children = node.getChildNodes();
      for(int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (child instanceof Text) {
          sb.append(child.getNodeValue());
        }
      }
      return sb.toString();
    }
    else {
      return null;
    }

  }

  public static Element getFirstElement (Element element, String name) {
    NodeList nl = element.getElementsByTagName(name);
    if (nl.getLength() < 1) {
      Log.w(Tag, "Element: " + element + " does not contain: " + name);
      return null;
    }
    else {
      return (Element) nl.item(0);
    }
  }

  public static String getSimpleElementText (Element node, String name) {
    Element namedElement = getFirstElement(node, name);
    return getSimpleElementText(namedElement);
  }

}
