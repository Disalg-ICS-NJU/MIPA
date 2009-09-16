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
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class ClassAB implements InterfaceA, InterfaceB {

    /* (non-Javadoc)
     * @see net.sourceforge.mipa.test.InterfaceA#A()
     */
    @Override
    public void A(int i) throws RemoteException {
        // TODO Auto-generated method stub
        System.out.println("A");
        i++;

    }

    /* (non-Javadoc)
     * @see net.sourceforge.mipa.test.InterfaceB#B()
     */
    @Override
    public void B() throws RemoteException {
        // TODO Auto-generated method stub
        System.out.println("B");
    }

    public static void main(String[] args) {
        try {
            Naming server = (Naming) java.rmi.Naming
                                    .lookup("rmi://127.0.0.1:1099/Naming");
           
            System.out.println("look up successfully.");
            
            ClassAB ab = new ClassAB();
            InterfaceA stub = (InterfaceA) UnicastRemoteObject.exportObject(ab, 0);

            server.bind("ab", stub);
            
           // InterfaceB a = (InterfaceB) server.lookup("ab");
            //a.B();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
