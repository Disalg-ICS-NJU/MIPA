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
package net.sourceforge.mipa.eca;

import static config.Debug.DEBUG;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import net.sourceforge.mipa.components.ContextRegister;
import net.sourceforge.mipa.components.Coordinator;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.NormalProcess;
import net.sourceforge.mipa.predicatedetection.PredicateType;
import net.sourceforge.mipa.predicatedetection.scp.SCPNormalProcess;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class ECAManagerImp implements ECAManager {

    /** name of ECAManager */
    private String ecaManagerName;

    private ContextRegister contextRegister;

    private DataSource dataSource;

    public ECAManagerImp(ContextRegister contextRegister,
                         DataSource dataSource, String ecaManagerName) {
        this.setContextRegister(contextRegister);
        this.ecaManagerName = ecaManagerName;
        this.dataSource = dataSource;
    }

    /**
     * @param ecaManagerName
     *            the ecaManagerName to set
     */
    public void setECAManagerName(String ecaManagerName) {
        this.ecaManagerName = ecaManagerName;
    }

    /**
     * @return the ecaManagerName
     */
    public String getECAManagerName() {
        return this.ecaManagerName;
    }

    /**
     * @param contextRegister
     *            the contextRegister to set
     */
    public void setContextRegister(ContextRegister contextRegister) {
        this.contextRegister = contextRegister;
    }

    /**
     * @return the contextRegister
     */
    public ContextRegister getContextRegister() {
        return contextRegister;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.mipa.eca.ECAManager#registerLocalPredicate(net.sourceforge
     * .mipa.predicatedetection.LocalPredicate, java.lang.String)
     */
    @Override
    public void registerLocalPredicate(LocalPredicate localPredicate,
                                       String groupId, PredicateType type)
                                                                          throws RemoteException {
        // new listener and condition
        // TODO listener should be Normal process and remove lookup to construction.
        
        try {
            Naming server = (Naming) java.rmi.Naming
                                                .lookup(MIPAResource
                                                                    .getNamingAddress()
                                                        + "Naming");
        
            IDManager idManger = (IDManager) server.lookup("IDManager");
            
            Coordinator coordinator = (Coordinator) server.lookup("Coordinator");
            
            String npName = idManger.getID(Catalog.NormalProcess);
        
            SCPNormalProcess np = new SCPNormalProcess(npName);
            NormalProcess npStub = (NormalProcess) UnicastRemoteObject.exportObject(np, 0);
            
            server.bind(npName, npStub);
            coordinator.normalProcessFinished(groupId, npName);
        
            Condition everything = new EmptyCondition(np, localPredicate);
            // attaching condition to data source.

            if (DEBUG) {
                System.out.println("local predicate name is "
                                   + localPredicate.getName());
            }

            // FIXME should attach local predicate related events, get from atoms.
            dataSource.attach(everything, localPredicate.getName());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * registers local resource to resource manager.
     * 
     * @param resources
     *            local resources
     */
    public void registerResources(ArrayList<SensorAgent> resources) {
        // TODO auto scan local resource

        try {
            for (int i = 0; i < resources.size(); i++) {
                SensorAgent resource = resources.get(i);
                contextRegister.registerResource(resource.getName(),
                                                 resource.getValueType(),
                                                 ecaManagerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
