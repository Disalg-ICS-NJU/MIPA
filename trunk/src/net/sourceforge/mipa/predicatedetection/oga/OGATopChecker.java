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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.mipa.ResultCallback;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.predicatedetection.AbstractChecker;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class OGATopChecker extends AbstractChecker {

    private static final long serialVersionUID = -5005195916176717460L;

    private int index;

    private int m;

    private Map<String, Integer> indexMap;
    private Map<String, ArrayList<OGAVectorClock>> preQueHiMap;

    public OGATopChecker(ResultCallback application, String checkerName,
                         String[] children) {
        super(application, checkerName, children);

        index = 1;

        m = children.length;

        indexMap = new HashMap<String, Integer>();
        preQueHiMap = new HashMap<String, ArrayList<OGAVectorClock>>();

        for(int i = 0; i < children.length; i++) {
            indexMap.put(children[i], new Integer(i + 1));
            preQueHiMap.put(children[i], null);
        }
    }

    @Override
    public void receive(Message message) throws RemoteException {
        OGAMessageContent content = message.getOgaMessageContent();
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
        
        if(messageSenderIndex != index) return;
        
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
