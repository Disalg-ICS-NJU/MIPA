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

import org.apache.commons.lang.StringUtils;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.predicatedetection.Automaton;
import net.sourceforge.mipa.predicatedetection.Composite;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.NodeType;
import net.sourceforge.mipa.predicatedetection.RegularExpression;
import net.sourceforge.mipa.predicatedetection.State;
import net.sourceforge.mipa.predicatedetection.Structure;
import net.sourceforge.mipa.predicatedetection.lattice.AbstractLatticeIDNode;
import net.sourceforge.mipa.predicatedetection.lattice.WindowedLatticeChecker;
import net.sourceforge.mipa.predicatedetection.lattice.LocalState;

/**
 * 
 * @author Yiling Yang <csylyang@gmail.com>
 * 
 */
public class SequenceWindowedLatticeChecker extends WindowedLatticeChecker {
    private static final long serialVersionUID = -4389010288731310197L;

    // private SequenceLatticeIDNode precedeNode = null;;
    private PrintWriter out = null;
    private int count;
    private HashMap<String, ArrayList<String>> CGSToNPs;
    private RegularExpression predicate;
    private Automaton automaton;
    private NodeType type;

    private boolean SurfaceFlag = false;

    public SequenceWindowedLatticeChecker(ResultCallback application,
            String predicateID, String checkerName, String[] normalProcesses,
            Structure specification) {
        super(application, predicateID, checkerName, normalProcesses,
                specification);
        try {
            out = new PrintWriter(LOG_DIRECTORY + "/WindowSequence.log");
            out.println("Start...");
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

    private void labelingCGS(SequenceLatticeIDNode node) {
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

    private boolean computePredicate(SequenceLatticeIDNode node) {
        if (node == null) {
            return false;
        }
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
        // long time_t = (new Date()).getTime();
        if (type == NodeType.DEF) {
            boolean result = flagInclusion;
            if (result == true) {
                /*
                 * if (count == 0) { try { application.callback(predicateID,
                 * String.valueOf(true)); } catch (Exception e) {
                 * e.printStackTrace(); } } count++;
                 */
                return true;
            }
        } else if (type == NodeType.POS) {
            boolean result = flagIntersection;
            if (result == true) {
                /*
                 * if (count == 0) { try { application.callback(predicateID,
                 * String.valueOf(true)); } catch (Exception e) {
                 * e.printStackTrace(); } } count++;
                 */
                return true;
            }
        }
        // wastedTime += (new Date()).getTime() - time_t;
        return false;
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

    private void parsePredicateToAutomaton(Structure specification) {
        Structure GSE = specification.getChildren().get(1);
        type = GSE.getNodeType();
        predicate = parsePredicateToRegExp(GSE);
        predicate = modifyRegularExpression(predicate);
        automaton = parseRegExpToAutomaton(predicate);
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
            out.print("initialState: ");
            out.println(a.getInitialState().getName());
            out.print("acceptStates: ");
            Iterator<State> it = a.getAcceptStates().iterator();
            while (it.hasNext()) {
                out.print(it.next().getName() + " ");
            }
            out.println();
            out.flush();
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

    public void repeatCallBack() {
        // TODO Auto-generated method stub
        if (DEBUG) {
            System.out.println("=====repeatCallBack=====");
        }
    }

    @Override
    public SequenceLatticeIDNode createNode(LocalState[] globalState, String[] s) {
        SequenceLatticeIDNode node = new SequenceLatticeIDNode(globalState, s);
        return node;
    }

    @Override
    public boolean check(AbstractLatticeIDNode minCGS,
            AbstractLatticeIDNode maxCGS, int id) {
        // TODO Auto-generated method stub
        out.println("----------------------");
        if (maxCGS != null
                && maxCGS.getGlobalState()[id].getID().equals(
                        windowedLocalStateSet.get(id).get(
                                windowedLocalStateSet.get(id).size() - 1)
                                .getID())) {
            long time = (new Date()).getTime();
            computeReachableStates((SequenceLatticeIDNode) maxCGS);
            computeTime = (new Date()).getTime() - time;
        }
        if (getPruneFlag() == true) {
            updateReachableState((SequenceLatticeIDNode) minCGS,
                    (SequenceLatticeIDNode) maxCGS, id);
            setPruneFlag(false);
        }
        if (SurfaceFlag == false) {
            return computePredicate((SequenceLatticeIDNode) maxCGS);
        } else {
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
                    if (index[i].equals(windowedLocalStateSet.get(i).get(
                            windowedLocalStateSet.get(i).size() - 1).getID())) {
                        flag = true;
                        break;
                    }
                }
                if (flag == true) {
                    computePredicate((SequenceLatticeIDNode) getMappedLattice()
                            .get(ind));
                    if (type == NodeType.DEF) {
                        result = result
                                && ((SequenceLatticeIDNode) getMappedLattice()
                                        .get(ind)).getFlagInclusion();
                        if (result == false) {
                            return false;
                        }
                    } else if (type == NodeType.POS) {
                        result = ((SequenceLatticeIDNode) getMappedLattice()
                                .get(ind)).getFlagIntersection();
                        if (result == true) {
                            break;
                        }
                    }
                }
            }
            return result;
        }
    }

    private void computeReachableStates(SequenceLatticeIDNode CGS) {
        // TODO Auto-generated method stub
        labelingCGS(CGS);
        if (CGS.equals(minCGS)) {
            CGS.getReachedStates().clear();
            String[] string = CGS.getSatisfiedPredicates().split(" ");
            for (int i = 0; i < string.length; i++) {
                State state = automaton.getInitialState().step(
                        string[i].charAt(0));
                CGS.addReachedStates(state);
            }
            if (DEBUG) {
                long time_t = (new Date()).getTime();
                out.print("[ ");
                for (int i = 0; i < CGS.getID().length; i++) {
                    out.print(CGS.getID()[i] + " ");
                }
                out.print("]: satisfied predicates: "
                        + CGS.getSatisfiedPredicates());
                out.print(" reachable states: ");
                Iterator<State> it = CGS.getReachedStates().iterator();
                while (it.hasNext()) {
                    State state = it.next();
                    out.print(state.getName() + " ");
                }
                out.println();
                out.flush();
                wastedTime += (new Date()).getTime() - time_t;
            }
        } else {
            HashSet<State> precState = new HashSet<State>();
            for (int i = 0; i < children.length; i++) {
                String[] index = new String[children.length];
                for (int j = 0; j < children.length; j++) {
                    index[j] = CGS.getID()[j];
                }
                index[i] = Integer.toString(Integer.valueOf(index[i]) - 1);
                String ID = StringUtils.join(index, ' ');
                if (getMappedLattice().get(ID) != null) {
                    SequenceLatticeIDNode node = (SequenceLatticeIDNode) getMappedLattice()
                            .get(ID);
                    if (node.getReachedStates().size() == 0) {
                        computeReachableStates(node);
                    }
                    Iterator<State> iterator = node.getReachedStates()
                            .iterator();
                    while (iterator.hasNext()) {
                        precState.add(iterator.next());
                    }
                }
            }
            Iterator<State> iterator = precState.iterator();
            while (iterator.hasNext()) {
                State state = iterator.next();
                String[] satisfiedPredicate = CGS.getSatisfiedPredicates()
                        .split(" ");
                for (int i = 0; i < satisfiedPredicate.length; i++) {
                    if (!satisfiedPredicate[i].equals("")) {
                        char c = satisfiedPredicate[i].charAt(0);
                        State step = state.step(c);
                        CGS.addReachedStates(step);
                    }
                }
            }

            if (DEBUG) {
                long time_t = (new Date()).getTime();
                out.print("[ ");
                for (int i = 0; i < CGS.getID().length; i++) {
                    out.print(CGS.getID()[i] + " ");
                }
                out.print("]: satisfied predicates: "
                        + CGS.getSatisfiedPredicates());
                out.print(" reachable states: ");
                Iterator<State> it = CGS.getReachedStates().iterator();
                while (it.hasNext()) {
                    State state = it.next();
                    out.print(state.getName() + " ");
                }
                out.println();
                out.flush();
                wastedTime += (new Date()).getTime() - time_t;
            }
        }
    }

    private void updateReachableState(SequenceLatticeIDNode minCGS,
            AbstractLatticeIDNode maxCGS, int id) {
        // TODO Auto-generated method stub
        if (minCGS == null) {
            return;
        }

        /*
         * minCGS.getReachedStates().clear();
         * 
         * String[] string = minCGS.getSatisfiedPredicates().split(" "); for
         * (int i = 0; i < string.length; i++) { State state =
         * automaton.getInitialState().step(string[i].charAt(0));
         * minCGS.addReachedStates(state); } //
         * minCGS.addReachedStates(automaton.getInitialState()); if (DEBUG) {
         * long time_t = (new Date()).getTime(); out.print("[ "); for (int i =
         * 0; i < minCGS.getID().length; i++) { out.print(minCGS.getID()[i] +
         * " "); } out.print("]: satisfied predicates: " +
         * minCGS.getSatisfiedPredicates()); out.print(" reachable states: ");
         * Iterator<State> it = minCGS.getReachedStates().iterator(); while
         * (it.hasNext()) { State state = it.next(); out.print(state.getName() +
         * " "); } out.println(); out.flush(); wastedTime += (new
         * Date()).getTime() - time_t; } long time = (new Date()).getTime();
         * ArrayList<SequenceLatticeIDNode> set = new
         * ArrayList<SequenceLatticeIDNode>(); ArrayList<String> setID = new
         * ArrayList<String>(); set.add(minCGS);
         * setID.add(StringUtils.join(minCGS.getID(), ' ')); while
         * (!set.isEmpty()) { SequenceLatticeIDNode node = set.remove(0); for
         * (int i = 0; i < children.length; i++) { String[] index = new
         * String[children.length]; for (int j = 0; j < children.length; j++) {
         * index[j] = node.getID()[j]; } index[i] =
         * Integer.toString(Integer.valueOf(index[i]) + 1); String ID =
         * StringUtils.join(index, ' '); if (!setID.contains(ID) &&
         * getMappedLattice().get(ID) != null) { SequenceLatticeIDNode newNode =
         * (SequenceLatticeIDNode) getMappedLattice() .get(ID); if
         * (newNode.getGlobalState()[id].getID().equals(
         * minCGS.getGlobalState()[id].getID())) {
         * newNode.getReachedStates().clear(); computeReachableStates(newNode);
         * set.add(newNode); setID.add(StringUtils.join(newNode.getID(), ' '));
         * } else { HashSet<State> oriState = new HashSet<State>();
         * Iterator<State> iterator = newNode.getReachedStates() .iterator();
         * while (iterator.hasNext()) { oriState.add(iterator.next()); }
         * newNode.getReachedStates().clear(); computeReachableStates(newNode);
         * 
         * boolean flag = true; if (oriState.size() ==
         * newNode.getReachedStates() .size()) { String ori = ""; iterator =
         * oriState.iterator(); while (iterator.hasNext()) { State state =
         * iterator.next(); ori += state.getName() + " ";
         * 
         * } String news = ""; iterator = newNode.getReachedStates().iterator();
         * while (iterator.hasNext()) { State state = iterator.next(); news +=
         * state.getName() + " "; }
         * 
         * String[] oriStates = ori.trim().split(" "); String[] newStates =
         * news.trim().split(" "); for (int j = 0; j < oriStates.length; j++) {
         * String s = oriStates[j]; boolean f = false; for (int k = 0; k <
         * newStates.length; k++) { if (s.equals(newStates[k])) { f = true;
         * break; } } if (f == false) { flag = false; break; } } } else { flag =
         * false; } if (flag == false) { set.add(newNode);
         * setID.add(StringUtils.join(newNode.getID(), ' ')); } } } } } for (int
         * i = 0; i < children.length; i++) { String[] index = new
         * String[children.length]; for (int j = 0; j < children.length; j++) {
         * index[j] = node.getID()[j]; } index[i] =
         * Integer.toString(Integer.valueOf(index[i]) + 1); String ID =
         * StringUtils.join(index, ' '); if (!setID.contains(ID) &&
         * getMappedLattice().get(ID) != null) { SequenceLatticeIDNode newNode =
         * (SequenceLatticeIDNode) getMappedLattice() .get(ID); if
         * (newNode.getGlobalState()[id].getID().equals(
         * minCGS.getGlobalState()[id].getID())) {
         * newNode.getReachedStates().clear(); computeReachableStates(newNode);
         * set.add(newNode); setID.add(StringUtils.join(newNode.getID(), ' '));
         * } else { HashSet<State> oriState = new HashSet<State>();
         * Iterator<State> iterator = newNode.getReachedStates() .iterator();
         * while (iterator.hasNext()) { oriState.add(iterator.next()); }
         * newNode.getReachedStates().clear(); computeReachableStates(newNode);
         * 
         * boolean flag = true; if (oriState.size() ==
         * newNode.getReachedStates() .size()) { String ori = ""; iterator =
         * oriState.iterator(); while (iterator.hasNext()) { State state =
         * iterator.next(); ori += state.getName() + " ";
         * 
         * } String news = ""; iterator = newNode.getReachedStates().iterator();
         * while (iterator.hasNext()) { State state = iterator.next(); news +=
         * state.getName() + " "; }
         * 
         * String[] oriStates = ori.trim().split(" "); String[] newStates =
         * news.trim().split(" "); for (int j = 0; j < oriStates.length; j++) {
         * String s = oriStates[j]; boolean f = false; for (int k = 0; k <
         * newStates.length; k++) { if (s.equals(newStates[k])) { f = true;
         * break; } } if (f == false) { flag = false; break; } } } else { flag =
         * false; } if (flag == false) { set.add(newNode);
         * setID.add(StringUtils.join(newNode.getID(), ' ')); } } } }
         */

        long time = (new Date()).getTime();
        ArrayList<SequenceLatticeIDNode> set = new ArrayList<SequenceLatticeIDNode>();
        ArrayList<String> setID = new ArrayList<String>();
        set.add(minCGS);
        while (!set.isEmpty()) {
            SequenceLatticeIDNode node = set.remove(0);
            if (!setID.contains(StringUtils.join(node.getID(), ' '))) {
                setID.add(StringUtils.join(node.getID(), ' '));
                HashSet<State> oriState = new HashSet<State>();
                Iterator<State> iterator = node.getReachedStates().iterator();
                while (iterator.hasNext()) {
                    oriState.add(iterator.next());
                }
                if (node.equals(minCGS)) {
                    node.getReachedStates().clear();
                    String[] string = node.getSatisfiedPredicates().split(" ");
                    for (int i = 0; i < string.length; i++) {
                        State state = automaton.getInitialState().step(
                                string[i].charAt(0));
                        node.addReachedStates(state);
                    }
                    if (DEBUG) {
                        long time_t = (new Date()).getTime();
                        out.print("[ ");
                        for (int i = 0; i < node.getID().length; i++) {
                            out.print(node.getID()[i] + " ");
                        }
                        out.print("]: satisfied predicates: "
                                + node.getSatisfiedPredicates());
                        out.print(" reachable states: ");
                        Iterator<State> it = node.getReachedStates().iterator();
                        while (it.hasNext()) {
                            State state = it.next();
                            out.print(state.getName() + " ");
                        }
                        out.println();
                        out.flush();
                        wastedTime += (new Date()).getTime() - time_t;
                    }
                } else {
                    node.getReachedStates().clear();
                    computeReachableStates(node);
                }

                boolean flag = true;
                if (oriState.size() == node.getReachedStates().size()) {
                    String ori = "";
                    iterator = oriState.iterator();
                    while (iterator.hasNext()) {
                        State state = iterator.next();
                        ori += state.getName() + " ";

                    }
                    String news = "";
                    iterator = node.getReachedStates().iterator();
                    while (iterator.hasNext()) {
                        State state = iterator.next();
                        news += state.getName() + " ";
                    }

                    String[] oriStates = ori.trim().split(" ");
                    String[] newStates = news.trim().split(" ");
                    for (int j = 0; j < oriStates.length; j++) {
                        String s = oriStates[j];
                        boolean f = false;
                        for (int k = 0; k < newStates.length; k++) {
                            if (s.equals(newStates[k])) {
                                f = true;
                                break;
                            }
                        }
                        if (f == false) {
                            flag = false;
                            break;
                        }
                    }
                } else {
                    flag = false;
                }
                if (flag == false) {
                    for (int i = 0; i < children.length; i++) {
                        String[] index = new String[children.length];
                        for (int j = 0; j < children.length; j++) {
                            index[j] = node.getID()[j];
                        }
                        index[i] = Integer
                                .toString(Integer.valueOf(index[i]) + 1);
                        String ID = StringUtils.join(index, ' ');
                        if (getMappedLattice().get(ID) != null) {
                            set.add((SequenceLatticeIDNode) getMappedLattice()
                                    .get(ID));
                        }
                    }
                } else {
                    // [id] not change
                    if (node.getID()[id].equals(windowedLocalStateSet.get(id)
                            .get(0).getID())) {
                        for (int i = 0; i < children.length; i++) {
                            if (i == id) {
                                continue;
                            }
                            String[] index = new String[children.length];
                            for (int j = 0; j < children.length; j++) {
                                index[j] = node.getID()[j];
                            }
                            index[i] = Integer.toString(Integer
                                    .valueOf(index[i]) + 1);
                            String ID = StringUtils.join(index, ' ');
                            if (getMappedLattice().get(ID) != null) {
                                set
                                        .add((SequenceLatticeIDNode) getMappedLattice()
                                                .get(ID));
                            }
                        }
                    }
                }
            }
        }
        updateNumber = setID.size();
        updateTime = (new Date()).getTime() - time;
    }

    @Override
    public boolean checkOriLattice(AbstractLatticeIDNode CGS, int id) {
        // TODO Auto-generated method stub
        computeReachableStatesOri(CGS);
        return computePredicateOri((SequenceLatticeIDNode) CGS);
    }

    private boolean computePredicateOri(SequenceLatticeIDNode node) {
        // TODO Auto-generated method stub
        if (node == null) {
            return false;
        }
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
        long time_t = (new Date()).getTime();
        if (type == NodeType.DEF) {
            boolean result = flagInclusion;
            if (result == true) {
                if (count == 0) {
                    // to do
                }
                count++;
                return true;
            }
        } else if (type == NodeType.POS) {
            boolean result = flagIntersection;
            if (result == true) {
                if (count == 0) {
                    // to do
                }
                count++;
                return true;
            }
        }
        wastedOriTime += (new Date()).getTime() - time_t;
        return false;
    }

    private void computeReachableStatesOri(AbstractLatticeIDNode CGS) {
        // TODO Auto-generated method stub
        labelingCGS((SequenceLatticeIDNode) CGS);
        HashSet<State> precState = new HashSet<State>();
        for (int i = 0; i < children.length; i++) {
            String[] index = new String[children.length];
            for (int j = 0; j < children.length; j++) {
                index[j] = CGS.getID()[j];
            }
            index[i] = Integer.toString(Integer.valueOf(index[i]) - 1);
            String ID = StringUtils.join(index, ' ');
            if (oriMappedLattice.get(ID) != null) {
                SequenceLatticeIDNode node = (SequenceLatticeIDNode) oriMappedLattice
                        .get(ID);
                if (node.getReachedStates().size() == 0) {
                    computeReachableStatesOri(node);
                }
                Iterator<State> iterator = node.getReachedStates().iterator();
                while (iterator.hasNext()) {
                    precState.add(iterator.next());
                }
            }
        }
        Iterator<State> iterator = precState.iterator();
        while (iterator.hasNext()) {
            State state = iterator.next();
            String[] satisfiedPredicate = ((SequenceLatticeIDNode) CGS)
                    .getSatisfiedPredicates().split(" ");
            for (int i = 0; i < satisfiedPredicate.length; i++) {
                if (!satisfiedPredicate[i].equals("")) {
                    char c = satisfiedPredicate[i].charAt(0);
                    State step = state.step(c);
                    ((SequenceLatticeIDNode) CGS).addReachedStates(step);
                }
            }
        }
        if (DEBUG) {
            long time_t = (new Date()).getTime();
            outOriConstruction.print("[ ");
            for (int i = 0; i < CGS.getID().length; i++) {
                outOriConstruction.print(CGS.getID()[i] + " ");
            }
            outOriConstruction.print("]: reachable states: ");
            Iterator<State> it = ((SequenceLatticeIDNode) CGS)
                    .getReachedStates().iterator();
            while (it.hasNext()) {
                State state = it.next();
                outOriConstruction.print(state.getName() + " ");
            }
            outOriConstruction.println();
            outOriConstruction.flush();
            wastedOriTime += (new Date()).getTime() - time_t;
        }
    }
}
