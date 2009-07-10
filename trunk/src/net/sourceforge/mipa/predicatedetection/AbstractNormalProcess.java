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

import java.io.Serializable;
import java.rmi.RemoteException;

import net.sourceforge.mipa.components.Communication;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.components.MessageDispatcher;
import net.sourceforge.mipa.eca.Listener;
import net.sourceforge.mipa.naming.Naming;

/**
 * abstract normal process.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public abstract class AbstractNormalProcess implements Serializable, Runnable,
        NormalProcess, Communication, Listener {

    private static final long serialVersionUID = 7874458145173305245L;

    /** name of normal process */
    protected String name;

    /** id of normal process */
    protected int id;

    protected String[] normalProcessesList;

    protected String checker;

    /** the vector clock of normal process */
    protected VectorClock currentClock;

    protected boolean finished;

    protected MessageDispatcher messageDispatcher;

    public AbstractNormalProcess(String name) {
        this.name = name;
        finished = false;

        try {
            Naming server = (Naming) java.rmi.Naming
                                                    .lookup(MIPAResource
                                                                        .getNamingAddress()
                                                            + "Naming");

            messageDispatcher = (MessageDispatcher) server
                                                          .lookup("MessageDispatcher");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public abstract void broadcast(MessageType type, MessageContent content);

    
    public void receive(Message message) throws RemoteException {
        if(finished) {
            receiveMsg(message);
        }
    }
    
    public void update(String eventName, String value) {
        if (finished) {
            boolean newValue = Boolean.parseBoolean(value);
            action(newValue);
        }

        if (DEBUG && !finished) {
            System.out.println("Ingore event update in normal process");
        }
    }
    
    public void run() {
        application();
    }

    public abstract void action(boolean value);
    
    public abstract void receiveMsg(Message message);
    
    public abstract void application();

    @Override
    public void retrieveInformation(String[] normalProcessesList, String checker) {
        for (int i = 0; i < normalProcessesList.length; i++) {
            if (normalProcessesList[i].equals(name)) {
                id = i;
                break;
            }
        }
        this.normalProcessesList = normalProcessesList;
        this.checker = checker;
        // initial its vector clock to 1
        currentClock.increment(id);
        finished = true;
        
        // run application thread
        Thread t = new Thread(this);
        t.start();
    }
}