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
package net.sourceforge.mipa.application;

import static config.Debug.DEBUG;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.mipa.components.BrokerInterface;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.Naming;

import org.w3c.dom.Document;

public abstract class AbstractApplication {

    private HashMap<String, String> predicateNameToID;
    
    public AbstractApplication() {
    	predicateNameToID = new HashMap<String, String>();
    }
    
    public String register(String predicateFile, ResultCallback callback, String predicateName) {
    	Document predicate = parseXml(predicateFile);
    	String predicateID = null;
        try {
            BrokerInterface broker = MIPAResource.getBroker();
            predicateID = broker.registerPredicate(callback, predicate);
            if(predicateID == null) {
            	System.out.println("Prediate: "+predicateName+" fails to register.");
            	return null;
            }
            predicateNameToID.put(predicateName, predicateID);
            System.out.println("Prediate: "+predicateName+" registers successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return predicateID;
    }

    public void unregister(String predicateName) {
    	String predicateID = predicateNameToID.get(predicateName);
    	BrokerInterface broker = MIPAResource.getBroker();
    	try {
    		broker.unregisterPredicate(predicateID);
    		predicateNameToID.remove(predicateName);
    		System.out.println("Prediate: "+predicateName+" unregisters successfully.");
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    private Document parseXml(String fileName) {
        Document doc = null;
        try {
            File f = new File(fileName);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }
}
