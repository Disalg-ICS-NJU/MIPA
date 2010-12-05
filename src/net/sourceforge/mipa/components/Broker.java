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
package net.sourceforge.mipa.components;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.w3c.dom.Document;

import net.sourceforge.mipa.components.rm.ResourceManager;
import net.sourceforge.mipa.eca.ECAManager;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.predicatedetection.Atom;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class Broker implements BrokerInterface {
    
    //private ContextModeling contextModeling;
    
    //private ContextRetrieving contextRetrieving;
    
    private ResourceManager resourceManager;
    
    
    public Broker(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
    
    /*
    public Broker(ContextModeling modeling, ContextRetrieving retrieving) {
        contextModeling = modeling;
        contextRetrieving = retrieving;
    }
    */
    
    public String registerPredicate(String applicationName, Document predicate) 
    												throws RemoteException {
    	return null;
    }
    
    public void unregisterPredicate(String predicateID) throws RemoteException {
    	
    }
    
    public synchronized void registerResource(String resourceName,
                                              String valueType, 
                                              String entityId)
                                                  throws RemoteException {
        
        resourceManager.registerResource(resourceName, valueType, entityId);
    }
    
    
    //FIXME it only supports that a high level context is mapping to only one low level context.
    //TODO it should support that a high level context is mapping to several low level contexts.
    public void registerLocalPredicate(LocalPredicate lp, String normalProcessId, Group g) {
        try {
            ArrayList<Atom> arrayList = lp.getAtoms();
            String ecaManagerId = resourceManager.findResource(arrayList.get(0).getName())[0];
            
            for(int i = 0; i < arrayList.size(); i++) {
                Atom atom = arrayList.get(i);
                String newId = resourceManager.findResource(atom.getName())[0];
                if(! newId.equals(ecaManagerId)) {
                    System.out.println("The sensors " + arrayList.get(0).getName()
                                                       + " and "
                                                       + arrayList.get(i).getName()
                                                       + " are in different ECA.");
                    
                }
                
                //FIXME atom should be recreated, and doesn't reuse the atom in the arrayList. 
                // It is not a bug, but it isn't elegant.
                String atomicContext = resourceManager.getAtomicContextNames(atom.getName())[0];
                atom.setName(atomicContext);
                atom.setValueType(resourceManager.getTypeOfAtomicContext(atomicContext));
            }
            
            Naming server = MIPAResource.getNamingServer();
            ECAManager ecaManager = (ECAManager) server.lookup(ecaManagerId);
            
            System.out.println("find eca manager successfully.");
            System.out.println(ecaManagerId);
            ecaManager.registerLocalPredicate(lp, normalProcessId, g);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
    public void registerLocalPredicate(LocalPredicate lp, String normalProcessId, Group g) {
        try {
                ArrayList<Atom> arrayList = lp.getAtoms();
                String ecaManagerID = contextRetrieving.getEntityId(
                                           contextModeling.
                                               getLowContext(arrayList.get(0).getName()));
                
                for(int i=0;i<arrayList.size();i++)
                {
                    Atom atom = arrayList.get(i);
                    String lowContext = contextModeling.getLowContext(atom.getName());
                    String ecaManagerIDNew = contextRetrieving.getEntityId(lowContext);
                    if(!ecaManagerIDNew.equals(ecaManagerID))
                    {
                        System.out.println("The sensors "+arrayList.get(0).getName()
                                                         +" and "
                                                         +arrayList.get(i).getName()
                                                         +" are in different ECA.");
                    }
                    // reset the name of local predicate from high level context to low level context.
                    atom.setName(lowContext);
                    atom.setValueType(contextModeling.getValueType(lowContext));
                }

                Naming server = MIPAResource.getNamingServer();
                ECAManager ecaManager = (ECAManager) server.lookup(ecaManagerID);

                System.out.println("find eca manager successfully.");
                System.out.println(ecaManagerID);
                ecaManager.registerLocalPredicate(lp, normalProcessId, g);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
