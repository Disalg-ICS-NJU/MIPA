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

import net.sourceforge.mipa.components.ContextRegister;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.PredicateType;
import net.sourceforge.mipa.test.DemoListener;

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
        // TODO listener should be Normal process
        Listener listener = new DemoListener();
        Condition everything = new EmptyCondition(listener, localPredicate);
        // attaching condition to data source.

        if(DEBUG) {
            System.out.println("local predicate name is " + localPredicate.getName());
        }
        dataSource.attach(everything, localPredicate.getName());

    }

    /**
     * registers local resource to resource manager.
     * 
     * @param resources
     *            local resources
     */
    public void registerResources(String[] resources) {
        // TODO auto scan local resource

        try {
            for (int i = 0; i < resources.length; i++) {
                contextRegister.registerResource(resources[i], ecaManagerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
