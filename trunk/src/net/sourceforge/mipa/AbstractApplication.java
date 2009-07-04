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
package net.sourceforge.mipa;

import static config.Debug.DEBUG;
import java.io.File;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.predicatedetection.PredicateParserMethod;

import org.w3c.dom.Document;

/**
 * provides application abstract method.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public abstract class AbstractApplication implements ResultCallback {

    /** application name binds in Naming server */
    private String applicationName;

    /** predicate represented by Document. */
    private Document predicate;

    /**
     * reads predicate from an xml file.
     * 
     * @param xmlName
     *            predicate file name
     */
    public AbstractApplication(String xmlName) {
        this.predicate = parseXml(xmlName);
    }

    /**
     * reads predicate from <code>Document</code>.
     * 
     * @param xml
     *            predicate <code>Document</code>
     */
    public AbstractApplication(Document xml) {
        this.predicate = xml;
    }

    public abstract void callback(int value) throws RemoteException;

    /**
     * starts application.
     * 
     * @param configFileName
     *            config file name
     */
    public void start(String configFileName) {
        parseConfig(configFileName);
        
        String namingAddress = MIPAResource.getNamingAddress();
        
        if(DEBUG) {
            System.out.println("naming address is " + namingAddress);
        }
        
        try {
            Naming server = (Naming) java.rmi.Naming.lookup(namingAddress
                                                            + "Naming");
            
            if(DEBUG) {
                System.out.println("application lookup Naming successfully.");
            }

            // TODO generates application name
            applicationName = "app_1";
            
            ResultCallback stub = (ResultCallback) UnicastRemoteObject.exportObject(this, 0);
            server.bind(applicationName, stub);
            
            if(DEBUG) {
                System.out.println("application binds successfully.");
            }

            // get predicate parser and transfer xml document to it.

            PredicateParserMethod parser = (PredicateParserMethod) server
                                                                         .lookup("PredicateParser");
            if(DEBUG) {
                System.out.println("application lookups predicate parser successfully.");
            }
            
            parser.parsePredicate(applicationName, predicate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * parse config file.
     * 
     * @param fileName
     *            config file name
     */
    private void parseConfig(String fileName) {

        try {
            File f = new File(fileName);

            DocumentBuilderFactory factory = DocumentBuilderFactory
                                                                   .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);

            String address = doc.getElementsByTagName("address").item(0)
                                .getFirstChild().getNodeValue();

            String port = doc.getElementsByTagName("port").item(0)
                             .getFirstChild().getNodeValue();

            MIPAResource
                        .setNamingAddress("rmi://" + address + ":" + port + "/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * parse predicate to document.
     * 
     * @param fileName
     *            a string
     * @return predicate document
     */
    private Document parseXml(String fileName) {
        Document doc = null;
        try {
            File f = new File(fileName);

            DocumentBuilderFactory factory = DocumentBuilderFactory
                                                                   .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(f);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }
}
