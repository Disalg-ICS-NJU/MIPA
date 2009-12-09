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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.Communication;
import net.sourceforge.mipa.components.Message;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public abstract class AbstractFIFOChecker implements Serializable, Communication, Runnable{

    private static final long serialVersionUID = -5023931031473945453L;
    
    protected ResultCallback application;
    
    protected String name;
    
    protected String[] children;
    
    protected Map<String, Integer> nameToID;
    
    private long[] currentMessageCount;

    private ArrayList<ArrayList<Message>> msgBuffer;
    
    private ArrayList<Message> messages;
    
    private boolean flag = false;
    
    public AbstractFIFOChecker(ResultCallback application, 
                            String checkerName, 
                            String[] children) {
        this.application = application;
        this.name = checkerName;
        this.children = children;
        
        nameToID = new HashMap<String, Integer>();
        for(int i = 0; i < children.length; i++) {
            nameToID.put(children[i], new Integer(i));
        }
        
        messages = new ArrayList<Message> ();
        
        msgBuffer = new ArrayList<ArrayList<Message>>();
        currentMessageCount = new long[children.length];
        for (int i = 0; i < children.length; i++) {
            msgBuffer.add(new ArrayList<Message>());
            currentMessageCount[i] = 0;
        }
        
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void add(ArrayList<Message> messages, Message msg) {
        long msgID = msg.getMessageID();

        for (int i = 0; i < messages.size(); i++) {
            long tempID = messages.get(i).getMessageID();

            if (msgID < tempID) {
                messages.add(i, msg);
                return;
            }
        }
        messages.add(msg);
    }

    public boolean isContinuous(ArrayList<Message> messages, int id) {
        assert (messages.size() > 0);

        long pre = messages.get(0).getMessageID();
        for (int i = 1; i < messages.size(); i++) {
            if (messages.get(i).getMessageID() != ++pre) {
                currentMessageCount[id] = pre;
                return false;
            }
        }
        currentMessageCount[id] = pre + 1;
        return true;
    }
    
    public synchronized void receive(Message message)throws RemoteException
    {
        String normalProcess = message.getSenderID();
        int id = nameToID.get(normalProcess).intValue();
        
        long messageID = message.getMessageID();
        add(msgBuffer.get(id), message);
        
        if(messageID == currentMessageCount[id]) {
            // check the buffer if is continuous or not
            if(isContinuous(msgBuffer.get(id), id) == true) {
                messages.clear();
                ArrayList<Message> buffer = msgBuffer.get(id);
                int size = buffer.size();
                for(int i = 0; i < size; i++) {
                    messages.add(buffer.remove(0));
                }
                flag = true;
            }
        }
    }
    public void run() {
        while(true)
        {
            if(flag == false)
            {
                try {
                    synchronized(this){
                       this.wait();
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else
            {
                flag = false;
                handle(messages);
            }
        }
    }
    protected abstract void handle(ArrayList<Message> message);
}
