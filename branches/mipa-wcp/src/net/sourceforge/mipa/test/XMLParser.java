/* 
 * MIPA - Middleware Infrastructure for Predicate detection in Asynchronous 
 * environments
 * 
 * Copyright (C) 2009 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the term of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.mipa.test;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class XMLParser {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            File f = new File("predicate.xml");

            DocumentBuilderFactory factory = DocumentBuilderFactory
                                                                   .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);

            NodeList elements = doc.getElementsByTagName("LP");

            if (elements != null) {
                for (int i = 0; i < elements.getLength(); i++) {
                    Node localPredicate = elements.item(i);
                    
                    for (Node node = localPredicate.getFirstChild(); node != null; node = node
                                                                                              .getNextSibling()) {
                        
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            if (node.getNodeName().equals("formula")) {
                                for (Node atom = node.getFirstChild(); atom != null; atom = atom
                                                                                                .getNextSibling()) {
                                    if (atom.getNodeType() == Node.ELEMENT_NODE) {
                                        if (atom.getNodeName().equals("atom")) {
                                            String operator = atom
                                                                  .getAttributes()
                                                                  .getNamedItem(
                                                                                "operator")
                                                                  .getNodeValue();
                                            String name = atom
                                                              .getAttributes()
                                                              .getNamedItem(
                                                                            "name")
                                                              .getNodeValue();
                                            String value = atom
                                                               .getAttributes()
                                                               .getNamedItem(
                                                                             "value")
                                                               .getNodeValue();
                                            System.out.println("operator is "
                                                               + operator);
                                            System.out.println("name is "
                                                               + name);
                                            System.out.println("value is "
                                                               + value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
