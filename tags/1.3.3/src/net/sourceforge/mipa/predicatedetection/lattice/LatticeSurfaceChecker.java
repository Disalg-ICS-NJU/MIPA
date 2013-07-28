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
package net.sourceforge.mipa.predicatedetection.lattice;

import static config.Config.LOG_DIRECTORY;
import static config.Debug.DEBUG;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.predicatedetection.AbstractFIFOChecker;
import net.sourceforge.mipa.predicatedetection.State;
import net.sourceforge.mipa.predicatedetection.lattice.sequence.SequenceLatticeIDNode;
import net.sourceforge.mipa.predicatedetection.lattice.sequence.SequenceLatticeNode;

/**
 * 
 * @author Yiling Yang <csylyang@gmail.com>
 * 
 */
public abstract class LatticeSurfaceChecker extends AbstractFIFOChecker {

    /**
     * 
     */
    private static final long serialVersionUID = -8857276299013214921L;

    /** output the lattice constructor procedure information */
    private PrintWriter out = null;

    private PrintWriter mappedLatticeOut = null;

    /** store all the received local state */
    private ArrayList<ArrayList<LocalState>> localStateSet;

    private AbstractLatticeIDNode startNode;

    private AbstractLatticeIDNode maxNode;

    private AbstractLatticeIDNode[] extremaSurfaceNodes;

    private HashMap<String, AbstractLatticeIDNode> mappedLattice;

    private int latticeNumber = 0;

    private int surfaceNumber = 0;

    private PrintWriter outTime = null;
    
    private int[] interNumArray;

    private boolean[] interNumFlag;

    public long wastedTime = 0;

    public long responseTime = 0;

    private boolean flag = false;

    private boolean jOptionPane = false;

