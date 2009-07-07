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
import net.sourceforge.mipa.components.ContextMapping;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.eca.ECAManager;
import net.sourceforge.mipa.naming.Naming;

import org.w3c.dom.Document;

/**
 * local predicate parser module.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class LocalPredicateParser {

    /** context mapping */
    private ContextMapping contextMapping;

    public LocalPredicateParser(ContextMapping contextMapping) {
        this.contextMapping = contextMapping;
    }

    /**
     * parse local predicate from <code>Document</code>.
     * 
     * @param predicate
     *            a document
     * @param coordinateID
     *            a String, usually is applicationName
     * @param type
     *            predicate type
     */
    public void parseLocalPredicate(Document predicate, String coordinateID,
                                    PredicateType type) {
        // TODO parse local predicate
        if (DEBUG) {
            System.out.println("\tparsing local preidcate...");

            LocalPredicate localPredicate = new LocalPredicate();
            localPredicate.setName("temperature");
            localPredicate.setOperator("great-than");
            localPredicate.setValue("1");
            localPredicate.setValueType("Float");
            try {
                String ECAManagerId = contextMapping.getMapping("temperature");
                // FIXME put lookup to a method
                Naming server = (Naming) java.rmi.Naming
                                                        .lookup(MIPAResource
                                                                            .getNamingAddress()
                                                                + "Naming");
                ECAManager ecaManager = (ECAManager) server.lookup(ECAManagerId);
                System.out.println("find eca manager successfully.");
                System.out.println(ECAManagerId);
                ecaManager.registerLocalPredicate(localPredicate, coordinateID, type);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*
         * NodeList elements = predicate.getElementsByTagName("LP"); for(int i =
         * 0; i < elements.getLength(); i++) {
         * 
         * }
         */
    }
}