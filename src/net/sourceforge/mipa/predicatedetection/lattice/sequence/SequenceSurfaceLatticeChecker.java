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
package net.sourceforge.mipa.predicatedetection.lattice.sequence;

import static config.Config.LOG_DIRECTORY;
import static config.Debug.DEBUG;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.predicatedetection.Automaton;
import net.sourceforge.mipa.predicatedetection.Composite;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.NodeType;
import net.sourceforge.mipa.predicatedetection.RegularExpression;
import net.sourceforge.mipa.predicatedetection.State;
import net.sourceforge.mipa.predicatedetection.Structure;
import net.sourceforge.mipa.predicatedetection.lattice.AbstractLatticeIDNode;
import net.sourceforge.mipa.predicatedetection.lattice.LatticeSurfaceChecker;
import net.sourceforge.mipa.predicatedetection.lattice.LocalState;

/**
 * 
 * @author Yiling Yang <csylyang@gmail.com>
 * 
 */
public class SequenceSurfaceLatticeChecker extends LatticeSurfaceChecker {
    private static final long serialVersionUID = -4389010288731310197L;

    // private SequenceLatticeIDNode precedeNode = null;;
    private PrintWriter out = null;
    private PrintWriter debug = null;
    private int count;
    private HashMap<String, ArrayList<String>> CGSToNPs;
    private RegularExpression predicate;
    private Automaton automaton;
    private NodeType type;

