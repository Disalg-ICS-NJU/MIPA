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
import java.rmi.server.UnicastRemoteObject;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.IDManagerImp;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.predicatedetection.PredicateParser;
import net.sourceforge.mipa.predicatedetection.PredicateParserMethod;

import org.w3c.dom.Document;

/**
 * MIPA platform initialization.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class Initialize {

    /**
     * initialize MIPA Infrastructure.
     * 
     * @return true if successful, false otherwise
     */
    public boolean initialize() {

        parseConfig("config.xml");

        try {
            // binds predicate parser
            Naming server = (Naming) java.rmi.Naming
                                                    .lookup(MIPAResource
                                                                        .getNamingAddress()
                                                            + "Naming");

            IDManagerImp idManager = new IDManagerImp();
            IDManager managerStub = (IDManager) UnicastRemoteObject
                                                                   .exportObject(
                                                                                 idManager,
                                                                                 0);
            server.bind("IDManager", managerStub);

            PredicateParser predicateParser = new PredicateParser();
            PredicateParserMethod predicateParserStub = (PredicateParserMethod) UnicastRemoteObject
                                                                                                   .exportObject(
                                                                                                                 predicateParser,
                                                                                                                 0);

            server.bind("PredicateParser", predicateParserStub);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void parseConfig(String fileName) {
        // TODO parses config file and sets MIPAResource
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

    public static void main(String[] args) {
        boolean result = new Initialize().initialize();
        if (result == true) {
            System.out.println("Initialization finished.");
        } else {
            System.out.println("Error occurs when initializing");
        }

        if (DEBUG) {
            System.out.println(MIPAResource.getNamingAddress());
        }
    }
}
