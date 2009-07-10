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
package net.sourceforge.mipa.predicatedetection.scp;

import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.components.MessageContent;
import net.sourceforge.mipa.components.MessageType;
import net.sourceforge.mipa.predicatedetection.AbstractNormalProcess;
import net.sourceforge.mipa.predicatedetection.VectorClock;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class SCPNormalProcess extends AbstractNormalProcess {

    private static final long serialVersionUID = -2040352661249255553L;

    private boolean currentState;
    
    private boolean firstflag;
    
    private SCPVectorClock lo;
    
    
    /**
     * @param name
     */
    public SCPNormalProcess(String name) {
        super(name);
        // TODO Auto-generated constructor stub
        currentState = false;
        firstflag = true;
    }

    @Override
    public void receiveMsg(Message message) {
        VectorClock timestamp = message.getTimestamp();
        currentClock.update(timestamp);
        firstflag = true;
    }

    @Override
    public void action(boolean value) {
        // TODO Auto-generated method stub
        if(currentState != value && firstflag) {
            if(currentState == false) {
                //interval begins
                lo = new SCPVectorClock(currentClock);
                broadcast(MessageType.Control, null);
            } else {
                //interval ends
                SCPVectorClock hi = new SCPVectorClock(currentClock);
                MessageContent content = new SCPMessageContent(lo, hi);
                //SCP doesn't send any message to other normal processes.
                send(MessageType.Detection, checker, content);
            }
            currentState = value;
        }
    }
    
    private void send(MessageType type, String receiverName, MessageContent content) {
        Message m = new Message();
        m.setType(type);
        m.setSenderID(name);
        m.setReceiverID(receiverName);
        VectorClock current = new SCPVectorClock(currentClock);
        m.setTimestamp(current);
        m.setContent(content);
        
        try {
            messageDispatcher.send(m);
        } catch(Exception e) {
            e.printStackTrace();
        }
        currentClock.increment(id);
    }
    
    private void broadcast(MessageType type, MessageContent content) {
        for(int i = 0; i < normalProcessesList.length; i++) {
            if(i != id) {
                send(type, normalProcessesList[i], content);
            }
        }
    }
    
    @Override
    public void application() {
        // TODO Auto-generated method stub
        
    }

}
