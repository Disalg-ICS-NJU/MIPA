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
package net.sourceforge.mipa.predicatedetection.oga;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.components.MessageType;
import net.sourceforge.mipa.predicatedetection.AbstractNormalProcess;
import net.sourceforge.mipa.predicatedetection.VectorClock;


/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class OGANormalProcess extends AbstractNormalProcess {

    private static final long serialVersionUID = -663144941748622894L;
    
    private boolean currentState;
    
    private boolean flagMsgAct;
    
    private OGAVectorClock lo;
    
    private String[] groupNormalProcesses;
    
    private Map<String, Long> currentMessageCount;
    /**
     * construction.
     * 
     * @param name
     * @param checkers
     * @param normalProcesses
     * @param subNormalProcesses Global activity group.
     */
    public OGANormalProcess(String name, String[] checkers, String[] normalProcesses, String[] subNormalProcesses) {
        super(name, checkers, normalProcesses);
        
        currentClock = new OGAVectorClock(normalProcesses.length);
        currentClock.increment(id);
        
        currentMessageCount = new HashMap<String, Long>();
        for(int i = 0; i < checkers.length; i++) {
            currentMessageCount.put(checkers[i], new Long(0));
        }
        
        currentState = false;
        flagMsgAct = true;
        
        groupNormalProcesses = subNormalProcesses;
        
    }
    @Override
    public void action(boolean value) {
        boolean changed = false;
        if(currentState != value) changed = true;
        
        if(changed == true && currentState == true) {
            // interval begins. Sending control message to GA group.
            groupBroadcast(MessageType.Control, null);
            
            if(flagMsgAct) {
                lo = new OGAVectorClock(currentClock);
            }
        } else if(changed == true && currentState == false) {
            // interval ends. Sending control message to all processes.
            broadcast(MessageType.Control, null);
            if(flagMsgAct) {
                OGAVectorClock hi = new OGAVectorClock(currentClock);
                
                OGAMessageContent content = new OGAMessageContent(lo, hi);
                
                for(int i = 0; i < checkers.length; i++) {
                    String checker = checkers[i];
                    send(MessageType.Detection, checker, content);
                }
                flagMsgAct = false;
            }
        }
        currentState = value;
    }

    @Override
    public void receiveMsg(Message message) {
        VectorClock timestamp = message.getTimestamp();
        currentClock.update(timestamp);
        flagMsgAct = true;
    }

    @Override
    public void application() {
        // TODO Auto-generated method stub
        
    }
    
    private void send(MessageType type, String receiverName, OGAMessageContent content) {
        Message m = new Message();
        m.setType(type);
        m.setSenderID(name);
        m.setReceiverID(receiverName);
        VectorClock current = new OGAVectorClock(currentClock);
        m.setTimestamp(current);
        m.setOgaMessageContent(content);
        
        if(currentMessageCount.containsKey(receiverName) == true) {
            long currentCount = currentMessageCount.get(receiverName);
            m.setMessageID(currentCount);
            currentMessageCount.put(receiverName, new Long(currentCount + 1));
        } else {
            assert(false);
        }
        
        try {
            messageDispatcher.send(m);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        currentClock.increment(id);
    }
    
    private void groupBroadcast(MessageType type, OGAMessageContent content) {
        for(int i = 0; i < groupNormalProcesses.length; i++) {
            if(! name.equals(groupNormalProcesses[i])) {
                send(type, groupNormalProcesses[i], content);
            }
        }
    }
    
    private void broadcast(MessageType type, OGAMessageContent content) {
        for(int i = 0; i < normalProcesses.length; i++) {
            if(! name.equals(normalProcesses[i])) {
                send(type, normalProcesses[i], content);
            }
        }
    }
}
