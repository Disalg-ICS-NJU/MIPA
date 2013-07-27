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

package net.sourceforge.mipa.naming;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.naming.NamingImp;

/**
 * This class <code>NamingService</code> provides Naming resolving.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class NamingService {

    /** registry port */
    private int port;

    /** registry address */
    private String address;
    
    private static Logger logger = Logger.getLogger(NamingService.class);

    /**
     * <code>NamingServer</code> construction.
     * 
     */
    public NamingService() {
        address = MIPAResource.getAddress();
        port = MIPAResource.getPort();
    }

    /**
     * starts naming server.
     */
    public void startService() {
        try {
            // TODO add security policy

            LocateRegistry.createRegistry(port);

            NamingImp service = new NamingImp();

            service.setRegistryAddress(address);
            service.setPort(port);
            service.formatAddress();

            Naming stub = (Naming) UnicastRemoteObject.exportObject(service, 0);

            java.rmi.Naming.rebind("rmi://" + address + ":" + port + "/Naming", stub);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * starts naming server.
     */
    public void stopService() {
        try {
            java.rmi.Naming.unbind("rmi://" + address + ":" + port + "/Naming");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        NamingService service = new NamingService();
        //System.out.println("Naming server is running...");
        System.out.println("========== MIPA system Naming Service ==========");
        System.out.println("Naming server information:");
        System.out.println("*\tIP address: " + service.address);
        System.out.println("*\tport: " + service.port);
        logger.info("========== MIPA system Naming Service ==========");
        logger.info("Naming server information:");
        logger.info("*IP address: " + service.address);
        logger.info("*Port: " + service.port);
        service.startService();
    }
}
