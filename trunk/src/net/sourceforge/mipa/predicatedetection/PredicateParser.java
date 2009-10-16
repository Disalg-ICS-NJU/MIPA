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
package net.sourceforge.mipa.predicatedetection;

import static config.Debug.DEBUG;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.mipa.components.ContextModeling;
import net.sourceforge.mipa.components.ContextRetrieving;
import net.sourceforge.mipa.components.Coordinator;
import net.sourceforge.mipa.components.Group;
import net.sourceforge.mipa.components.GroupManager;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.eca.ECAManager;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.Naming;

import org.w3c.dom.Document;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class PredicateParser implements PredicateParserMethod {

    /** structure of predicate parser reference */
    private StructureParser structureParser;

    /** checker logic parser reference */
    // private CheckerParser checkerParser;
    
    private GroupManager groupManager;


    /**
     * default construction.
     */
    public PredicateParser(GroupManager groupManager) {
        structureParser = new StructureParser();
        this.groupManager = groupManager;
        // checkerParser = new CheckerParser();
    }

    public synchronized void parsePredicate(String applicationName,
                                            Document predicate)
                                                               throws RemoteException {

        if (DEBUG) {
            System.out.println("parsing predicate...");
        }
        //only for debug
        groupManager.setCallback(applicationName);

        PredicateType type = PredicateIdentify.predicateIdentify(predicate);

        Structure predicateStructure = structureParser
                                                      .parseStructure(predicate);
        if (type == PredicateType.OGA)
            groupManager.parseOGAStructure(predicateStructure);
        else if (type == PredicateType.SCP)
            groupManager.parseSCPStructure(predicateStructure);
        else {
            System.out
                      .println("This predicate type have not been implemented yet.");
        }

        // checkerParser.parseChecker(predicate, applicationName, type);
    }


    /*
    private void registerLocalPredicate(LocalPredicate lp, String name, Group g) {
        try {
            String lowContext = contextModeling.getLowContext(lp.getName());
            String ecaManagerID = contextRetrieving.getEntityId(lowContext);
            lp.setValueType(contextModeling.getValueType(lowContext));

            Naming server = MIPAResource.getNamingServer();
            ECAManager ecaManager = (ECAManager) server.lookup(ecaManagerID);

            System.out.println("find eca manager successfully.");
            System.out.println(ecaManagerID);
            ecaManager.registerLocalPredicate(lp, name, g);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
