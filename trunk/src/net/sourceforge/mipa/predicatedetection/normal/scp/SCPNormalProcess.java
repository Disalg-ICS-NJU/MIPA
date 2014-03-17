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
package net.sourceforge.mipa.predicatedetection.normal.scp;

import static config.Config.ENABLE_PHYSICAL_CLOCK;
import static config.Config.LOG_DIRECTORY;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.components.MessageType;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.predicatedetection.AbstractNormalProcess;
import net.sourceforge.mipa.predicatedetection.VectorClock;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class SCPNormalProcess extends AbstractNormalProcess {

    private static final long serialVersionUID = -2040352661249255553L;

    private boolean prevState;
    
    private boolean firstflag;
    
    private SCPVectorClock lo;
    
    private long pTimeLo;
    
    private PrintWriter out;
    
    private Map<String, Long> currentMessageCount;
    
    private ArrayList<Message> msgBuf;
    /**
     * @param name
     */
    public SCPNormalProcess(String name, String[] checkers, String[] normalProcesses) {
        super(name, checkers, normalProcesses);
        
        currentClock = new SCPVectorClock(normalProcesses.length);
        currentClock.increment(id);
        
        // TODO Auto-generated constructor stub
        currentMessageCount = new HashMap<String, Long>();
        for(int i = 0; i < checkers.length; i++) {
            currentMessageCount.put(checkers[i], new Long(0));
        }
        
        prevState = false;
        firstflag = true;
        
        if(ENABLE_PHYSICAL_CLOCK) {
            try {
                out = new PrintWriter(LOG_DIRECTORY + "/" + name + ".log");
                msgBuf = new ArrayList<Message>();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
    }

    @Override
    public void receiveMsg(Message message) {
    	
    	if(ENABLE_PHYSICAL_CLOCK) {
    		message.setPhysicalReceiveTime((new Date()).getTime());
    		msgBuf.add(message);
        }
    	
        VectorClock timestamp = message.getTimestamp();
        currentClock.update(timestamp);
        firstflag = true;
    }

    @Override
    public void action(String value) {
        // TODO Auto-generated method stub
    	String[] values = value.split("\\s+");
    	boolean newValue = Boolean.parseBoolean(values[0]);
    	if(prevState != newValue) {
            if(prevState == false) {
            	broadcast(MessageType.Control, null);
            	lo = new SCPVectorClock(currentClock);
            	if(ENABLE_PHYSICAL_CLOCK) {
                    pTimeLo = (new Date()).getTime();
                }
            	currentClock.increment(id);
            }
            else if(prevState == true && firstflag == true) {
            	SCPVectorClock hi = new SCPVectorClock(currentClock);
            	SCPMessageContent content = new SCPMessageContent(lo, hi);
            	if(ENABLE_PHYSICAL_CLOCK) {
                    IDManager idManager = MIPAResource.getIDManager();
                    try {
                        String intervalID = idManager.getID(Catalog.Numerical);

                        content.setIntervalID(intervalID);
                        long pTimeHi = (new Date()).getTime();
                        content.setpTimeLo(pTimeLo);
                        content.setpTimeHi(pTimeHi);
                        out.println(intervalID + " " + pTimeLo + " " + pTimeHi);
                        out.flush();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            	//??? SCP doesn't send any message to other normal processes.
                for(int i = 0; i < checkers.length; i++) {
                    String checker = checkers[i];
                    send(MessageType.Detection, checker, content);
                }
                firstflag = false;
            }
            System.out.println(name+": The value of the local predicate is changed.");
    	}
    	else{
    		System.out.println(name+": The event is ignored because it does not change the value of the local predicate.");
    	}
        prevState = newValue;
    }
    
    public void send(MessageType type, String receiverName, SCPMessageContent content) {
        Message m = new Message();
        m.setType(type);
        m.setSenderID(name);
        m.setReceiverID(receiverName);
        VectorClock current = new SCPVectorClock(currentClock);
        m.setTimestamp(current);
        m.setMessageContent(content);
        
        if(ENABLE_PHYSICAL_CLOCK) {
        	m.setPhysicalSendTime((new Date()).getTime());
        }
        
        if(currentMessageCount.containsKey(receiverName) == true) {
            long currentCount = currentMessageCount.get(receiverName);
            m.setMessageID(currentCount);
            currentMessageCount.put(receiverName, new Long(currentCount + 1));
        } else {
            assert(false);
        }        
        sender.send(m);
    }
    
    private void broadcast(MessageType type, SCPMessageContent content) {
        for(int i = 0; i < normalProcesses.length; i++) {
            if(i != id) {
                send(type, normalProcesses[i], content);
            }
        }
    }
    
    @Override
    public void application() {
        // TODO Auto-generated method stub
        
    }

	public ArrayList<Message> getMsgBuf() {
		return msgBuf;
	}

	public void setMsgBuf(ArrayList<Message> msgBuf) {
		this.msgBuf = msgBuf;
	}

}
