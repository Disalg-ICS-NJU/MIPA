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
    
    private String[] groupNormalProcesses;
    

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
            
            
            if(flagMsgAct) {
                
            }
        } else if(changed == true && currentState == false) {
            // interval ends. Sending control message to all processes.
            
            if(flagMsgAct) {
                
            }
        }
        
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
        
    }
    
    private void groupBroadcast(MessageType type, OGAMessageContent content) {
        for(int i = 0; i < groupNormalProcesses.length; i++) {
            if(! name.equals(groupNormalProcesses[i])) {
                send(type, groupNormalProcesses[i], content);
            }
        }
    }
    
    private void broadcoast(MessageType type, OGAMessageContent content) {
        for(int i = 0; i < normalProcesses.length; i++) {
            if(! name.equals(normalProcesses[i])) {
                send(type, normalProcesses[i], content);
            }
        }
    }
}
