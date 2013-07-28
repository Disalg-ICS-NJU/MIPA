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
import org.apache.commons.lang.StringUtils;
import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.predicatedetection.AbstractFIFOChecker;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.Structure;
import net.sourceforge.mipa.test.PhysicalTimeInterval;

/**
 * 
 * @author Yiling Yang <csylyang@gmail.com>
 * 
 */
public abstract class WindowedLatticeChecker extends AbstractFIFOChecker {
    /**
     * 
     */
    private static final long serialVersionUID = 5970384654877012269L;

    /** output the lattice constructor procedure information */
    // private PrintWriter out = null;
    /** store all the received local state */
    protected ArrayList<ArrayList<LocalState>> localStateSet;

    protected ArrayList<ArrayList<LocalState>> windowedLocalStateSet;

    private int[] windowSize;

    private AbstractLatticeIDNode startCGS;

    protected AbstractLatticeIDNode minCGS;

    private AbstractLatticeIDNode maxCGS;

    // private AbstractLatticeIDNode[] extremaSurfaceNodes;

    private HashMap<String, AbstractLatticeIDNode> mappedLattice;

    public int latticeNumber = 0;

    public int windowedLatticeNumber = 0;

    private PrintWriter outTime = null;

    private int[] interNumArray;

    private boolean[] interNumFlag;

    public long wastedTime = 0;

    public long oriTime = 0;

    public long wastedOriTime = 0;

    public long responseTime = 0;

    public long growTime = 0;

    public long pruneTime = 0;

    public long computeTime = 0;

    public long updateTime = 0;

    public int updateNumber = 0;

    // private boolean jOptionPane = false;

    private boolean pruneFlag = false;

    private PrintWriter outConstruction = null;

    public HashMap<String, AbstractLatticeIDNode> oriMappedLattice;

    public AbstractLatticeIDNode oriMaxCGS;

    public PrintWriter outOriConstruction = null;

    public PrintWriter outPhysicalTime = null;

    public PrintWriter outDetection = null;

    private int DetectNumber = 0;

    private int OriDetectNumber = 0;

    private int PhysicalNumber = 0;

