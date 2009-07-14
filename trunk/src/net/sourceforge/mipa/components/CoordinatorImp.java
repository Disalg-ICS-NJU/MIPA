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

import static config.Debug.DEBUG;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.predicatedetection.CheckerFactory;
import net.sourceforge.mipa.predicatedetection.NormalProcess;
import net.sourceforge.mipa.predicatedetection.PredicateType;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class CoordinatorImp implements Coordinator {

    private Map<String, Group> groupMap;

    private Naming server;
    private IDManager idManager;

    public CoordinatorImp() {
        groupMap = new HashMap<String, Group>();

        try {
            server = MIPAResource.getNamingServer();
            idManager = MIPAResource.getIDManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void normalProcessFinished(String groupID,
                                                   String normalProcessID)
                                                                          throws RemoteException {
        assert (groupMap.containsKey(groupID));
        
        Group g = groupMap.get(groupID);
        int finishedNum = g.getNumberOfFinishedNormalProcesses();
        int total = g.getNumberOfNormalProcesses();
        
        assert(finishedNum < total);
        
        finishedNum++;
        g.setNumberOfFinishedNormalProcesses(finishedNum);
        
        if(DEBUG) {
            System.out.println("Coordinator receive normal process name: " + normalProcessID);
        }
        g.addNormalProcess(normalProcessID);
        
        if(finishedNum == total) {
            // create checker
            PredicateType type = g.getPredicateType();
            String[] normalProcesses = g.getNormalProcesses();
            String checker = g.getCheckerName();
            
            if(DEBUG) {
                System.out.println("new checker in coordinator...");
                System.out.println("type is " + type);
            }
            CheckerFactory.newChecker(groupID, checker, normalProcesses, type);
            for(int i = 0; i < normalProcesses.length; i++) {
                try {
                    NormalProcess np = (NormalProcess) server.lookup(normalProcesses[i]);
                    np.retrieveInformation(normalProcesses, checker);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                
            }
        }
    }

    @Override
    public synchronized void newCoordinator(String groupID,
                                            int numberOfNormalProcesses,
                                            PredicateType type)
                                                               throws RemoteException {
        assert (groupMap.containsKey(groupID) == false);

        Group g = new Group(groupID, idManager.getID(Catalog.Checker),
                            numberOfNormalProcesses, type);
        groupMap.put(groupID, g);
    }
}
