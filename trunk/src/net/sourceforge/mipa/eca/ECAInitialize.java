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

import java.io.File;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.mipa.components.ContextRegister;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.Naming;

import org.w3c.dom.Document;

/**
 * initialize ECA mechanism.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class ECAInitialize {
    /**
     * initialize method.
     */
    public void initialize() {
        parseConfig("config.xml");

        String namingAddress = MIPAResource.getNamingAddress();
        try {
            Naming server = (Naming) java.rmi.Naming.lookup(namingAddress
                                                            + "Naming");

            IDManager idManager = (IDManager) server.lookup("IDManager");
            ContextRegister contextRegister = (ContextRegister) server
                                                                      .lookup("ContextRegister");

            // binding data source
            String dataSourceId = idManager.getID(Catalog.DataSource);
            DataSourceImp dataSource = new DataSourceImp();
            DataSource dataSourceStub = (DataSource) UnicastRemoteObject
                                                                        .exportObject(
                                                                                      dataSource,
                                                                                      0);
            server.bind(dataSourceId, dataSourceStub);

            String ecaManagerId = idManager.getID(Catalog.ECAManager);
            ECAManagerImp ecaManager = new ECAManagerImp(contextRegister,
                                                         dataSourceStub,
                                                         ecaManagerId);
            // ecaManager.setECAManagerName(ecaManagerId);

            ECAManager ecaManagerStub = (ECAManager) UnicastRemoteObject
                                                                        .exportObject(
                                                                                      ecaManager,
                                                                                      0);
            server.bind(ecaManagerId, ecaManagerStub);

            // start sensor agent in threads.
            // TODO sensor name should read from config file.
            String eventName = "temperature";
            SensorAgent temperature = new TemperatureAgent(dataSourceStub,
                                                           eventName);
            Thread t = new Thread(temperature);
            t.start();
            // temperature.start();
            if (DEBUG) {
                System.out.println("sensor running...");
            }

            // add resources to list for registering resources.
            ArrayList<String> list = new ArrayList<String>();
            list.add(eventName);
            String[] resources = new String[list.size()];
            list.toArray(resources);

            if (DEBUG) {
                System.out.println("resources value: ");
                for (int i = 0; i < resources.length; i++) {
                    System.out.println(resources[i]);
                }
                System.out.println();
            }

            ecaManager.registerResources(resources);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
}