    public LatticeSurfaceChecker(ResultCallback application,
            String predicateID, String checkerName, String[] children) {
        super(application, predicateID, checkerName, children);
        localStateSet = new ArrayList<ArrayList<LocalState>>();
        extremaSurfaceNodes = new AbstractLatticeIDNode[children.length];
        mappedLattice = new HashMap<String, AbstractLatticeIDNode>();
        interNumArray = new int[children.length];
        interNumFlag = new boolean[children.length];
        if (DEBUG) {
            try {
                out = new PrintWriter(LOG_DIRECTORY + "/surfaceChecker.log");
                mappedLatticeOut = new PrintWriter(LOG_DIRECTORY
                        + "/mappedLatticeNode.log");
                outTime = new PrintWriter(LOG_DIRECTORY
                        + "/SurSequence_Time.log");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        addInitialCGS();
    }

    private void addInitialCGS() {
        String[] index = new String[children.length];
        LocalState[] CGS = new LocalState[children.length];

        for (int i = 0; i < children.length; i++) {
            index[i] = "0";
            interNumArray[i] = 0;
            interNumFlag[i] = true;
            LatticeVectorClock vc = new LatticeVectorClock(children.length);
            vc.increment(i);
            CGS[i] = new LocalState(i, interNumArray[i], vc, false);
            localStateSet.add(new ArrayList<LocalState>());
            localStateSet.get(i).add(CGS[i]);
        }
        startNode = createNode(CGS, index);
        maxNode = startNode;
        for (int i = 0; i < children.length; i++) {
            extremaSurfaceNodes[i] = startNode;
        }
        String ID = "";
        for (int j = 0; j < children.length; j++) {
            ID += index[j] + " ";
        }
        ID = ID.trim();
        mappedLattice.put(ID, startNode);

        if (DEBUG) {
            try {
                String id = "";
                String vc = "";
                String lp = "";
                for (int i = 0; i < index.length; i++) {
                    id += index[i] + " ";
                    vc += startNode.getGlobalState()[i].getvc().toString()
                            + "  ";
                    lp += startNode.getGlobalState()[i].getlocalPredicate()
                            + " ";
                }
                mappedLatticeOut.println("[I]: [" + id.trim() + "] ["
                        + vc.trim() + "] [" + lp.trim() + "]");
                mappedLatticeOut.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // experiment
        latticeNumber++;
    }

    @Override
    protected void handle(ArrayList<Message> messages) {
        String normalProcess = messages.get(0).getSenderID();
        int id = nameToID.get(normalProcess).intValue();

        for (int i = 0; i < messages.size(); i++) {
            
           // response time
            responseTime = 0;
            wastedTime = 0;
            long time_1 = (new Date()).getTime();

            Message message = messages.get(i);
            LatticeMessageContent content = (LatticeMessageContent) message
                    .getMessageContent();
            
           // compute interval number of current process
            if (content.getlocalPredicate() == true) {
                if (interNumFlag[id] == true) {
                    interNumArray[id]++;
                    interNumFlag[id] = false;
                }
            } else {
                interNumFlag[id] = true;
            }
            
            LocalState localState = new LocalState(id, interNumArray[id],
                    content.getlvc(), content.getlocalPredicate());
            localStateSet.get(id).add(localState);

            // output the lattice constructor procedure information
            if (DEBUG) {
                long time_2 = (new Date()).getTime();
                try {
                    out.println(message.getMessageID() + ", " + normalProcess
                            + ", " + content.getlocalPredicate() + ", "
                            + content.getlvc().toString());
                    out.flush();
                    mappedLatticeOut.println();
                    mappedLatticeOut.println("=============== process id:" + id
                            + " vc:" + localState.getvc().toString() + " lp:"
                            + localState.getlocalPredicate()
                            + " ===============");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                wastedTime += (new Date()).getTime() - time_2;
            }
            expandLattice(localState, id);
            
            // response time
            responseTime = (new Date()).getTime() - time_1 - wastedTime;
            outTime.println(responseTime);
            outTime.flush();
            mappedLatticeOut.println("responseTime: "+responseTime);
            mappedLatticeOut.flush();
            /*if (jOptionPane == false) {
                JOptionPane.showMessageDialog(null, "Detection complete!");
                jOptionPane = true;
            }*/
        }
    }

    private void expandLattice(LocalState localState, int id) {
        long ti = (new Date()).getTime();
        // the new localState will not affect anything about lattice
        if (localStateSet.get(id).size() != Integer
                .valueOf(maxNode.getID()[id]) + 2) {
            // lattice will not be changed
            repeatCallBack();

            if (DEBUG) {
                long time_1 = (new Date()).getTime();
                try {
                    for (int i = 0; i < children.length; i++) {
                        if (extremaSurfaceNodes[i] != null) {
                            String ind = "";
                            String vc = "";
                            String lp = "";
                            for (int j = 0; j < children.length; j++) {
                                ind += extremaSurfaceNodes[i].getID()[j] + " ";
                                vc += extremaSurfaceNodes[i].getGlobalState()[j]
                                        .getvc().toString()
                                        + "  ";
                                lp += extremaSurfaceNodes[i].getGlobalState()[j]
                                        .getlocalPredicate()
                                        + " ";
                            }
                            mappedLatticeOut
                                    .println("[N] extremaSurfaceNode: [" + i
                                            + "] [" + ind.trim() + "] ["
                                            + vc.trim() + "] [" + lp.trim()
                                            + "]");
                            mappedLatticeOut.flush();
                        } else {
                            mappedLatticeOut
                                    .println("[N] extremaSurfaceNode: [" + i
                                            + "] null");
                            mappedLatticeOut.flush();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                wastedTime += (new Date()).getTime() - time_1;
            }
        } else {
            LocalState[] globalState = getCombinedGlobalState(maxNode,
                    localState, id);
            // the next global state of maxNode on the NP(id) is CGS, thus new
            // CGS will be grew.
            if (isCGS(globalState, id)) {
                // grow lattice with new added CGSs, and update new maxNode and
                // extremaSurfaceNode, then check()
                
                long t = (new Date()).getTime();
                
                // get extremaSurfaceNode at the NP(id) side
                extremaSurfaceNodes[id] = findNewExtremaSurfaceNode(
                        extremaSurfaceNodes[id], localState, id);
                String ID = "";
                for (int j = 0; j < children.length; j++) {
                    ID += extremaSurfaceNodes[id].getID()[j] + " ";
                }
                ID = ID.trim();
                mappedLattice.put(ID, extremaSurfaceNodes[id]);

                // experiment
                latticeNumber++;
                
                mappedLatticeOut.println("FindExtre: "+((new Date()).getTime()-t)+"                 ");

                if (DEBUG) {
                    long time_1 = (new Date()).getTime();
                    try {
                        String ind = "";
                        String vc = "";
                        String lp = "";
                        for (int i = 0; i < children.length; i++) {
                            ind += extremaSurfaceNodes[id].getID()[i] + " ";
                            vc += extremaSurfaceNodes[id].getGlobalState()[i]
                                    .getvc().toString()
                                    + "  ";
                            lp += extremaSurfaceNodes[id].getGlobalState()[i]
                                    .getlocalPredicate()
                                    + " ";
                        }
                        mappedLatticeOut.println("[I]: [" + ind.trim() + "] ["
                                + vc.trim() + "] [" + lp.trim() + "]");
                        mappedLatticeOut.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    wastedTime += (new Date()).getTime() - time_1;
                }

                t = (new Date()).getTime();
                // check on the new added CGS
                checkOnNode(extremaSurfaceNodes[id]);
                mappedLatticeOut.println("CheckExtre: "+((new Date()).getTime()-t)+"                 ");
                
                t = (new Date()).getTime();
                // recursive grow all new CGSs
                ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
                set.add(extremaSurfaceNodes[id]);
                while (!set.isEmpty()) {
                    AbstractLatticeIDNode node = set.remove(0);
                    boolean hasSub = false;
                    for (int i = 0; i < children.length; i++) {
                        if (i == id)
                            continue;
                        String[] index = new String[children.length];
                        for (int j = 0; j < children.length; j++) {
                            index[j] = node.getID()[j];
                        }
                        index[i] = Integer
                                .toString(Integer.valueOf(index[i]) + 1);
                        ID = "";
                        for (int j = 0; j < children.length; j++) {
                            ID += index[j] + " ";
                        }
                        ID = ID.trim();
                        if (indexIsValid(index)
                                && mappedLattice.get(ID) == null) {
                            LocalState[] newGlobalState = getCombinedGlobalState(
                                    node, localStateSet.get(i).get(
                                            Integer.valueOf(index[i])), i);
                            if (isCGS(newGlobalState, i)) {
                                hasSub = true;
                                AbstractLatticeIDNode newNode = createNode(
                                        newGlobalState, index);
                                set.add(newNode);
                                mappedLattice.put(ID, newNode);

                                // experiment
                                latticeNumber++;

                                // check on each new added CGSs
                                checkOnNode(newNode);

                                if (DEBUG) {
                                    long time_1 = (new Date()).getTime();
                                    try {
                                        String ind = "";
                                        String vc = "";
                                        String lp = "";
                                        for (int j = 0; j < index.length; j++) {
                                            ind += index[j] + " ";
                                            vc += newNode.getGlobalState()[j]
                                                    .getvc().toString()
                                                    + "  ";
                                            lp += newNode.getGlobalState()[j]
                                                    .getlocalPredicate()
                                                    + " ";
                                        }
                                        mappedLatticeOut.println("[I]: ["
                                                + ind.trim() + "] ["
                                                + vc.trim() + "] [" + lp.trim()
                                                + "]");
                                        mappedLatticeOut.flush();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    wastedTime += (new Date()).getTime()
                                            - time_1;
                                }
                            }
                        }
                    }
                    if (!hasSub) {
                        // find maxNode
                        maxNode = node;
                    }
                }
                mappedLatticeOut.println("Grow: "+((new Date()).getTime()-t)+"                 ");
                
                
                if (DEBUG) {
                    long time_1 = (new Date()).getTime();
                    try {
                        String ind = "";
                        String vc = "";
                        String lp = "";
                        for (int j = 0; j < children.length; j++) {
                            ind += maxNode.getID()[j] + " ";
                            vc += maxNode.getGlobalState()[j].getvc()
                                    .toString()
                                    + "  ";
                            lp += maxNode.getGlobalState()[j]
                                    .getlocalPredicate()
                                    + " ";
                        }
                        mappedLatticeOut.println("[N] maxNode: [" + ind.trim()
                                + "] [" + vc.trim() + "] [" + lp.trim() + "]");
                        mappedLatticeOut.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    wastedTime += (new Date()).getTime() - time_1;
                }

                // till now, grow complete
                t = (new Date()).getTime();
                // update new extremaSurfaceNodes
                for (int i = 0; i < children.length; i++) {
                    if (Integer.valueOf(maxNode.getID()[i]) != localStateSet
                            .get(i).size() - 1) {
                        extremaSurfaceNodes[i] = null;
                        if (DEBUG) {
                            try {
                                mappedLatticeOut
                                        .println("[N] extremaSurfaceNode: ["
                                                + i + "] null");
                                mappedLatticeOut.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        continue;
                    }
                    ArrayList<AbstractLatticeIDNode> backSet = new ArrayList<AbstractLatticeIDNode>();
                    backSet.add(maxNode);
                    while (!backSet.isEmpty()) {
                        AbstractLatticeIDNode newNode = backSet.remove(0);
                        ArrayList<AbstractLatticeIDNode> prec = precWithID(
                                newNode, i);
                        if (prec.isEmpty()) {
                            extremaSurfaceNodes[i] = newNode;
                            if (DEBUG) {
                                long time_1 = (new Date()).getTime();
                                try {
                                    String ind = "";
                                    String vc = "";
                                    String lp = "";
                                    for (int j = 0; j < children.length; j++) {
                                        ind += extremaSurfaceNodes[i].getID()[j]
                                                + " ";
                                        vc += extremaSurfaceNodes[i]
                                                .getGlobalState()[j].getvc()
                                                .toString()
                                                + "  ";
                                        lp += extremaSurfaceNodes[i]
                                                .getGlobalState()[j]
                                                .getlocalPredicate()
                                                + " ";
                                    }
                                    mappedLatticeOut
                                            .println("[N] extremaSurfaceNode: ["
                                                    + i
                                                    + "] ["
                                                    + ind.trim()
                                                    + "] ["
                                                    + vc.trim()
                                                    + "] ["
                                                    + lp.trim() + "]");
                                    mappedLatticeOut.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                wastedTime += (new Date()).getTime() - time_1;
                            }
                            break;
                        } else {
                            int num = prec.size();
                            for (int j = 0; j < num; j++) {
                                AbstractLatticeIDNode node = prec.remove(0);
                                if (!backSet.contains(node)) {
                                    backSet.add(node);
                                }
                            }
                        }
                    }
                }
                // do surface checking
                check(maxNode);
                mappedLatticeOut.println("UpdateExtrema: "+((new Date()).getTime()-t)+"           ");
                
                t = (new Date()).getTime();
                
                // delete dead CGSs from old Surface
                    Set<String> keySet = getMappedLattice().keySet();
                    Iterator<String> it = keySet.iterator();
                    while (it.hasNext()) {
                        String ind = it.next();
                        String[] index = ind.split(" ");
                        boolean flag = false;
                        for (int i = 0; i < children.length; i++) {
                            if (Integer.valueOf(index[i]) == getLocalStateSet()
                                    .get(i).size() - 1) {
                                flag = true;
                                break;
                            }
                        }
                        if(flag == false){
                            if (DEBUG) {
                                long time_1 = (new Date()).getTime();
                                try {
                                    String vc = "";
                                    String lp = "";
                                    for (int j = 0; j < children.length; j++) {
                                        vc += mappedLattice.get(ind).getGlobalState()[j].getvc()
                                                .toString()
                                                + "  ";
                                        lp += mappedLattice.get(ind).getGlobalState()[j]
                                                .getlocalPredicate()
                                                + " ";
                                    }
                                    mappedLatticeOut.println("[O]: ["
                                            + ind + "] [" + vc.trim()
                                            + "] [" + lp.trim() + "]");
                                    mappedLatticeOut.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                wastedTime += (new Date()).getTime() - time_1;
                            }
                            //mappedLattice.remove(ind);
                            it.remove();
                        }
                    }
                
                
                /*// delete dead CGSs from old Surface
                ArrayList<AbstractLatticeIDNode> old = new ArrayList<AbstractLatticeIDNode>();
                old.add(oldMaxNode);
                while (!old.isEmpty()) {
                    AbstractLatticeIDNode node = old.remove(0);
                    boolean F = false;
                    for(int i=0;i<children.length;i++) {
                        if(Integer.valueOf(node.getID()[i]) == localStateSet.get(i).size()-1){
                            F = true;
                            break;
                        }
                    }
                    ArrayList<AbstractLatticeIDNode> pre = precWithID(node,id);
                    for(int i=0;i<pre.size();i++) {
                        if(!old.contains(pre.get(i))){
                            old.add(pre.get(i));
                        }
                    }
                    if(F == false) {
                        String ind = "";
                        for (int j = 0; j < children.length; j++) {
                            ind += node.getID()[j] + " ";
                        }
                        ind = ind.trim();
                        if (mappedLattice.get(ind) != null) {
                            mappedLattice.remove(ind);
                            if (DEBUG) {
                                long time_1 = (new Date()).getTime();
                                try {
                                    ind = "";
                                    String vc = "";
                                    String lp = "";
                                    for (int j = 0; j < children.length; j++) {
                                        ind += node.getID()[j] + " ";
                                        vc += node.getGlobalState()[j].getvc()
                                                .toString()
                                                + "  ";
                                        lp += node.getGlobalState()[j]
                                                .getlocalPredicate()
                                                + " ";
                                    }
                                    mappedLatticeOut.println("[O]: ["
                                            + ind.trim() + "] [" + vc.trim()
                                            + "] [" + lp.trim() + "]");
                                    mappedLatticeOut.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                wastedTime += (new Date()).getTime() - time_1;
                            }
                        }
                    }
                }*/
                
                
                /*
                
                // delete dead CGSs from old Surface
                ArrayList<AbstractLatticeIDNode> old = new ArrayList<AbstractLatticeIDNode>();
                old.add(oldEetremaNode);
                while (!old.isEmpty()) {
                    AbstractLatticeIDNode node = old.remove(0);
                    boolean flag = false;
                    for (int i = 0; i < children.length; i++) {
                        if (Integer.valueOf(node.getID()[i]) == localStateSet
                                .get(i).size() - 1) {
                            flag = true;
                            continue;
                        }
                    }

                    if (flag == false) {
                        String ind = "";
                        for (int j = 0; j < children.length; j++) {
                            ind += node.getID()[j] + " ";
                        }
                        ind = ind.trim();
                        if (mappedLattice.get(ind) != null) {
                            mappedLattice.remove(ind);

                            if (DEBUG) {
                                long time_1 = (new Date()).getTime();
                                try {
                                    ind = "";
                                    String vc = "";
                                    String lp = "";
                                    for (int j = 0; j < children.length; j++) {
                                        ind += node.getID()[j] + " ";
                                        vc += node.getGlobalState()[j].getvc()
                                                .toString()
                                                + "  ";
                                        lp += node.getGlobalState()[j]
                                                .getlocalPredicate()
                                                + " ";
                                    }
                                    mappedLatticeOut.println("[O]: ["
                                            + ind.trim() + "] [" + vc.trim()
                                            + "] [" + lp.trim() + "]");
                                    mappedLatticeOut.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                wastedTime += (new Date()).getTime() - time_1;
                            }
                        }
                    }

                    for (int i = 0; i < children.length; i++) {
                        if (i == id)
                            continue;
                        String[] index = new String[children.length];
                        for (int j = 0; j < children.length; j++) {
                            index[j] = node.getID()[j];
                        }
                        index[i] = Integer
                                .toString(Integer.valueOf(index[i]) + 1);
                        String ind = "";
                        for (int j = 0; j < children.length; j++) {
                            ind += index[j] + " ";
                        }
                        ind = ind.trim();
                        if (indexIsValid(index)
                                && mappedLattice.get(ind) != null) {
                            old.add(mappedLattice.get(ind));
                        }
                    }
                }*/
                mappedLatticeOut.println("Delete old: "+((new Date()).getTime()-t)+"             ");
            } else {
                // delete dead CGSs in Surface, and check()
                // another choice: only update extremaSurfaceNodes[id], without

             // delete dead CGSs from old Surface
                Set<String> keySet = getMappedLattice().keySet();
                Iterator<String> it = keySet.iterator();
                while (it.hasNext()) {
                    String ind = it.next();
                    String[] index = ind.split(" ");
                    boolean flag = false;
                    for (int i = 0; i < children.length; i++) {
                        if (Integer.valueOf(index[i]) == getLocalStateSet()
                                .get(i).size() - 1) {
                            flag = true;
                            break;
                        }
                    }
                    if(flag == false){
                        if (DEBUG) {
                            long time_1 = (new Date()).getTime();
                            try {
                                String vc = "";
                                String lp = "";
                                for (int j = 0; j < children.length; j++) {
                                    vc += mappedLattice.get(ind).getGlobalState()[j].getvc()
                                            .toString()
                                            + "  ";
                                    lp += mappedLattice.get(ind).getGlobalState()[j]
                                            .getlocalPredicate()
                                            + " ";
                                }
                                mappedLatticeOut.println("[O]: ["
                                        + ind + "] [" + vc.trim()
                                        + "] [" + lp.trim() + "]");
                                mappedLatticeOut.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            wastedTime += (new Date()).getTime() - time_1;
                        }
                        //mappedLattice.remove(ind);
                        it.remove();
                    }
                }
                
                /*// delete dead CGSs
                ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
                set.add(extremaSurfaceNodes[id]);
                while (!set.isEmpty()) {
                    AbstractLatticeIDNode node = set.remove(0);
                    for (int i = 0; i < children.length; i++) {
                        if (i == id)
                            continue;
                        String[] index = new String[children.length];
                        for (int j = 0; j < children.length; j++) {
                            index[j] = node.getID()[j];
                        }
                        index[i] = Integer
                                .toString(Integer.valueOf(index[i]) + 1);
                        String ID = "";
                        for (int j = 0; j < children.length; j++) {
                            ID += index[j] + " ";
                        }
                        ID = ID.trim();
                        boolean flag = false;
                        for (int j = 0; j < children.length; j++) {
                            if (Integer.valueOf(index[j]) == Integer
                                    .valueOf(localStateSet.get(j).size()) - 1) {
                                flag = true;
                            }
                        }
                        if (flag == false && mappedLattice.get(ID) != null) {
                            set.add(mappedLattice.get(ID));
                        }
                    }

                    String ID = "";
                    for (int j = 0; j < children.length; j++) {
                        ID += node.getID()[j] + " ";
                    }
                    ID = ID.trim();
                    boolean flag = false;
                    for (int j = 0; j < children.length; j++) {
                        if (Integer.valueOf(node.getID()[j]) == Integer
                                .valueOf(localStateSet.get(j).size()) - 1) {
                            flag = true;
                        }
                    }
                    if (flag == false && mappedLattice.get(ID) != null) {
                        mappedLattice.remove(ID);
                        if (DEBUG) {
                            long time_1 = (new Date()).getTime();
                            try {
                                String ind = "";
                                String vc = "";
                                String lp = "";
                                for (int j = 0; j < children.length; j++) {
                                    ind += node.getID()[j] + " ";
                                    vc += node.getGlobalState()[j].getvc()
                                            .toString()
                                            + "  ";
                                    lp += node.getGlobalState()[j]
                                            .getlocalPredicate()
                                            + " ";
                                }
                                mappedLatticeOut.println("[O]: [" + ind.trim()
                                        + "] [" + vc.trim() + "] [" + lp.trim()
                                        + "]");
                                mappedLatticeOut.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            wastedTime += (new Date()).getTime() - time_1;
                        }
                    }
                }*/
                
                extremaSurfaceNodes[id] = null;

                if (DEBUG) {
                    long time_1 = (new Date()).getTime();
                    try {
                        for (int i = 0; i < children.length; i++) {
                            if (extremaSurfaceNodes[i] != null) {
                                String ind = "";
                                String vc = "";
                                String lp = "";
                                for (int j = 0; j < children.length; j++) {
                                    ind += extremaSurfaceNodes[i].getID()[j]
                                            + " ";
                                    vc += extremaSurfaceNodes[i]
                                            .getGlobalState()[j].getvc()
                                            .toString()
                                            + "  ";
                                    lp += extremaSurfaceNodes[i]
                                            .getGlobalState()[j]
                                            .getlocalPredicate()
                                            + " ";
                                }
                                mappedLatticeOut
                                        .println("[N] extremaSurfaceNode: ["
                                                + i + "] [" + ind.trim()
                                                + "] [" + vc.trim() + "] ["
                                                + lp.trim() + "]");
                                mappedLatticeOut.flush();
                            } else {
                                mappedLatticeOut
                                        .println("[N] extremaSurfaceNode: ["
                                                + i + "] null");
                                mappedLatticeOut.flush();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    wastedTime += (new Date()).getTime() - time_1;
                }

                // do surface checking
                check(maxNode);
            }
        }
        // surfaceNumber & latticeNumber & runtime size of lattice
        if (DEBUG) {
            long time_1 = (new Date()).getTime();
            surfaceNumber = 0;
            Set<String> keySet = mappedLattice.keySet();
            Iterator<String> it = keySet.iterator();
            while (it.hasNext()) {
                String ind = it.next();
                String[] index = ind.split(" ");
                for (int i = 0; i < children.length; i++) {
                    if (Integer.valueOf(index[i]) == localStateSet.get(i)
                            .size() - 1) {
                        surfaceNumber++;
                        break;
                    }
                }
            }
            mappedLatticeOut.println("[N] SurfaceNumber: " + surfaceNumber);
            mappedLatticeOut.flush();
            mappedLatticeOut.println("[N] LatticeNumber: " + latticeNumber);
            mappedLatticeOut.flush();
            mappedLatticeOut.println("[N] Size of Lattice: "
                    + mappedLattice.size());
            mappedLatticeOut.flush();
            wastedTime += (new Date()).getTime() - time_1;
        }
        mappedLatticeOut.println((new Date()).getTime() - ti);
        mappedLatticeOut.flush();
    }

    private boolean indexIsValid(String[] index) {
        for (int i = 0; i < children.length; i++) {
            if (Integer.valueOf(index[i]) > Integer.valueOf(localStateSet
                    .get(i).size()) - 1) {
                return false;
            }
        }
        return true;
    }

    private AbstractLatticeIDNode findNewExtremaSurfaceNode(
            AbstractLatticeIDNode oriNode, LocalState localState, int id) {
        String[] temp = new String[children.length];
        AbstractLatticeIDNode tempNode = null;
        
        Set<String> keySet = getMappedLattice().keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String s = it.next();
            String[] ind = s.split(" ");
            if (Integer.valueOf(ind[id]) == getLocalStateSet()
                        .get(id).size() - 2) {
                LocalState[] globalState = getCombinedGlobalState(mappedLattice.get(s), localState,
                        id);
                if (isCGS(globalState, id)) {
                    String[] index = new String[children.length];
                    for (int j = 0; j < children.length; j++) {
                        index[j] = mappedLattice.get(s).getID()[j];
                    }
                    index[id] = Integer.toString(localStateSet.get(id).size() - 1);
                    AbstractLatticeIDNode newNode  = createNode(globalState, index);
                    if(tempNode != null) {
                        boolean flag = false;
                        for(int i =0;i<children.length;i++) {
                            if(Integer.valueOf(index[i]) < Integer.valueOf(temp[i])) {
                                flag = true;
                                break;
                            }
                        }
                        if(flag == true) {
                            for(int i =0;i<children.length;i++) {
                                temp[i] = index[i];
                            }
                            tempNode = newNode;
                        }
                    }
                    else {
                        for(int i =0;i<children.length;i++) {
                            temp[i] = index[i];
                        }
                        tempNode = newNode;
                    }
                }
            }
        }
        return tempNode;
        
        /*AbstractLatticeIDNode newNode = null;
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        set.add(oriNode);
        while (!set.isEmpty()) {
            AbstractLatticeIDNode node = set.remove(0);
            LocalState[] globalState = getCombinedGlobalState(node, localState,
                    id);
            if (isCGS(globalState, id)) {
                String[] index = new String[children.length];
                for (int j = 0; j < children.length; j++) {
                    index[j] = node.getID()[j];
                }
                index[id] = Integer.toString(localStateSet.get(id).size() - 1);
                newNode = createNode(globalState, index);
                return newNode;
            } else {
                for (int i = 0; i < children.length; i++) {
                    if (i == id)
                        continue;
                    String[] index = new String[children.length];
                    for (int j = 0; j < children.length; j++) {
                        index[j] = node.getID()[j];
                    }
                    index[i] = Integer.toString(Integer.valueOf(index[i]) + 1);
                    String ID = "";
                    for (int j = 0; j < children.length; j++) {
                        ID += index[j] + " ";
                    }
                    ID = ID.trim();
                    if (mappedLattice.get(ID) != null) {
                        set.add(mappedLattice.get(ID));
                    }
                }
            }
        }
        return newNode;*/
    }

    private boolean isCGS(LocalState[] CGS, int id) {
        for (int i = 0; i < children.length; i++) {
            if (i != id && (CGS[i].vc.lessThan(CGS[id].vc))) {
                return false;
            }
        }
        return true;
    }

    private LocalState[] getCombinedGlobalState(AbstractLatticeIDNode oriNode,
            LocalState localState, int id) {
        LocalState[] globalState = new LocalState[children.length];
        for (int i = 0; i < children.length; i++) {
            globalState[i] = oriNode.getGlobalState()[i];
        }
        globalState[id] = localState;
        return globalState;
    }

    protected ArrayList<AbstractLatticeIDNode> prec(AbstractLatticeIDNode node) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        for (int i = 0; i < children.length; i++) {
            String[] index = new String[children.length];
            for (int j = 0; j < children.length; j++) {
                index[j] = node.getID()[j];
            }
            index[i] = Integer.toString(Integer.valueOf(index[i]) - 1);
            String ID = "";
            for (int j = 0; j < children.length; j++) {
                ID += index[j] + " ";
            }
            ID = ID.trim();
            if (mappedLattice.get(ID) != null) {
                set.add(mappedLattice.get(ID));
            }
        }
        return set;
    }

    private ArrayList<AbstractLatticeIDNode> precWithID(
            AbstractLatticeIDNode node, int id) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        for (int i = 0; i < children.length; i++) {
            if (i == id)
                continue;
            String[] index = new String[children.length];
            for (int j = 0; j < children.length; j++) {
                index[j] = node.getID()[j];
            }
            index[i] = Integer.toString(Integer.valueOf(index[i]) - 1);
            String ID = "";
            for (int j = 0; j < children.length; j++) {
                ID += index[j] + " ";
            }
            ID = ID.trim();
            if (mappedLattice.get(ID) != null) {
                set.add(mappedLattice.get(ID));
            }
        }
        return set;
    }

    protected ArrayList<AbstractLatticeIDNode> sub(AbstractLatticeIDNode node) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        for (int i = 0; i < children.length; i++) {
            String[] index = new String[children.length];
            for (int j = 0; j < children.length; j++) {
                index[j] = node.getID()[j];
            }
            index[i] = Integer.toString(Integer.valueOf(index[i]) + 1);
            String ID = "";
            for (int j = 0; j < children.length; j++) {
                ID += index[j] + " ";
            }
            ID = ID.trim();
            if (indexIsValid(index) && mappedLattice.get(ID) != null) {
                set.add(mappedLattice.get(ID));
            }
        }
        return set;
    }

    private ArrayList<AbstractLatticeIDNode> subWithID(
            AbstractLatticeIDNode node, int id) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        for (int i = 0; i < children.length; i++) {
            if (i == id)
                continue;
            String[] index = new String[children.length];
            for (int j = 0; j < children.length; j++) {
                index[j] = node.getID()[j];
            }
            index[i] = Integer.toString(Integer.valueOf(index[i]) + 1);
            String ID = "";
            for (int j = 0; j < children.length; j++) {
                ID += index[j] + " ";
            }
            ID = ID.trim();
            if (indexIsValid(index) && mappedLattice.get(ID) != null) {
                set.add(mappedLattice.get(ID));
            }
        }
        return set;
    }

    protected AbstractLatticeIDNode[] getExtremaSurfaceNodes() {
        return extremaSurfaceNodes;
    }

    protected AbstractLatticeIDNode getMaxNode() {
        return maxNode;
    }

    public AbstractLatticeIDNode getStartNode() {
        return startNode;
    }

    public ArrayList<ArrayList<LocalState>> getLocalStateSet() {
        return localStateSet;
    }

    public void setLocalStateSet(ArrayList<ArrayList<LocalState>> localStateSet) {
        this.localStateSet = localStateSet;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setStartNode(AbstractLatticeIDNode startNode) {
        this.startNode = startNode;
    }

    public HashMap<String, AbstractLatticeIDNode> getMappedLattice() {
        return mappedLattice;
    }

    public void setMappedLattice(
            HashMap<String, AbstractLatticeIDNode> mappedLattice) {
        this.mappedLattice = mappedLattice;
    }

    public abstract AbstractLatticeIDNode createNode(LocalState[] globalState,
            String[] s);

    public abstract void checkOnNode(AbstractLatticeIDNode node);

    public abstract void check(AbstractLatticeIDNode currentNode);

    public abstract void repeatCallBack();
}
