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

package net.sourceforge.mipa.test;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.sourceforge.mipa.naming.Naming;

/**
 * Naming server test example.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class NamingTest implements NamingTestInterface {

    public String str = "A test from Naming tree.";

    /*
     * public NamingTest() throws RemoteException { super(); }
     */

    public String getStr() throws RemoteException {
        return str;
    }

    public static void main(String[] args) {
        try {
            Naming server = (Naming) java.rmi.Naming
                                                    .lookup("rmi://127.0.0.1:1099/Naming");
            System.out.println("look up successfully.");
            NamingTest test = new NamingTest();
            NamingTestInterface stub = (NamingTestInterface) UnicastRemoteObject
                                                                                .exportObject(
                                                                                              test,
                                                                                              0);

            server.bind("str", stub);
            System.out.println("Binding successful.");

            NamingTestInterface t1 = (NamingTestInterface) server.lookup("str");
            System.out.println(t1.getStr());

            server.unbind("str");
            System.out.println("Unbinding successful.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
