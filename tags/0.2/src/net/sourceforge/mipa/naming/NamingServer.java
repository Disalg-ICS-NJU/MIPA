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

/**
 * This class <code>NamingServer</code> provides Naming resolving.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class NamingServer {

    /** registry port */
    private int port;

    /** registry address */
    private String address;

    /**
     * <code>NamingServer</code> construction.
     * 
     * @param port
     */
    public NamingServer(String address, int port) {
        this.address = address;
        this.port = port;
    }

    /**
     * starts naming server.
     */
    public void startServer() {
        try {
            // TODO add security policy

            LocateRegistry.createRegistry(port);

            NamingImp server = new NamingImp();

            server.setRegistryAddress(address);
            server.setPort(port);
            server.formatAddress();

            Naming stub = (Naming) UnicastRemoteObject.exportObject(server, 0);

            java.rmi.Naming.rebind(address + ":" + port + "/Naming", stub);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        String address = "rmi://127.0.0.1";
        int port = 1099;

        NamingServer server = new NamingServer(address, port);
        System.out.println("Naming server is running...");
        server.startServer();
    }
}
