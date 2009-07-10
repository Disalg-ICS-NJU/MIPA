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

import static config.Debug.DEBUG;

import java.rmi.RemoteException;
import java.util.ArrayList;

import net.sourceforge.mipa.ResultCallback;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.components.MessageContent;
import net.sourceforge.mipa.predicatedetection.AbstractChecker;
import net.sourceforge.mipa.predicatedetection.VectorClock;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class SCPChecker extends AbstractChecker {

    private static final long serialVersionUID = -4933006939390647117L;

    private ArrayList<ArrayList<MessageContent>> queues;

    /**
     * @param application
     * @param checkerName
     * @param normalProcesses
     */
    public SCPChecker(ResultCallback application, String checkerName,
                      String[] normalProcesses) {
        super(application, checkerName, normalProcesses);
        // TODO Auto-generated constructor stub
        queues = new ArrayList<ArrayList<MessageContent>>();
        for (int i = 0; i < normalProcesses.length; i++) {
            queues.add(new ArrayList<MessageContent>());
        }
    }

    @Override
    public void receive(Message message) throws RemoteException {
        /*
        if (DEBUG) {
            System.out.println("received message");
            MessageContent debug = message.getContent();
            VectorClock lo = debug.getLo();
            ArrayList<Long> list = lo.getVectorClock();
            for(int i = 0; i < list.size(); i++)
                System.out.print(list.get(i));
            System.out.print(' ');
            VectorClock hi = debug.getHi();
            list = hi.getVectorClock();
            for(int i = 0; i < list.size(); i++)
                System.out.print(list.get(i));
            System.out.println();
        }
        */
        String normalProcess = message.getSenderID();
        int id = nameToID.get(normalProcess).intValue();
        
        if(DEBUG) System.out.println(id);
        
        MessageContent content = message.getContent();

        ArrayList<MessageContent> queue = queues.get(id);
        queue.add(content);

        ArrayList<Integer> changed = new ArrayList<Integer>();

        if (queue.size() == 1) {
            if(DEBUG) System.out.println("In size == 1");
            changed.add(new Integer(id));
            while (true) {
                while (changed.size() != 0) {
                    ArrayList<Integer> newchanged = new ArrayList<Integer>();
                    for (int i = 0; i < changed.size(); i++) {
                        int elem = changed.get(i).intValue();
                        for (int j = 0; j < normalProcesses.length; j++) {
                            if (elem == j)
                                continue;
                            ArrayList<MessageContent> qi = queues.get(elem);
                            ArrayList<MessageContent> qj = queues.get(j);
                            if (qi.size() != 0 && qj.size() != 0) {
                                MessageContent qiHead = qi.get(0);
                                MessageContent qjHead = qj.get(0);
                                if (qjHead
                                          .getLo()
                                          .notLessThan(
                                                       qiHead
                                                             .getHi())) {
                                    //if(DEBUG) System.out.println("condition 1");
                                    newchanged.add(new Integer(elem));
                                }
                                if (qiHead
                                          .getLo()
                                          .notLessThan(
                                                       qjHead
                                                             .getHi())) {
                                    //if(DEBUG) System.out.println("condition 2");
                                    newchanged.add(new Integer(j));
                                }
                            }
                        } // end for j
                    }// end for i
                    changed = newchanged;
                    for (int i = 0; i < changed.size(); i++) {
                        int elem = changed.get(i).intValue();
                        queues.get(elem).remove(0);
                    }
                }// end while
                boolean found = true;
                for (int i = 0; i < normalProcesses.length; i++) {
                    if (queues.get(i).size() == 0) {
                        found = false;
                        break;
                    }
                }
                if (found == true) {
                    try {
                        application.callback(String.valueOf(true));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    break;

                // detection found
                for (int i = 0; i < normalProcesses.length; i++) {
                    queues.get(i).remove(0);
                    changed.add(new Integer(i));
                }
            }
        }
    }
}
