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

import static config.Config.ENABLE_PHYSICAL_CLOCK;
import static config.Config.LOG_DIRECTORY;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.components.MessageDispatcher;
import net.sourceforge.mipa.components.MessageType;
import net.sourceforge.mipa.predicatedetection.AbstractChecker;

/**
 * OGA algorithm sub checker.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class OGASubChecker extends AbstractChecker {

    private static final long serialVersionUID = -8872862161054484454L;

    private ArrayList<ArrayList<OGAMessageContent>> queues;

    private ArrayList<ArrayList<Message>> msgBuffer;

    private String[] topCheckers;

    private long[] currentMessageCount;

    private PrintWriter out;

    public OGASubChecker(ResultCallback application, String checkerName,
                         String[] topCheckers, String[] children) {
        super(application, checkerName, children);

        this.topCheckers = topCheckers;

        currentMessageCount = new long[children.length];

        queues = new ArrayList<ArrayList<OGAMessageContent>>();
        msgBuffer = new ArrayList<ArrayList<Message>>();

        for (int i = 0; i < children.length; i++) {
            queues.add(new ArrayList<OGAMessageContent>());
            msgBuffer.add(new ArrayList<Message>());
            currentMessageCount[i] = 0;
        }

        if (ENABLE_PHYSICAL_CLOCK) {
            try {
                out = new PrintWriter(LOG_DIRECTORY + "/" + name + ".log");
                for (int i = 0; i < topCheckers.length; i++) {
                    out.print(topCheckers[i]);
                    if (i != topCheckers.length - 1)
                        out.print(" ");
                }
                out.println();
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void receive(Message message) throws RemoteException {

        ArrayList<Message> messages = new ArrayList<Message>();
        String child = message.getSenderID();
        int id = nameToID.get(child).intValue();

        long messageID = message.getMessageID();
        add(msgBuffer.get(id), message);

        if (messageID == currentMessageCount[id]) {

            if (isContinuous(msgBuffer.get(id), id) == true) {
                ArrayList<Message> buffer = msgBuffer.get(id);
                int size = buffer.size();
                for (int i = 0; i < size; i++) {
                    Message elem = buffer.remove(0);
                    messages.add(elem);
                }
                check(messages);
            }
        }
    }

    private void add(ArrayList<Message> messages, Message msg) {
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

    private boolean isContinuous(ArrayList<Message> messages, int id) {
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
    
    // FIX bug of issue 8 at http://mipa.googlecode.com
    private void addOnce(ArrayList<Integer> list, Integer num) {
        boolean already = false;
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).intValue() == num.intValue()) {
                already = true;
                break;
            }
        }
        if(already == false) list.add(num);
    }

    private void check(ArrayList<Message> messages) {

        String child = messages.get(0).getSenderID();
        int id = nameToID.get(child).intValue();

        ArrayList<OGAMessageContent> contents = new ArrayList<OGAMessageContent>();
        for (int i = 0; i < messages.size(); i++)
            contents.add(messages.get(i).getOgaMessageContent());

        ArrayList<OGAMessageContent> queue = queues.get(id);
        // queue.add(content);

        if(queue.size() != 0) {
            for (int i = 0; i < contents.size(); i++)
                queue.add(contents.get(i));
            
        } else {    // queue.size() == 0
            for (int i = 0; i < contents.size(); i++)
                queue.add(contents.get(i));

            ArrayList<Integer> changed = new ArrayList<Integer>();
            changed.add(new Integer(id));
            while (true) {
                while (changed.size() != 0) {
                    ArrayList<Integer> newchanged = new ArrayList<Integer>();
                    for (int i = 0; i < changed.size(); i++) {
                        int elem = changed.get(i).intValue();
                        for (int j = 0; j < children.length; j++) {
                            if (elem == j)
                                continue;
                            ArrayList<OGAMessageContent> qi = queues.get(elem);
                            ArrayList<OGAMessageContent> qj = queues.get(j);
                            if (qi.size() != 0 && qj.size() != 0) {
                                OGAMessageContent qiHead = qi.get(0);
                                OGAMessageContent qjHead = qj.get(0);
                                if (qjHead
                                          .getLo()
                                          .notLessThan(
                                                       qiHead
                                                             .getHi())) {

                                    //newchanged.add(new Integer(elem));
                                    addOnce(newchanged, new Integer(elem));
                                }
                                if (qiHead
                                          .getLo()
                                          .notLessThan(
                                                       qjHead
                                                             .getHi())) {

                                    //newchanged.add(new Integer(j));
                                    addOnce(newchanged, new Integer(j));
                                }
                            }
                        } // end for j
                    }// end for i
                    changed = newchanged;
                    for (int i = 0; i < changed.size(); i++) {
                        int elem = changed.get(i).intValue();
                        
                        assert(queues.get(elem).size() != 0);
                        queues.get(elem).remove(0);
                    }
                }// end while
                boolean found = true;
                for (int i = 0; i < children.length; i++) {
                    if (queues.get(i).size() == 0) {
                        found = false;
                        break;
                    }
                }
                /*
                 * if (found == true) { try {
                 * application.callback(String.valueOf(true)); } catch
                 * (Exception e) { e.printStackTrace(); } } else break;
                 */
                if (found == false)
                    break;

                // detection found
                // TODO we will implement or-activity in future. Currently only
                // and-activity
                ArrayList<OGAVectorClock> SetLo = new ArrayList<OGAVectorClock>();
                ArrayList<OGAVectorClock> SetHi = new ArrayList<OGAVectorClock>();
                for (int i = 0; i < children.length; i++) {
                    changed.add(new Integer(i));

                    OGAMessageContent foundContent = queues.get(i).remove(0);
                    SetLo.add(foundContent.getLo());
                    SetHi.add(foundContent.getHi());

                    if (ENABLE_PHYSICAL_CLOCK) {
                        String intervalID = foundContent.getIntervalID();
                        long lo = foundContent.getLo().getPhysicalClock();
                        long hi = foundContent.getHi().getPhysicalClock();
                        try {
                            String end = i + 1 != children.length ? " " : "\n";
                            out.print(intervalID + " " + lo + " " + hi + end);
                            out.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                /* prune result in SetLo and SetHi */
                // prune SetLo
                for (int i = 0; i < SetLo.size(); i++) {
                    OGAVectorClock clock_i = SetLo.get(i);
                    for (int j = 0; j < SetLo.size(); j++) {
                        if (i == j)
                            continue;
                        OGAVectorClock clock_j = SetLo.get(j);
                        // clock_i > clock_j
                        if (OGAVectorClock.compare(clock_i, clock_j) == 1) {
                            SetLo.remove(j);
                            if (i > j)
                                i--;
                            j--;
                        }
                    }
                }
                // prune SetHi
                for (int i = 0; i < SetHi.size(); i++) {
                    OGAVectorClock clock_i = SetHi.get(i);
                    for (int j = 0; j < SetHi.size(); j++) {
                        if (i == j)
                            continue;
                        OGAVectorClock clock_j = SetHi.get(j);
                        // clock_i < clock_j
                        if (OGAVectorClock.compare(clock_i, clock_j) == -1) {
                            SetHi.remove(j);
                            if (i > j)
                                i--;
                            j--;
                        }
                    }
                }
                // send SetHi and SetLo to Top Checker.
                OGAMessageContent content = new OGAMessageContent();
                content.setSetHi(SetHi);
                content.setSetLo(SetLo);

                for (int i = 0; i < topCheckers.length; i++)
                    send(MessageType.Detection, topCheckers[i], content);
            }
        } // :end else (queue.size() == 0)
    }

    private void send(MessageType type, String receiverName,
                      OGAMessageContent content) {
        Message m = new Message();
        m.setType(type);
        m.setSenderID(name);
        m.setReceiverID(receiverName);
        m.setOgaMessageContent(content);

        try {
            MessageDispatcher messageDispatcher = MIPAResource
                                                              .getMessageDispatcher();
            messageDispatcher.send(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
