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

import java.rmi.server.UnicastRemoteObject;

import net.sourceforge.mipa.ResultCallback;
import net.sourceforge.mipa.components.Communication;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.predicatedetection.scp.SCPChecker;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class CheckerFactory {

    private static Naming server;
    static {
        try {
            server = (Naming) java.rmi.Naming
                                             .lookup(MIPAResource
                                                                 .getNamingAddress()
                                                     + "Naming");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void newChecker(String callback, String checkerName,
                                  String[] normalProcesses,
                                  PredicateType type) {
        try {
            ResultCallback application = (ResultCallback) server
                                                                .lookup(callback);

            switch (type) {
            case SCP:
                SCPChecker checker = new SCPChecker(application, checkerName,
                                                    normalProcesses);
                Communication checkerStub = (Communication) UnicastRemoteObject
                                                                               .exportObject(
                                                                                             checker,
                                                                                             0);
                server.bind(checkerName, checkerStub);
                break;
            case LP:

            case WCP:

            default:
                System.out.println("Type has not been defined.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
