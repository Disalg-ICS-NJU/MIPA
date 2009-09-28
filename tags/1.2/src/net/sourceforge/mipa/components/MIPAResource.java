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
package net.sourceforge.mipa.components;

import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.predicatedetection.PredicateParserMethod;

/**
 * <code>MIPAResource</code> provides basic resource that MIPA uses.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class MIPAResource {
    /** naming server address */
    private static String namingAddress = null;
    
    private static int port = 0;

    private static Naming server = null;

    private static IDManager idManager = null;

    private static ContextRegister contextRegister = null;
    
    private static Coordinator coordinator = null;
    
    private static PredicateParserMethod predicateParser = null;
    
    private static MessageDispatcher messageDispatcher = null;

    //TODO parse config will move into MIPAResource.
    
    /**
     * returns naming server address.
     * 
     * @return naming server address
     */
    public static String getNamingAddress() {
        return namingAddress;
    }

    /**
     * sets naming server address.
     * 
     * @param namingAddress
     *            naming server address
     */
    public static void setNamingAddress(String namingAddress) {
        MIPAResource.namingAddress = namingAddress;
    }
    

    public static Naming getNamingServer() {
        if (server == null) {
            try {
                server = (Naming) java.rmi.Naming
                                                 .lookup(MIPAResource
                                                                     .getNamingAddress()
                                                         + "Naming");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return server;
    }
    
    
    public static IDManager getIDManager() {
        if(idManager == null) {
            Naming server = getNamingServer();
            try {
                idManager = (IDManager) server.lookup("IDManager");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return idManager;
    }
    
    public static ContextRegister getContextRegister() {
        if(contextRegister == null) {
            Naming server = getNamingServer();
            try {
                contextRegister = (ContextRegister) server.lookup("ContextRegister");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return contextRegister;
    }
    
    public static Coordinator getCoordinator() {
        if(coordinator == null) {
            Naming server = getNamingServer();
            try {
                coordinator = (Coordinator) server.lookup("Coordinator");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return coordinator;
    }
    
    public static PredicateParserMethod getPredicateParser() {
        if(predicateParser == null) {
            Naming server = getNamingServer();
            try {
                predicateParser = (PredicateParserMethod) server.lookup("PredicateParser");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return predicateParser;
    }
    
    public static MessageDispatcher getMessageDispatcher() {
        if(messageDispatcher == null) {
            Naming server = getNamingServer();
            try {
                messageDispatcher = (MessageDispatcher) server.lookup("MessageDispatcher");
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return messageDispatcher;
    }
}
