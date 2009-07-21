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

    protected String[] normalProcesses;

    protected String[] checkers;

    /** the vector clock of normal process */
    protected VectorClock currentClock;

    protected boolean finished;

    protected MessageDispatcher messageDispatcher;

    public AbstractNormalProcess(String name, String[] checkers, String[] normalProcesses) {
	this.checkers = checkers;
	this.normalProcesses = normalProcesses;
        this.name = name;
        finished = false;
        
        for (int i = 0; i < normalProcesses.length; i++) {
            if (normalProcesses[i].equals(name)) {
                this.id = i;
                break;
            }
        }

        try {
            messageDispatcher = MIPAResource.getMessageDispatcher();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public abstract void broadcast(MessageType type, MessageContent content);

    
    public void receive(Message message) throws RemoteException {
        //if(finished) {
        receiveMsg(message);
        //}
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
    public void finished() {
	finished = true;
	Thread t = new Thread(this);
	t.start();
    }
}