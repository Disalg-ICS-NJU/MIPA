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
import net.sourceforge.mipa.components.Coordinator;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.eca.ECAManager;
import net.sourceforge.mipa.naming.Naming;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        }
        Coordinator coordinator = MIPAResource.getCoordinator();

        NodeList elements = predicate.getElementsByTagName("LP");

        if (elements != null) {
            
            if(DEBUG) {
                System.out.println("number of LOCAL PREDICATE: " + elements.getLength());
            }
            try {
                coordinator.newCoordinator(coordinateID, elements.getLength(), type);
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            
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

                                        // if (name.equals("temperature")) {
                                        registerLocalPredicate(
                                                               operator,
                                                               name,
                                                               value,
                                                               coordinateID,
                                                               type);
                                        // } //:end if
                                    } // :end if
                                } // :end if
                            } // :end for
                        } // :end if
                    } // :end if
                } // :end for
            } // :end for
        } // :end if
    }

    private void registerLocalPredicate(String operator, String name,
                                        String value, String coordinateID,
                                        PredicateType type) {

        LocalPredicate localPredicate = new LocalPredicate();
        localPredicate.setOperator(operator);
        localPredicate.setName(name);
        localPredicate.setValue(value);

        try {
            String ecaManagerId = contextMapping
                                                .getEntityId(localPredicate
                                                                           .getName());
            localPredicate
                          .setValueType(contextMapping
                                                      .getValueType(localPredicate
                                                                                  .getName()));
            // FIXME put lookup to a method
            Naming server = MIPAResource.getNamingServer();
            ECAManager ecaManager = (ECAManager) server
                                                       .lookup(ecaManagerId);
            System.out.println("find eca manager successfully.");
            System.out.println(ecaManagerId);
            ecaManager.registerLocalPredicate(localPredicate, coordinateID,
                                              type);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}