    public WindowedLatticeChecker(ResultCallback application,
            String predicateID, String checkerName, String[] children,
            Structure specification) {
        super(application, predicateID, checkerName, children);
        localStateSet = new ArrayList<ArrayList<LocalState>>();
        mappedLattice = new HashMap<String, AbstractLatticeIDNode>();
        windowedLocalStateSet = new ArrayList<ArrayList<LocalState>>();
        interNumArray = new int[children.length];
        interNumFlag = new boolean[children.length];
        windowSize = new int[children.length];
        if (DEBUG) {
            try {
                // out = new PrintWriter(LOG_DIRECTORY
                // + "/windowedLatticeChecker.log");
                outTime = new PrintWriter(LOG_DIRECTORY
                        + "/WindowedLattice_Time.log");
                outConstruction = new PrintWriter(LOG_DIRECTORY
                        + "/WindowedLattice_Construction.log");
                outOriConstruction = new PrintWriter(LOG_DIRECTORY
                        + "/OriLattice_Construction.log");
                outPhysicalTime = new PrintWriter(LOG_DIRECTORY
                        + "/PhysicalTime.log");
                outDetection = new PrintWriter(LOG_DIRECTORY + "/Detection.log");
                oriMappedLattice = new HashMap<String, AbstractLatticeIDNode>();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        addInitialCGS();
        getWindowSize(specification);
    }

    private void getWindowSize(Structure specification) {
        ArrayList<Structure> structure = specification.getChildren();
        Structure CGSsNode = structure.get(0);
        ArrayList<Structure> CGSNode = CGSsNode.getChildren();
        for (int i = 0; i < CGSNode.size(); i++) {
            ArrayList<Structure> LPsNode = CGSNode.get(i).getChildren();
            for (int j = 0; j < LPsNode.size(); j++) {
                String normalProcess = ((LocalPredicate) LPsNode.get(j))
                        .getNormalProcess();
                int index = Integer.valueOf(normalProcess.split("ss")[1]);
                 windowSize[index] = Integer.valueOf(((LocalPredicate) LPsNode
                 .get(j)).getWindowSize());
                //windowSize[index] = 4;
            }
        }
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
            CGS[i].setID("0");
            localStateSet.add(new ArrayList<LocalState>());
            localStateSet.get(i).add(CGS[i]);
            windowedLocalStateSet.add(new ArrayList<LocalState>());
            windowedLocalStateSet.get(i).add(CGS[i]);
        }
        startCGS = createNode(CGS, index);
        minCGS = startCGS;
        maxCGS = startCGS;
        oriMaxCGS = startCGS;
        String ID = StringUtils.join(index, ' ');
        mappedLattice.put(ID, startCGS);
        if (DEBUG) {
            oriMappedLattice.put(ID, startCGS);
            outOriConstruction.println("[In]" + ID);
            outOriConstruction.flush();
            outConstruction.println("[In]" + ID);
            outConstruction.flush();
            latticeNumber = 1;
        }
        windowedLatticeNumber = 1;
    }

    @Override
    protected void handle(ArrayList<Message> messages) {
        String normalProcess = messages.get(0).getSenderID();
        int id = nameToID.get(normalProcess).intValue();
        for (int i = 0; i < messages.size(); i++) {
            outOriConstruction.println("receive message from NP"
                    + messages.get(i).getSenderID() + "["
                    + messages.get(i).getMessageID() + ": ["
                    + messages.get(i).getTimestamp().toString() + "]]");
            outOriConstruction.flush();
        }
        for (int i = 0; i < messages.size(); i++) {

            // response time
            responseTime = 0;
            wastedTime = 0;
            growTime = pruneTime = computeTime = updateTime = updateNumber = 0;

            Message message = messages.get(i);
            LatticeMessageContent content = (LatticeMessageContent) message
                    .getMessageContent();

            outOriConstruction.println("deal with NP" + message.getSenderID()
                    + "[" + message.getMessageID() + ": ["
                    + message.getTimestamp().toString() + "]]");
            outOriConstruction.flush();
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
                    content.getlvc(), content.getlocalPredicate(), content
                            .getPhysicalTime());
            outOriConstruction.println("Get localState from message ["
                    + message.getSenderID() + "[" + message.getMessageID()
                    + ": [" + message.getTimestamp().toString() + "]]");
            outOriConstruction.flush();
            localStateSet.get(id).add(localState);
            localState.setID(String.valueOf(localStateSet.get(id).size() - 1));
            windowedLocalStateSet.get(id).add(localState);

            // maintenance of window
            if (windowedLocalStateSet.get(id).size() > windowSize[id]) {
                windowedLocalStateSet.get(id).remove(0);
            }
            if (DEBUG) {
                outConstruction.println("------" + id + "------");
                outConstruction.flush();
                outOriConstruction.println("------" + id + "------");
                outOriConstruction.flush();
            }
            long time_1 = (new Date()).getTime();
            growLattice(localState, id);
            growTime = (new Date()).getTime() - time_1;
            if (minCGS != null
                    && Integer.valueOf(minCGS.getGlobalState()[id].getID()) < Integer
                            .valueOf(windowedLocalStateSet.get(id).get(0)
                                    .getID())) {
                pruneLattice(minCGS, id);
                pruneFlag = true;
            }
            boolean checkResult = check(minCGS, maxCGS, id);
            responseTime = (new Date()).getTime() - time_1 - wastedTime;
            if (checkResult == true) {
            	if(DetectNumber == 0) {
                    try {
                    	System.out.println("The predicate "+predicateID+" is satisfied.");
                        application.callback(String.valueOf(true));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                DetectNumber++;
                outDetection.write("DetectNumber: " + DetectNumber + "\n");
                outDetection.flush();
            }
            if (DEBUG) {
                windowedLatticeNumber = mappedLattice.size();
                outConstruction.println("windowedLattice: "
                        + windowedLatticeNumber);
                outConstruction.println("updateNumber: " + updateNumber);
                outConstruction.flush();
                outTime.println("windowTime: " + responseTime);
                outTime.println("windowTime': "
                        + (growTime + pruneTime + computeTime + updateTime));
                outTime.println("growTime: " + growTime);
                outTime.println("pruneTime: " + pruneTime);
                outTime.println("computeTime: " + computeTime);
                outTime.println("updateTime: " + updateTime);
                outTime.flush();
            }

            if (DEBUG) {
                boolean oriDetect = false;
                boolean flg = false;
                long time_2 = (new Date()).getTime();
                if (flg == true) {
                    growOriLattice(localState, id);
                    oriDetect = checkOriLattice(oriMaxCGS, id);
                }
                oriTime = (new Date()).getTime() - time_2;

                if (oriDetect == true) {
                    OriDetectNumber++;
                    outDetection.write("OriDetectNumber: " + OriDetectNumber
                            + "\n");
                    outDetection.flush();
                }

                latticeNumber = oriMappedLattice.size();
                outOriConstruction.println("oriLattice: " + latticeNumber);
                outOriConstruction.println("oriTime: " + oriTime);
                outOriConstruction.flush();
                outConstruction.println("oriLattice: " + latticeNumber);
                outConstruction.flush();
                outTime.println("oriTime: " + oriTime);
                outTime.flush();
                if (minCGS != null) {
                    outConstruction.println("minCGS: "
                            + StringUtils.join(minCGS.getID(), ' '));
                    outConstruction.println("maxCGS: "
                            + StringUtils.join(maxCGS.getID(), ' '));
                    outConstruction.flush();
                } else {
                    outConstruction.println("minCGS: " + "null");
                    outConstruction.println("maxCGS: " + "null");
                    outConstruction.flush();
                }
                outOriConstruction.println("oriMaxCGS: "
                        + StringUtils.join(oriMaxCGS.getID(), ' '));
                outOriConstruction.flush();

                /*
                 * // out local state set for (int j = 0; j <
                 * localStateSet.size(); j++) { ArrayList<LocalState> local =
                 * localStateSet.get(j); outOriConstruction.print(j + ":: ");
                 * for (int k = 0; k < local.size(); k++) { LocalState ls =
                 * local.get(k); outOriConstruction.print("["+ls.getID() + ": "
                 * + ls.getvc().toString() + "] "); }
                 * outOriConstruction.println(); } outOriConstruction.flush();
                 */
            }

            if (DEBUG) {
                /*
                 * if (children.length == 2) {
                 * ArrayList<ArrayList<PhysicalTimeInterval>> nonChecker = new
                 * ArrayList<ArrayList<PhysicalTimeInterval>>();
                 * nonChecker.add(new ArrayList<PhysicalTimeInterval>());
                 * nonChecker.add(new ArrayList<PhysicalTimeInterval>());
                 * boolean findHead = false; ArrayList<LocalState> window =
                 * windowedLocalStateSet.get(0); for (int k = 0; k <
                 * window.size(); k++) { if (window.get(k).getlocalPredicate()
                 * == true) { if (findHead == false) { findHead = true;
                 * PhysicalTimeInterval p = new PhysicalTimeInterval(
                 * window.get(k).getPhysicalTime()); nonChecker.get(0).add(p); }
                 * else { PhysicalTimeInterval p = nonChecker.get(0).get(
                 * nonChecker.get(0).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } else { if
                 * (findHead == true) { findHead = false; PhysicalTimeInterval p
                 * = nonChecker.get(0).get( nonChecker.get(0).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } }
                 * 
                 * findHead = false; window = windowedLocalStateSet.get(1); for
                 * (int k = 0; k < window.size(); k++) { if
                 * (window.get(k).getlocalPredicate() == true) { if (findHead ==
                 * false) { findHead = true; PhysicalTimeInterval p = new
                 * PhysicalTimeInterval( window.get(k).getPhysicalTime());
                 * nonChecker.get(1).add(p); } else { PhysicalTimeInterval p =
                 * nonChecker.get(1).get( nonChecker.get(1).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } else { if
                 * (findHead == true) { findHead = false; PhysicalTimeInterval p
                 * = nonChecker.get(1).get( nonChecker.get(1).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } } if
                 * (nonChecker.get(0).size() != 0 && nonChecker.get(1).size() !=
                 * 0) { while (nonChecker.get(0).get(0).getpTimeHi() != 0 &&
                 * nonChecker.get(0).get(0).getpTimeHi() < nonChecker
                 * .get(1).get(0).getpTimeLo()) { nonChecker.get(0).remove(0);
                 * if (nonChecker.get(0).size() == 0) { break; } } if
                 * (nonChecker.get(0).size() != 0) { while
                 * (nonChecker.get(1).get(0).getpTimeHi() != 0 &&
                 * nonChecker.get(1).get(0).getpTimeHi() < nonChecker
                 * .get(0).get(0).getpTimeLo()) { nonChecker.get(1).remove(0);
                 * if (nonChecker.get(1).size() == 0) { break; } } if
                 * (nonChecker.get(0).size() != 0 && nonChecker.get(1).size() !=
                 * 0) { if (nonChecker.get(0).get(0).getpTimeHi() == 0 &&
                 * nonChecker.get(1).get(0) .getpTimeHi() != 0) { if
                 * (nonChecker.get(0).get(0).getpTimeLo() >= nonChecker
                 * .get(1).get(0).getpTimeLo() && nonChecker.get(0).get(0)
                 * .getpTimeLo() <= nonChecker .get(1).get(0).getpTimeHi()) {
                 * PhysicalNumber++; outDetection.write("PhysicalNumber: " +
                 * PhysicalNumber + "\n"); outDetection.flush(); } } else if
                 * (nonChecker.get(0).get(0) .getpTimeHi() != 0 &&
                 * nonChecker.get(1).get(0) .getpTimeHi() == 0) { if
                 * (nonChecker.get(1).get(0).getpTimeLo() >= nonChecker
                 * .get(0).get(0).getpTimeLo() && nonChecker.get(1).get(0)
                 * .getpTimeLo() <= nonChecker .get(0).get(0).getpTimeHi()) {
                 * PhysicalNumber++; outDetection.write("PhysicalNumber: " +
                 * PhysicalNumber + "\n"); outDetection.flush(); } } else if
                 * (nonChecker.get(0).get(0) .getpTimeHi() != 0 &&
                 * nonChecker.get(1).get(0) .getpTimeHi() != 0) {
                 * PhysicalNumber++; outDetection.write("PhysicalNumber: " +
                 * PhysicalNumber + "\n"); outDetection.flush(); } } } } } else
                 * if (children.length == 3) {
                 * ArrayList<ArrayList<PhysicalTimeInterval>> nonChecker = new
                 * ArrayList<ArrayList<PhysicalTimeInterval>>();
                 * nonChecker.add(new ArrayList<PhysicalTimeInterval>());
                 * nonChecker.add(new ArrayList<PhysicalTimeInterval>());
                 * nonChecker.add(new ArrayList<PhysicalTimeInterval>());
                 * boolean findHead = false; ArrayList<LocalState> window =
                 * windowedLocalStateSet.get(0); for (int k = 0; k <
                 * window.size(); k++) { if (window.get(k).getlocalPredicate()
                 * == true) { if (findHead == false) { findHead = true;
                 * PhysicalTimeInterval p = new PhysicalTimeInterval(
                 * window.get(k).getPhysicalTime()); nonChecker.get(0).add(p); }
                 * else { PhysicalTimeInterval p = nonChecker.get(0).get(
                 * nonChecker.get(0).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } else { if
                 * (findHead == true) { findHead = false; PhysicalTimeInterval p
                 * = nonChecker.get(0).get( nonChecker.get(0).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } }
                 * 
                 * findHead = false; window = windowedLocalStateSet.get(1); for
                 * (int k = 0; k < window.size(); k++) { if
                 * (window.get(k).getlocalPredicate() == true) { if (findHead ==
                 * false) { findHead = true; PhysicalTimeInterval p = new
                 * PhysicalTimeInterval( window.get(k).getPhysicalTime());
                 * nonChecker.get(1).add(p); } else { PhysicalTimeInterval p =
                 * nonChecker.get(1).get( nonChecker.get(1).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } else { if
                 * (findHead == true) { findHead = false; PhysicalTimeInterval p
                 * = nonChecker.get(1).get( nonChecker.get(1).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } }
                 * 
                 * findHead = false; window = windowedLocalStateSet.get(2); for
                 * (int k = 0; k < window.size(); k++) { if
                 * (window.get(k).getlocalPredicate() == true) { if (findHead ==
                 * false) { findHead = true; PhysicalTimeInterval p = new
                 * PhysicalTimeInterval( window.get(k).getPhysicalTime());
                 * nonChecker.get(2).add(p); } else { PhysicalTimeInterval p =
                 * nonChecker.get(2).get( nonChecker.get(2).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } else { if
                 * (findHead == true) { findHead = false; PhysicalTimeInterval p
                 * = nonChecker.get(2).get( nonChecker.get(2).size() - 1);
                 * p.setpTimeHi(window.get(k).getPhysicalTime()); } } } for (int
                 * m = 0; m < 10; m++) { while (nonChecker.get(0).size() != 0 &&
                 * nonChecker.get(1).size() != 0 && nonChecker.get(2).size() !=
                 * 0 && nonChecker.get(0).get(0).getpTimeHi() != 0 &&
                 * (nonChecker.get(0).get(0).getpTimeHi() < nonChecker
                 * .get(1).get(0).getpTimeLo() || nonChecker
                 * .get(0).get(0).getpTimeHi() < nonChecker
                 * .get(2).get(0).getpTimeLo())) { nonChecker.get(0).remove(0);
                 * } while (nonChecker.get(0).size() != 0 &&
                 * nonChecker.get(1).size() != 0 && nonChecker.get(2).size() !=
                 * 0 && nonChecker.get(1).get(0).getpTimeHi() != 0 &&
                 * (nonChecker.get(1).get(0).getpTimeHi() < nonChecker
                 * .get(0).get(0).getpTimeLo() || nonChecker
                 * .get(1).get(0).getpTimeHi() < nonChecker
                 * .get(2).get(0).getpTimeLo())) { nonChecker.get(1).remove(0);
                 * } while (nonChecker.get(0).size() != 0 &&
                 * nonChecker.get(1).size() != 0 && nonChecker.get(2).size() !=
                 * 0 && nonChecker.get(2).get(0).getpTimeHi() != 0 &&
                 * (nonChecker.get(2).get(0).getpTimeHi() < nonChecker
                 * .get(0).get(0).getpTimeLo() || nonChecker
                 * .get(2).get(0).getpTimeHi() < nonChecker
                 * .get(1).get(0).getpTimeLo())) { nonChecker.get(2).remove(0);
                 * } } if (nonChecker.get(0).size() != 0 &&
                 * nonChecker.get(1).size() != 0 && nonChecker.get(2).size() !=
                 * 0) { if (nonChecker.get(0).get(0).getpTimeHi() == 0 &&
                 * nonChecker.get(1).get(0).getpTimeHi() != 0 &&
                 * nonChecker.get(2).get(0).getpTimeHi() != 0) { if
                 * (nonChecker.get(0).get(0).getpTimeLo() >= nonChecker
                 * .get(1).get(0).getpTimeLo() &&
                 * nonChecker.get(0).get(0).getpTimeLo() <= nonChecker
                 * .get(1).get(0).getpTimeHi() &&
                 * nonChecker.get(0).get(0).getpTimeLo() >= nonChecker
                 * .get(2).get(0).getpTimeLo() &&
                 * nonChecker.get(0).get(0).getpTimeLo() <= nonChecker
                 * .get(2).get(0).getpTimeHi()) { PhysicalNumber++;
                 * outDetection.println("PhysicalNumber: " + PhysicalNumber);
                 * long lo = Math.max(nonChecker.get(0).get(0) .getpTimeLo(),
                 * nonChecker.get(1).get(0) .getpTimeLo()); lo = Math.max(lo,
                 * nonChecker.get(2).get(0) .getpTimeLo()); long hi =
                 * Math.min(nonChecker.get(1).get(0) .getpTimeHi(),
                 * nonChecker.get(2).get(0) .getpTimeHi()); if (hi < lo) { try {
                 * throw (new Exception("SCP error!" + hi + "-" + lo)); } catch
                 * (Exception e) {
                 * e.printStackTrace(); } } outDetection.println("Interval: " +
                 * lo + "-" + hi); outDetection.flush(); } } else if
                 * (nonChecker.get(1).get(0).getpTimeHi() == 0 &&
                 * nonChecker.get(0).get(0).getpTimeHi() != 0 &&
                 * nonChecker.get(2).get(0).getpTimeHi() != 0) { if
                 * (nonChecker.get(1).get(0).getpTimeLo() >= nonChecker
                 * .get(0).get(0).getpTimeLo() &&
                 * nonChecker.get(1).get(0).getpTimeLo() <= nonChecker
                 * .get(0).get(0).getpTimeHi() &&
                 * nonChecker.get(1).get(0).getpTimeLo() >= nonChecker
                 * .get(2).get(0).getpTimeLo() &&
                 * nonChecker.get(1).get(0).getpTimeLo() <= nonChecker
                 * .get(2).get(0).getpTimeHi()) { PhysicalNumber++;
                 * outDetection.write("PhysicalNumber: " + PhysicalNumber +
                 * "\n"); long lo = Math.max(nonChecker.get(0).get(0)
                 * .getpTimeLo(), nonChecker.get(1).get(0) .getpTimeLo()); lo =
                 * Math.max(lo, nonChecker.get(2).get(0) .getpTimeLo()); long hi
                 * = Math.min(nonChecker.get(0).get(0) .getpTimeHi(),
                 * nonChecker.get(2).get(0) .getpTimeHi()); if (hi < lo) { try {
                 * throw (new Exception("SCP error!" + hi + "-" + lo)); } catch
                 * (Exception e) {
                 * e.printStackTrace(); } } outDetection.println("Interval: " +
                 * lo + "-" + hi); outDetection.flush(); } } else if
                 * (nonChecker.get(2).get(0).getpTimeHi() == 0 &&
                 * nonChecker.get(0).get(0).getpTimeHi() != 0 &&
                 * nonChecker.get(1).get(0).getpTimeHi() != 0) { if
                 * (nonChecker.get(2).get(0).getpTimeLo() >= nonChecker
                 * .get(0).get(0).getpTimeLo() &&
                 * nonChecker.get(2).get(0).getpTimeLo() <= nonChecker
                 * .get(0).get(0).getpTimeHi() &&
                 * nonChecker.get(2).get(0).getpTimeLo() >= nonChecker
                 * .get(1).get(0).getpTimeLo() &&
                 * nonChecker.get(2).get(0).getpTimeLo() <= nonChecker
                 * .get(1).get(0).getpTimeHi()) { PhysicalNumber++;
                 * outDetection.write("PhysicalNumber: " + PhysicalNumber +
                 * "\n"); long lo = Math.max(nonChecker.get(0).get(0)
                 * .getpTimeLo(), nonChecker.get(1).get(0) .getpTimeLo()); lo =
                 * Math.max(lo, nonChecker.get(2).get(0) .getpTimeLo()); long hi
                 * = Math.min(nonChecker.get(0).get(0) .getpTimeHi(),
                 * nonChecker.get(1).get(0) .getpTimeHi()); if (hi < lo) { try {
                 * throw (new Exception("SCP error!" + hi + "-" + lo)); } catch
                 * (Exception e) {
                 * e.printStackTrace(); } } outDetection.println("Interval: " +
                 * lo + "-" + hi); outDetection.flush(); } } else if
                 * (nonChecker.get(0).get(0).getpTimeHi() != 0 &&
                 * nonChecker.get(1).get(0).getpTimeHi() != 0 &&
                 * nonChecker.get(2).get(0).getpTimeHi() != 0) {
                 * PhysicalNumber++; outDetection.write("PhysicalNumber: " +
                 * PhysicalNumber + "\n"); long lo =
                 * Math.max(nonChecker.get(0).get(0) .getpTimeLo(),
                 * nonChecker.get(1).get(0) .getpTimeLo()); lo = Math.max(lo,
                 * nonChecker.get(2).get(0) .getpTimeLo()); long hi =
                 * Math.min(nonChecker.get(0).get(0) .getpTimeHi(),
                 * nonChecker.get(1).get(0) .getpTimeHi()); hi = Math.min(hi,
                 * nonChecker.get(2).get(0) .getpTimeHi()); if (hi < lo) { try {
                 * throw (new Exception("SCP error!" + hi + "-" + lo)); } catch
                 * (Exception e) {
                 * e.printStackTrace(); } } outDetection.println("Interval: " +
                 * lo + "-" + hi); outDetection.flush(); }
                 * 
                 * } } else
                 */
                if (children.length > 0) {
                    ArrayList<ArrayList<PhysicalTimeInterval>> nonChecker = new ArrayList<ArrayList<PhysicalTimeInterval>>();
                    for (int j = 0; j < children.length; j++) {
                        nonChecker.add(new ArrayList<PhysicalTimeInterval>());
                    }
                    for (int j = 0; j < children.length; j++) {
                        boolean findHead = false;
                        ArrayList<LocalState> window = windowedLocalStateSet
                                .get(j);
                        for (int k = 0; k < window.size(); k++) {
                            if (window.get(k).getlocalPredicate() == true) {
                                if (findHead == false) {
                                    findHead = true;
                                    PhysicalTimeInterval p = new PhysicalTimeInterval(
                                            window.get(k).getPhysicalTime());
                                    p.setpTimeHi(window.get(k)
                                            .getPhysicalTime());
                                    nonChecker.get(j).add(p);
                                } else {
                                    PhysicalTimeInterval p = nonChecker.get(j)
                                            .get(nonChecker.get(j).size() - 1);
                                    p.setpTimeHi(window.get(k)
                                            .getPhysicalTime());
                                }
                            } else {
                                if (findHead == true) {
                                    findHead = false;
                                    PhysicalTimeInterval p = nonChecker.get(j)
                                            .get(nonChecker.get(j).size() - 1);
                                    p.setpTimeHi(window.get(k)
                                            .getPhysicalTime());
                                }
                            }
                        }
                    }
                    ArrayList<Integer> changed = new ArrayList<Integer>();
                    for (int j = 0; j < children.length; j++) {
                        changed.add(new Integer(j));
                    }
                    while (changed.size() != 0) {
                        ArrayList<Integer> newchanged = new ArrayList<Integer>();
                        for (int m = 0; m < changed.size(); m++) {
                            int elem = changed.get(m).intValue();
                            for (int n = 0; n < children.length; n++) {
                                if (elem == n)
                                    continue;
                                if (nonChecker.get(elem).size() != 0
                                        && nonChecker.get(n).size() != 0) {
                                    PhysicalTimeInterval qm = nonChecker.get(
                                            elem).get(0);
                                    PhysicalTimeInterval qn = nonChecker.get(n)
                                            .get(0);
                                    if (qm.getpTimeHi() != 0
                                            && (qn.getpTimeLo() > qm
                                                    .getpTimeHi())) {
                                        addOnce(newchanged, new Integer(elem));
                                    }
                                    if (qn.getpTimeHi() != 0
                                            && (qm.getpTimeLo() > qn
                                                    .getpTimeHi())) {
                                        addOnce(newchanged, new Integer(n));
                                    }
                                }
                            }
                        }
                        changed = newchanged;
                        for (int j = 0; j < changed.size(); j++) {
                            int elem = changed.get(j).intValue();
                            nonChecker.get(elem).remove(0);
                        }
                    }
                    boolean flg = false;
                    for (int j = 0; j < children.length; j++) {
                        if (nonChecker.get(j).size() == 0) {
                            flg = true;
                            break;
                        }
                    }
                    if (flg == false) {
                        PhysicalNumber++;
                        outDetection.write("PhysicalNumber: " + PhysicalNumber
                                + "\n");
                        outDetection.flush();
                        long lo = 0;
                        long hi = Long.MAX_VALUE - 1;
                        for (int j = 0; j < children.length; j++) {
                            lo = Math.max(lo, nonChecker.get(j).get(0)
                                    .getpTimeLo());
                            hi = Math.min(hi, nonChecker.get(j).get(0)
                                    .getpTimeHi());
                        }
                        if (hi < lo) {
                            try {
                                throw (new Exception("SCP error!" + hi + "-"
                                        + lo));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        /*
         * if (jOptionPane == false) { JOptionPane.showMessageDialog(null,
         * "Detection complete!"); jOptionPane = true; }
         */
    }

    private void addOnce(ArrayList<Integer> list, Integer num) {
        boolean already = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).intValue() == num.intValue()) {
                already = true;
                break;
            }
        }
        if (already == false)
            list.add(num);
    }

    private void growOriLattice(LocalState localState, int id) {
        LocalState[] globalState = new LocalState[children.length];
        String[] index = new String[children.length];
        for (int i = 0; i < children.length; i++) {
            if (i != id) {
                globalState[i] = oriMaxCGS.getGlobalState()[i];
                index[i] = globalState[i].getID();
            }
        }
        globalState[id] = localState;
        index[id] = localState.getID();
        AbstractLatticeIDNode node = createNode(globalState, index);

        // only the first node is CGS, can the lattice grow.
        if (isCGS(node)) {
            ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
            set.add(node);
            while (!set.isEmpty()) {
                AbstractLatticeIDNode newNode = set.remove(0);
                String ind = StringUtils.join(newNode.getID(), ' ');
                if (oriMappedLattice.get(ind) == null) {
                    oriMappedLattice.put(ind, newNode);
                    if (DEBUG) {
                        long time_t = (new Date()).getTime();
                        outOriConstruction.println("[In] " + ind);
                        outOriConstruction.flush();
                        wastedOriTime += (new Date()).getTime() - time_t;
                    }
                    for (int i = 0; i < children.length; i++) {
                        String[] index_2 = new String[children.length];
                        LocalState[] globalState_2 = new LocalState[children.length];
                        for (int j = 0; j < children.length; j++) {
                            if (j != i) {
                                index_2[j] = newNode.getID()[j];
                                globalState_2[j] = newNode.getGlobalState()[j];
                            }
                        }
                        index_2[i] = Integer.toString(Integer.valueOf(newNode
                                .getID()[i]) - 1);
                        if (indexIsOriValid(index_2, id)) {
                            globalState_2[i] = localStateSet.get(i).get(
                                    Integer.valueOf(index_2[i]));
                            String ID = StringUtils.join(index_2, ' ');
                            if (oriMappedLattice.get(ID) == null) {
                                if (isCGS(globalState_2)) {
                                    set.add(createNode(globalState_2, index_2));
                                } else {
                                    outOriConstruction.print("[" + ID + "]: ");
                                    for (int j = 0; j < globalState_2.length; j++) {
                                        outOriConstruction
                                                .print(globalState_2[j].getvc()
                                                        .toString()
                                                        + ",");
                                    }
                                    outOriConstruction.println();
                                    outOriConstruction.println("[" + ID
                                            + "] is not CGS.");
                                    outOriConstruction.println();
                                    outOriConstruction.flush();
                                }
                            }
                        }
                        String[] index_1 = new String[children.length];
                        LocalState[] globalState_1 = new LocalState[children.length];
                        for (int j = 0; j < children.length; j++) {
                            if (j != i) {
                                index_1[j] = newNode.getID()[j];
                                globalState_1[j] = newNode.getGlobalState()[j];
                            }
                        }
                        index_1[i] = Integer.toString(Integer.valueOf(newNode
                                .getID()[i]) + 1);
                        if (indexIsOriValid(index_1, id)) {
                            globalState_1[i] = localStateSet.get(i).get(
                                    Integer.valueOf(index_1[i]));
                            String ID = StringUtils.join(index_1, ' ');
                            if (oriMappedLattice.get(ID) == null) {
                                if (isCGS(globalState_1)) {
                                    set.add(createNode(globalState_1, index_1));
                                } else {
                                    outOriConstruction.print("[" + ID + "]: ");
                                    for (int j = 0; j < globalState_1.length; j++) {
                                        outOriConstruction
                                                .print(globalState_1[j].getvc()
                                                        .toString()
                                                        + ",");
                                    }
                                    outOriConstruction.println();
                                    outOriConstruction.println("[" + ID
                                            + "] is not CGS.");
                                    outOriConstruction.println();
                                    outOriConstruction.flush();
                                }
                            }
                        }
                    }
                    if (subOri(newNode, id).size() == 0) {
                        oriMaxCGS = newNode;
                    }
                }
            }
        } else {
            outOriConstruction.print("[" + StringUtils.join(node.getID(), ' ')
                    + "]: ");
            LocalState[] local = node.getGlobalState();
            for (int i = 0; i < local.length; i++) {
                outOriConstruction.print(local[i].getvc().toString() + ",");
            }
            outOriConstruction.println();
            outOriConstruction.println("["
                    + StringUtils.join(node.getID(), ' ') + "] is not CGS.");
            outOriConstruction.println();
            outOriConstruction.flush();
        }
    }

    private void growLattice(LocalState localState, int id) {
        if (mappedLattice.size() == 0) {
            LocalState[] globalState = new LocalState[children.length];
            String[] index = new String[children.length];
            for (int i = 0; i < children.length; i++) {
                if (i != id) {
                    globalState[i] = windowedLocalStateSet.get(i).get(
                            windowedLocalStateSet.get(i).size() - 1);
                    index[i] = globalState[i].getID();
                }
            }
            globalState[id] = localState;
            index[id] = localState.getID();
            AbstractLatticeIDNode node = createNode(globalState, index);

            ArrayList<String> setID = new ArrayList<String>();
            ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
            set.add(node);
            setID.add(StringUtils.join(index, ' '));

            while (!set.isEmpty()) {
                AbstractLatticeIDNode newNode = set.remove(0);
                if (isCGS(newNode)) {
                    set.clear();
                    setID.clear();
                    grow(newNode, id);
                    break;
                } else {
                    for (int i = 0; i < children.length; i++) {
                        if (i == id) {
                            continue;
                        }
                        index = new String[children.length];
                        globalState = new LocalState[children.length];
                        for (int j = 0; j < children.length; j++) {
                            index[j] = newNode.getID()[j];
                            globalState[j] = newNode.getGlobalState()[j];
                        }
                        index[i] = Integer
                                .toString(Integer.valueOf(index[i]) - 1);
                        if (!setID.contains(StringUtils.join(index, ' '))
                                && indexIsValid(index, id)) {
                            globalState[i] = localStateSet.get(i).get(
                                    Integer.valueOf(index[i]));
                            set.add(createNode(globalState, index));
                            setID.add(StringUtils.join(index, ' '));
                        }
                    }
                }
            }
        } else {
            if (maxCGS.getGlobalState()[id].equals(localStateSet.get(id).get(
                    Integer.valueOf(localState.getID()) - 1))) {
                LocalState[] globalState = new LocalState[children.length];
                String[] index = new String[children.length];
                for (int i = 0; i < children.length; i++) {
                    if (i != id) {
                        globalState[i] = maxCGS.getGlobalState()[i];
                        index[i] = globalState[i].getID();
                    }
                }
                index[id] = localState.getID();
                globalState[id] = localState;
                AbstractLatticeIDNode newNode = createNode(globalState, index);
                if (isCGS(newNode)) {
                    grow(newNode, id);
                }
            }
        }
    }

    private void grow(AbstractLatticeIDNode node, int id) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        set.add(node);
        while (!set.isEmpty()) {
            AbstractLatticeIDNode newNode = set.remove(0);
            String ID = StringUtils.join(newNode.getID(), ' ');
            if (mappedLattice.get(ID) == null) {
                mappedLattice.put(ID, newNode);
                if (DEBUG) {
                    long time_t = (new Date()).getTime();
                    outConstruction.println("[In]" + ID);
                    outConstruction.flush();
                    wastedTime += (new Date()).getTime() - time_t;
                }
                ArrayList<AbstractLatticeIDNode> prec = prec(newNode, id);
                ArrayList<AbstractLatticeIDNode> sub = sub(newNode, id);
                if (prec.size() == 0) {
                    minCGS = newNode;
                }
                if (sub.size() == 0) {
                    maxCGS = newNode;
                }
                for (int i = 0; i < prec.size(); i++) {
                    String ind = StringUtils.join(prec.get(i).getID(), ' ');
                    if (mappedLattice.get(ind) == null) {
                        if (!set.contains(prec.get(i))) {
                            set.add(prec.get(i));
                        }
                    }
                }
                for (int i = 0; i < sub.size(); i++) {
                    String ind = StringUtils.join(sub.get(i).getID(), ' ');
                    if (mappedLattice.get(ind) == null) {
                        if (!set.contains(sub.get(i))) {
                            set.add(sub.get(i));
                        }
                    }
                }
            }
        }
    }

    private void pruneLattice(AbstractLatticeIDNode minCGS, int id) {
        long time = (new Date()).getTime();
        long innerTime = 0;
        if (this.minCGS.getGlobalState()[id].getID().equals(
                this.maxCGS.getGlobalState()[id].getID())) {
            mappedLattice.clear();
            if (DEBUG) {
                long time_t = (new Date()).getTime();
                outConstruction.println("[Out] all CGSs");
                outConstruction.flush();
                innerTime += (new Date()).getTime() - time_t;
                wastedTime += (new Date()).getTime() - time_t;
            }
            this.minCGS = null;
            this.maxCGS = null;
        } else {
            boolean minFlag = false;
            ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
            set.add(minCGS);
            this.minCGS = null;
            while (!set.isEmpty()) {
                AbstractLatticeIDNode node = set.remove(0);
                ArrayList<AbstractLatticeIDNode> sub = subAfterGrown(node, id);
                for (int i = 0; i < sub.size(); i++) {
                    if (sub.get(i).getGlobalState()[id].getID().equals(
                            node.getGlobalState()[id].getID())) {
                        if (!set.contains(sub.get(i))) {
                            set.add(sub.get(i));
                        }
                    } else if (minFlag == false
                            && sub.get(i).getGlobalState()[id].getID().equals(
                                    windowedLocalStateSet.get(id).get(0)
                                            .getID())) {
                        ArrayList<AbstractLatticeIDNode> prec = precAfterGrown(
                                sub.get(i), id);
                        if (prec.size() == 1) {
                            this.minCGS = sub.get(i);
                            minFlag = true;
                        }
                    }
                }
                String ID = StringUtils.join(node.getID(), ' ');
                mappedLattice.remove(ID);
                if (DEBUG) {
                    long time_t = (new Date()).getTime();
                    outConstruction.println("[Out]" + ID);
                    outConstruction.flush();
                    innerTime += (new Date()).getTime() - time_t;
                    wastedTime += (new Date()).getTime() - time_t;
                }
            }
            if (mappedLattice.size() == 0) {
                this.maxCGS = null;
            }

            /*
             * Iterator<String> it = mappedLattice.keySet().iterator();
             * while(it.hasNext()) { String key = it.next();
             * AbstractLatticeIDNode node = mappedLattice.get(key);
             * if(node.getID()[id].equals(minCGS.getID()[id])) { it.remove(); }
             * }
             * 
             * it = mappedLattice.keySet().iterator(); while(it.hasNext()) {
             * String key = it.next(); AbstractLatticeIDNode node =
             * mappedLattice.get(key); if(precAfterGrown(node, id).size() == 0)
             * { this.minCGS = node; break; } } if (mappedLattice.size() == 0) {
             * this.maxCGS = null; }
             */
        }
        pruneTime = (new Date()).getTime() - time - innerTime;
    }

    private boolean indexIsValid(String[] index, int id) {
        for (int i = 0; i < children.length; i++) {
            if (i != id) {
                int min = Integer.valueOf(windowedLocalStateSet.get(i).get(0)
                        .getID());
                int max = Integer.valueOf(windowedLocalStateSet.get(i).get(
                        windowedLocalStateSet.get(i).size() - 1).getID());
                if (Integer.valueOf(index[i]) < min
                        || Integer.valueOf(index[i]) > max) {
                    return false;
                }
            } else {
                // -1 is for the to-be-deleted CGSs
                int min = Integer.valueOf(windowedLocalStateSet.get(i).get(0)
                        .getID()) - 1;
                int max = Integer.valueOf(windowedLocalStateSet.get(i).get(
                        windowedLocalStateSet.get(i).size() - 1).getID());
                if (Integer.valueOf(index[i]) < min
                        || Integer.valueOf(index[i]) > max) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isCGS(LocalState[] CGS) {
        for (int i = 0; i < children.length; i++) {
            for (int j = 0; j < children.length; j++) {
                if (i != j) {
                    if (CGS[i].getvc().lessThan(CGS[j].getvc())
                            || CGS[j].getvc().lessThan(CGS[i].getvc())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isCGS(AbstractLatticeIDNode node) {
        LocalState[] CGS = node.getGlobalState();
        return isCGS(CGS);
    }

    private ArrayList<AbstractLatticeIDNode> prec(AbstractLatticeIDNode node,
            int id) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        for (int i = 0; i < children.length; i++) {
            String[] index = new String[children.length];
            LocalState[] globalState = new LocalState[children.length];
            for (int j = 0; j < children.length; j++) {
                index[j] = node.getID()[j];
                globalState[j] = node.getGlobalState()[j];
            }
            index[i] = Integer.toString(Integer.valueOf(index[i]) - 1);
            if (indexIsValid(index, id)) {
                globalState[i] = localStateSet.get(i).get(
                        Integer.valueOf(index[i]));
                String ID = StringUtils.join(index, ' ');
                if (mappedLattice.get(ID) != null) {
                    set.add(mappedLattice.get(ID));
                } else {
                    if (isCGS(globalState)) {
                        set.add(createNode(globalState, index));
                    }
                }
            }
        }
        return set;
    }

    protected ArrayList<AbstractLatticeIDNode> precAfterGrown(
            AbstractLatticeIDNode node, int id) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        for (int i = 0; i < children.length; i++) {
            String[] index = new String[children.length];
            for (int j = 0; j < children.length; j++) {
                index[j] = node.getID()[j];
            }
            index[i] = Integer.toString(Integer.valueOf(index[i]) - 1);
            if (indexIsValid(index, id)) {
                String ID = StringUtils.join(index, ' ');
                if (mappedLattice.get(ID) != null) {
                    set.add(mappedLattice.get(ID));
                }
            }
        }
        return set;
    }

    protected ArrayList<AbstractLatticeIDNode> sub(AbstractLatticeIDNode node,
            int id) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        for (int i = 0; i < children.length; i++) {
            String[] index = new String[children.length];
            LocalState[] globalState = new LocalState[children.length];
            for (int j = 0; j < children.length; j++) {
                index[j] = node.getID()[j];
                globalState[j] = node.getGlobalState()[j];
            }
            index[i] = Integer.toString(Integer.valueOf(index[i]) + 1);
            if (indexIsValid(index, id)) {
                globalState[i] = localStateSet.get(i).get(
                        Integer.valueOf(index[i]));
                String ID = StringUtils.join(index, ' ');
                if (mappedLattice.get(ID) != null) {
                    set.add(mappedLattice.get(ID));
                } else {
                    if (isCGS(globalState)) {
                        set.add(createNode(globalState, index));
                    }
                }
            }
        }
        return set;
    }

    private ArrayList<AbstractLatticeIDNode> subOri(AbstractLatticeIDNode node,
            int id) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        for (int i = 0; i < children.length; i++) {
            String[] index = new String[children.length];
            for (int j = 0; j < children.length; j++) {
                index[j] = node.getID()[j];
            }
            index[i] = Integer.toString(Integer.valueOf(index[i]) + 1);
            if (indexIsOriValid(index, id)) {
                String ID = StringUtils.join(index, ' ');
                if (oriMappedLattice.get(ID) != null) {
                    set.add(oriMappedLattice.get(ID));
                }
            }
        }
        return set;
    }

    protected ArrayList<AbstractLatticeIDNode> subAfterGrown(
            AbstractLatticeIDNode node, int id) {
        ArrayList<AbstractLatticeIDNode> set = new ArrayList<AbstractLatticeIDNode>();
        for (int i = 0; i < children.length; i++) {
            String[] index = new String[children.length];
            for (int j = 0; j < children.length; j++) {
                if (j != i) {
                    index[j] = node.getID()[j];
                }
            }
            index[i] = Integer.toString(Integer.valueOf(node.getID()[i]) + 1);
            if (indexIsValid(index, id)) {
                String ID = StringUtils.join(index, ' ');
                if (mappedLattice.get(ID) != null) {
                    set.add(mappedLattice.get(ID));
                }
            }
        }
        return set;
    }

    protected AbstractLatticeIDNode getMaxNode() {
        return maxCGS;
    }

    /**
     * @param minNode
     *            the minNode to set
     */
    public void setMinNode(AbstractLatticeIDNode minNode) {
        this.minCGS = minNode;
    }

    /**
     * @return the minNode
     */
    public AbstractLatticeIDNode getMinNode() {
        return minCGS;
    }

    public AbstractLatticeIDNode getStartNode() {
        return startCGS;
    }

    public ArrayList<ArrayList<LocalState>> getLocalStateSet() {
        return localStateSet;
    }

    public void setLocalStateSet(ArrayList<ArrayList<LocalState>> localStateSet) {
        this.localStateSet = localStateSet;
    }

    public void setStartNode(AbstractLatticeIDNode startNode) {
        this.startCGS = startNode;
    }

    public HashMap<String, AbstractLatticeIDNode> getMappedLattice() {
        return mappedLattice;
    }

    public void setMappedLattice(
            HashMap<String, AbstractLatticeIDNode> mappedLattice) {
        this.mappedLattice = mappedLattice;
    }

    private boolean indexIsOriValid(String[] index, int id) {
        for (int i = 0; i < children.length; i++) {
            if (Integer.valueOf(index[i]) > Integer.valueOf(localStateSet
                    .get(i).size()) - 1
                    || Integer.valueOf(index[i]) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param pruneFlag
     *            the pruneFlag to set
     */
    public void setPruneFlag(boolean pruneFlag) {
        this.pruneFlag = pruneFlag;
    }

    /**
     * @return the pruneFlag
     */
    public boolean getPruneFlag() {
        return pruneFlag;
    }

    public abstract AbstractLatticeIDNode createNode(LocalState[] globalState,
            String[] s);

    public abstract boolean check(AbstractLatticeIDNode minCGS,
            AbstractLatticeIDNode maxCGS, int id);

    public abstract void repeatCallBack();

    public abstract boolean checkOriLattice(AbstractLatticeIDNode oriMaxCGS,
            int id);
}