    public SequenceSurfaceLatticeChecker(ResultCallback application,
            String predicateID, String checkerName, String[] normalProcesses,
            Structure specification) {
        super(application, predicateID, checkerName, normalProcesses);
        try {
            out = new PrintWriter(LOG_DIRECTORY + "/SurSequence.log");
            debug = new PrintWriter(LOG_DIRECTORY + "/SurSequence_Debug.log");
            out.println("Start...");
            debug.println("Start...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        count = 0;
        CGSToNPs = new HashMap<String, ArrayList<String>>();
        getCGSToNPs(specification);
        parsePredicateToAutomaton(specification);
        ((SequenceLatticeIDNode) getStartNode()).addReachedStates(automaton
                .getInitialState());
        ((SequenceLatticeIDNode) getStartNode()).setVisited(true);
    }

    public void check(AbstractLatticeIDNode currentNode) {
        if (getFlag() == false) {
            boolean result = false;
            if (type == NodeType.DEF) {
                result = true;
            } else if (type == NodeType.POS) {
                result = false;
            }
            
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
                if(flag == true){
                    if (type == NodeType.DEF) {
                        result = result && ((SequenceLatticeIDNode)getMappedLattice().get(ind)).getFlagInclusion();
                        if (result == false) {
                            return;
                        }
                    } else if (type == NodeType.POS) {
                        result = ((SequenceLatticeIDNode)getMappedLattice().get(ind)).getFlagIntersection();
                        if (result == true) {
                            break;
                        }
                    }
                }
            }
            
            
            /*ArrayList<SequenceLatticeIDNode> set = new ArrayList<SequenceLatticeIDNode>();
            for (int i = 0; i < children.length; i++) {
                if (getExtremaSurfaceNodes()[i] != null) {
                    if (!set
                            .contains((SequenceLatticeIDNode) (getExtremaSurfaceNodes()[i]))) {
                        set
                                .add((SequenceLatticeIDNode) getExtremaSurfaceNodes()[i]);
                    }
                }
            }

            
            while (!set.isEmpty()) {
                SequenceLatticeIDNode node = set.remove(0);
                if (type == NodeType.DEF) {
                    result = result && node.getFlagInclusion();
                    if (result == false) {
                        return;
                    }
                } else if (type == NodeType.POS) {
                    result = node.getFlagIntersection();
                    if (result == true) {
                        break;
                    }
                }
                ArrayList<AbstractLatticeIDNode> sub = sub(node);
                if (!sub.isEmpty()) {
                    int num = sub.size();
                    for (int j = 0; j < num; j++) {
                        SequenceLatticeIDNode subNode = (SequenceLatticeIDNode) sub
                                .remove(0);
                        if (!set.contains(subNode)) {
                            set.add(subNode);
                        }
                    }
                }
            }*/

            if (result == true) {
                if(count == 0) {
                    try {
                        application.callback(predicateID, String.valueOf(true));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                long time_1 = (new Date()).getTime();

                count++;
                if(count == 1) {
                    out.println(count + ": ");
                    out.println("==========surface nodes==========");
    
                    keySet = getMappedLattice().keySet();
                    it = keySet.iterator();
                    while (it.hasNext()) {
                        String id = it.next();
                        String[] index = id.split(" ");
                        boolean flag = false;
                        for (int i = 0; i < children.length; i++) {
                            if (Integer.valueOf(index[i]) == getLocalStateSet()
                                    .get(i).size() - 1) {
                                flag = true;
                                break;
                            }
                        }
                        if(flag == true){
                            out.print("id: " + id);
                            out.print(" reachedStates: ");
                            Iterator<State> iterator = ((SequenceLatticeIDNode) getMappedLattice()
                                    .get(id)).getReachedStates().iterator();
                            while (iterator.hasNext()) {
                                out.print(iterator.next().getName() + " ");
                            }
                            out.print("Intevals: ");
                            for (int i = 0; i < children.length; i++) {
                                String end = i + 1 != children.length ? " " : " \r\n";
                                out.print(((SequenceLatticeIDNode) getMappedLattice()
                                        .get(id)).getGlobalState()[i].getintervalID() + end);
                                out.flush();
                            }
                        }
                    }
                    out.flush();
                }
                setFlag(true);//
                wastedTime += (new Date()).getTime() - time_1;
            }
        }
    }

    private void computeCGS(SequenceLatticeIDNode node) {
        Set<String> CGSs = CGSToNPs.keySet();
        Iterator<String> it = CGSs.iterator();
        while (it.hasNext()) {
            String CGS = it.next();
            boolean result = true;
            ArrayList<String> NPs = CGSToNPs.get(CGS);
            for (int i = 0; i < NPs.size(); i++) {
                String np = NPs.get(i);
                String[] c = np.split("ss");
                assert (c.length == 2);
                int npIndex = Integer.valueOf(c[1]);
                result = result
                        && (node.getGlobalState()[npIndex].getlocalPredicate());
            }
            if (result == true) {
                node.addSatisfiedPredicates(CGS.charAt(0));
            }
        }
        // add {}
        if (node.getSatisfiedPredicates().trim().equals("")) {
            node.addSatisfiedPredicates('z');
        }
        node.setVisited(true);
    }

    private void detectSequence(SequenceLatticeIDNode node) {
        computeCGS(node);
        ArrayList<SequenceLatticeIDNode> precede = new ArrayList<SequenceLatticeIDNode>();
        ArrayList<AbstractLatticeIDNode> prec = prec(node);
        for (int i = 0; i < prec.size(); i++) {
            precede.add((SequenceLatticeIDNode) prec.get(i));
        }

        /*
         * if the node is the initial node, precede is null. if (node.getID() ==
         * getStartNode().getID()) {
         * node.addReachedStates(automaton.getInitialState()); } else {
         */
        /*
         * Iterator<SequenceLatticeIDNode> it = precede.iterator(); while
         * (it.hasNext()) { SequenceLatticeIDNode predNode = it.next(); if
         * (predNode.getVisited() == false) { detectSequence(predNode);
         * predNode.setVisited(true); } }
         */

        Iterator<SequenceLatticeIDNode> it = precede.iterator();
        while (it.hasNext()) {
            SequenceLatticeIDNode preNode = it.next();
            HashSet<State> precedeState = preNode.getReachedStates();
            Iterator<State> iterator = precedeState.iterator();
            while (iterator.hasNext()) {
                State state = iterator.next();
                String[] satisfiedPredicate = node.getSatisfiedPredicates()
                        .split(" ");
                for (int i = 0; i < satisfiedPredicate.length; i++) {
                    if (!satisfiedPredicate[i].equals("")) {
                        char c = satisfiedPredicate[i].charAt(0);
                        State step = state.step(c);
                        node.addReachedStates(step);
                    }
                }
            }
        }
        computePredicate(node);
        /* } */
        if (DEBUG) {
            long time_1 = (new Date()).getTime();
            String[] s = node.getID();
            for (int i = 0; i < s.length; i++) {
                debug.print(s[i] + " ");
                debug.flush();
            }
            debug.print(": ");
            LocalState[] gs = node.getGlobalState();
            for (int i = 0; i < gs.length; i++) {
                // String end = i + 1 != children.length ? " " : "\r\n";
                debug.print(gs[i].getlocalPredicate() + " ");
                debug.flush();
            }
            debug.print(": SatisfiedPredicates: ");
            debug.print(node.getSatisfiedPredicates());
            debug.print(": ");
            debug.print("reachedStates: ");
            debug.flush();
            Iterator<State> iterator = node.getReachedStates().iterator();
            while (iterator.hasNext()) {
                debug.print(iterator.next().getName() + " ");
                debug.flush();
            }
            debug.println();
            debug.flush();
            wastedTime += (new Date()).getTime() - time_1;
        }
    }

    private void computePredicate(SequenceLatticeIDNode node) {
        boolean flagIntersection = false;
        boolean flagInclusion = true;
        HashSet<State> reachedStates = node.getReachedStates();
        HashSet<State> acceptStates = automaton.getAcceptStates();
        Iterator<State> it = reachedStates.iterator();
        while (it.hasNext()) {
            boolean flag = false;
            State state = it.next();
            Iterator<State> iterator = acceptStates.iterator();
            while (iterator.hasNext()) {
                State acceptState = iterator.next();
                if (state.getName().equals(acceptState.getName())) {
                    flag = true;
                    break;
                }
            }
            if (flag == false) {
                flagInclusion = false;
            } else {
                flagIntersection = true;
            }
            if (flagInclusion == false && flagIntersection == true) {
                break;
            }
        }
        if (reachedStates.isEmpty()) {
            node.setFlagInclusion(false);
            node.setFlagIntersection(false);
        } else {
            node.setFlagInclusion(flagInclusion);
            node.setFlagIntersection(flagIntersection);
        }
    }

    @Override
    public SequenceLatticeIDNode createNode(LocalState[] globalState, String[] s) {
        SequenceLatticeIDNode node = new SequenceLatticeIDNode(globalState, s);
        return node;
    }

    private void getCGSToNPs(Structure specification) {
        Structure CGSs = specification.getChildren().get(0);
        for (int i = 0; i < CGSs.getChildren().size(); i++) {
            ArrayList<String> NPs = new ArrayList<String>();
            Structure CGS = CGSs.getChildren().get(i);
            String name = ((Composite) CGS).getNodeName();
            for (int j = 0; j < CGS.getChildren().size(); j++) {
                NPs.add(((LocalPredicate) CGS.getChildren().get(j))
                        .getNormalProcess());
            }
            CGSToNPs.put(name, NPs);
        }
        if (DEBUG) {
            printCGSToNPs();
        }
    }

    private void parsePredicateToAutomaton(Structure specification) {
        Structure GSE = specification.getChildren().get(1);
        type = GSE.getNodeType();
        predicate = parsePredicateToRegExp(GSE);
        predicate = modifyRegularExpression(predicate);
        automaton = parseRegExpToAutomaton(predicate);
    }

    private void printCGSToNPs() {
        System.out.println("========================================");
        System.out.println("Print CGS name to NPs:");
        Set<String> names = CGSToNPs.keySet();
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String result = "";
            String name = it.next();
            result += name + ":  ";
            ArrayList<String> NPs = CGSToNPs.get(name);
            for (int i = 0; i < NPs.size(); i++) {
                result += NPs.get(i) + "  ";
            }
            System.out.println(result);
        }
        System.out.println("Print over");
        System.out.println("----------------------------------------");
        System.out.println();
    }

    private Automaton parseRegExpToAutomaton(RegularExpression regularExpression) {
        if (DEBUG) {
            System.out.println("========================================");
            System.out.println("Parse regular expression to automaton:");
        }
        long time_1 = (new Date()).getTime();
        Automaton a = new Automaton(regularExpression);
        long time_2 = (new Date()).getTime();
        if (DEBUG) {
            System.out.println(a.toString());
            System.out.println("Parse over");
            System.out.println("Parse time: " + (time_2 - time_1));
            System.out.println("----------------------------------------");
            System.out.println();
        }
        if (DEBUG) {
            debug.print("initialState: ");
            debug.println(a.getInitialState().getName());
            debug.print("acceptStates: ");
            Iterator<State> it = a.getAcceptStates().iterator();
            while (it.hasNext()) {
                debug.print(it.next().getName() + " ");
            }
            debug.println();
            debug.flush();
        }
        return a;
    }

    private RegularExpression parsePredicateToRegExp(Structure GSE) {
        if (DEBUG) {
            System.out.println("========================================");
            System.out.println("Parse predicate to regular expression:");
        }
        String string = "";
        for (int i = 0; i < GSE.getChildren().size(); i++) {
            string += parseElement(GSE.getChildren().get(i));
        }
        RegularExpression predicate = new RegularExpression(string);
        if (DEBUG) {
            System.out.println(string);
            System.out.println("Parse over");
            System.out.println("----------------------------------------");
            System.out.println();
        }
        return predicate;
    }

    private RegularExpression modifyRegularExpression(
            RegularExpression regularExpression) {
        if (DEBUG) {
            System.out.println("========================================");
            System.out.println("Modify regular expression:");
        }
        HashSet<String> identifiers = regularExpression.getIdentifiers();
        String expression = regularExpression.getRegularExpression();
        String result = "";
        Iterator<String> it = identifiers.iterator();
        boolean firstPosition = true;
        while (it.hasNext()) {
            String identifier = it.next();
            if (firstPosition == true) {
                result += "(" + identifier + ")";
                firstPosition = false;
            } else {
                result += "|(" + identifier + ")";
            }
        }
        // add {}
        result += "|(" + "z" + ")";

        result = "((" + result + ")*)";
        result = result + expression + result;
        RegularExpression predicate = new RegularExpression(result);
        if (DEBUG) {
            System.out.println(result);
            System.out.println("Modify over");
            System.out.println("----------------------------------------");
            System.out.println();
        }
        return predicate;
    }

    private String parseElement(Structure structure) {
        // TODO Auto-generated method stub
        NodeType nodeType = structure.getNodeType();
        String result = "";
        switch (nodeType) {
        case ZEROORMORE: {
            String elements = "";
            for (int i = 0; i < structure.getChildren().size(); i++) {
                String element = parseElement(structure.getChildren().get(i));
                elements += element;
            }
            result = "((" + elements + ")*)";
            break;
        }
        case ONEORMORE: {
            String elements = "";
            for (int i = 0; i < structure.getChildren().size(); i++) {
                String element = parseElement(structure.getChildren().get(i));
                elements += element;
            }
            result = "((" + elements + ")+)";
            break;
        }
        case CHOICE: {
            String elements = "";
            for (int i = 0; i < structure.getChildren().size(); i++) {
                String element = parseElement(structure.getChildren().get(i));
                if (i == 0) {
                    elements += element;
                } else {
                    elements += "|" + element;
                }
            }
            result = "(" + elements + ")";
            break;
        }
        case OPTIONAL: {
            String elements = "";
            for (int i = 0; i < structure.getChildren().size(); i++) {
                String element = parseElement(structure.getChildren().get(i));
                elements += element;
            }
            result = "((" + elements + ")?)";
            break;
        }
        case CGS: {
            String name = ((Composite) structure).getNodeName();
            result = "(" + name + ")";
            break;
        }
        default: {
            System.out.println("Parse error: not defined connector: "
                    + nodeType.toString());
            break;
        }
        }
        return result;
    }

    public void checkOnNode(AbstractLatticeIDNode node) {
        // TODO Auto-generated method stub
        detectSequence((SequenceLatticeIDNode) node);
    }

    public void repeatCallBack() {
        // TODO Auto-generated method stub
        if (DEBUG) {
            System.out.println("=====repeatCallBack=====");
        }
    }
}
