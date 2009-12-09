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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.predicatedetection.AbstractNonFIFOChecker;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class OGATopChecker extends AbstractNonFIFOChecker {

    private static final long serialVersionUID = -5005195916176717460L;

    private int m;

    private Map<String, ArrayList<OGAVectorClock>> preQueHiMap;

    public OGATopChecker(ResultCallback application, String checkerName,
                         String[] children) {
        super(application, checkerName, children);

        m = children.length;

        preQueHiMap = new HashMap<String, ArrayList<OGAVectorClock>>();
        for(int i = 0; i < children.length; i++) {
            preQueHiMap.put(children[i], null);
        }
    }
    /*
    @Override
    public synchronized void receive(Message message) throws RemoteException {
        
        String senderName = message.getSenderID();
        Integer messageSenderIndex = indexMap.get(senderName);
        
        ArrayList<Message> queue = messageQueues.get(messageSenderIndex);
        queue.add(message);
        
        queue = messageQueues.get(new Integer(index));
        
        while(queue.size() != 0) {
            detect(queue.remove(0));
            // index will change in detect().
            queue = messageQueues.get(new Integer(index));
        }
    }
    */
    
    protected void handle(ArrayList<Message> messages) {
        while(messages.size() != 0) {
            detect(messages.remove(0));
            // index will change in detect().
            messages = messageQueues.get(new Integer(index));
        }
    }
    private void detect(Message message) {
        OGAMessageContent content = (OGAMessageContent) message.getMessageContent();
        String senderName = message.getSenderID();
        ArrayList<OGAVectorClock> curQueLo = content.getSetLo();
        ArrayList<OGAVectorClock> curQueHi = content.getSetHi();
        boolean find = true;

        int messageSenderIndex = indexMap.get(senderName).intValue();
        ArrayList<OGAVectorClock> preQueHi = preQueHiMap.get(senderName);
        
        
        if (preQueHi == null) {
            preQueHiMap.put(senderName, curQueHi);
            return;
        }
        
        assert(messageSenderIndex == index);
        
        outer: 
            for (int i = 0; i < preQueHi.size(); i++) {
                for (int j = 0; j < curQueLo.size(); j++) {
                    int value = OGAVectorClock.compare(preQueHi.get(i),
                                                           curQueLo.get(j));
                    if (!(value == -1 || value == 0)) {
                        find = false;
                        break outer;
                    }
                }
            }
        
        if (find) index++;
        if (index > m) {
            try {
                application.callback(String.valueOf(true));
                index = 1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        preQueHiMap.put(senderName, curQueHi);
    }
}
