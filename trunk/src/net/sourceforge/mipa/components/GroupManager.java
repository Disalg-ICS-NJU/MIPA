/* 
 * MIPA - Middleware Infrastructure for Predicate detection in Asynchronous 
 * environments
 * 
 * Copyright (C) 2009-2010 the original author or authors.
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
package net.sourceforge.mipa.components;

import static config.Config.EXPERIMENT;
import static config.Debug.DEBUG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.rm.ResourceManager;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.predicatedetection.CheckerFactory;
import net.sourceforge.mipa.predicatedetection.Composite;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.NodeType;
import net.sourceforge.mipa.predicatedetection.PredicateType;
import net.sourceforge.mipa.predicatedetection.Structure;

class PredicateInfo {
    public String predicateID;

    public ArrayList<String> checkers;

    public ArrayList<String> normalProcesses;

}

/**
 * grouping the local predicate
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class GroupManager {

	private static Logger logger = Logger.getLogger(GroupManager.class);

    // private ContextModeling modeling;

    // private ContextRetrieving retrieving;

    private ResourceManager resourceManager;

    private HashMap<String, PredicateInfo> predicates;

    private Broker broker;

    private Structure specification;

    public GroupManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.broker = null;
        predicates = new HashMap<String, PredicateInfo>();
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public PredicateInfo getPredicateInfo(String predicateID) {
        if (predicates.containsKey(predicateID)) {
            return predicates.get(predicateID);
        }
        return null;
    }

    public void removePredicateInfo(String predicateID) {
        if (predicates.containsKey(predicateID))
            predicates.remove(predicateID);
    }

    public String createGroups(Structure s, PredicateType predicateType,
    		ResultCallback callback) {
        specification = s;

        String predicateID = null;
        IDManager idManager = MIPAResource.getIDManager();
        try {
            predicateID = idManager.getID(Catalog.Predicate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, AbstractGroup> groups = structureGrouping(s, predicateType);
        analyzeDistribution(groups);
        PredicateInfo info = parseGroups(groups, predicateType, callback,
                predicateID);
        if(info == null) {
        	return null;
        }

        info.predicateID = predicateID;
        predicates.put(predicateID, info);

		// add logger for this method
        if (DEBUG) {
            System.out.println("predicates info:");
            logger.info("Predicates info:");
            
            PredicateInfo pInfo = predicates.get(predicateID);
            System.out.println("\tpredicate id: " + pInfo.predicateID);
            System.out.println("\tcheckers:");
            
            logger.info("Predicate id is: " + pInfo.predicateID);
        	logger.info("Checkers:");
            
            for (int i = 0; i < pInfo.checkers.size(); i++) {
                System.out.println("\t\t" + pInfo.checkers.get(i));
                logger.info(pInfo.checkers.get(i));
            }
            System.out.println("\tnormal processes:");
            logger.info("Normal processes:");
            for (int i = 0; i < pInfo.normalProcesses.size(); i++) {
                System.out.println("\t\t" + pInfo.normalProcesses.get(i));
                logger.info(pInfo.normalProcesses.get(i));
            }
        }

        if (EXPERIMENT) {
            Runtime.getRuntime().gc();
        }

        return predicateID;
    }

    private Map<String, AbstractGroup> structureGrouping(Structure s,
            PredicateType predicateType) {
        Map<String, AbstractGroup> groups = new HashMap<String, AbstractGroup>();
        ArrayList<Structure> structure = s.getChildren();
        Structure CGSsNode = structure.get(0);
        Structure GSENode = structure.get(1);
        Structure groupNode;
        if (predicateType.equals(PredicateType.SEQUENCE)
                || predicateType.equals(PredicateType.SURSEQUENCE)
                || predicateType.equals(PredicateType.CTL)
                || predicateType.equals(PredicateType.WINDOWSEQUENCE)
                || predicateType.equals(PredicateType.TCTL)) {
            Structure GSE = new Composite(NodeType.GSE, "GSE");
            Structure CGS = new Composite(NodeType.CGS, "CGS");
            GSE.add(CGS);
            ArrayList<Structure> CGSNode = CGSsNode.getChildren();
            for (int i = 0; i < CGSNode.size(); i++) {
                ArrayList<Structure> LPsNode = CGSNode.get(i).getChildren();
                for (int j = 0; j < LPsNode.size(); j++) {
                    CGS.add(LPsNode.get(j));
                }
            }
            groupNode = GSE;
        } else {
            groupNode = GSENode;
        }
        IDManager idManager = MIPAResource.getIDManager();

        // parse GSE
        String topAbstractGroupId = null;
        try {
            topAbstractGroupId = idManager.getID(Catalog.AbstractGroup);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AbstractGroup topAbstractGroup = new AbstractGroup();
        topAbstractGroup.setGroupId(topAbstractGroupId);
        topAbstractGroup.setLevel(1);

        groups.put(topAbstractGroup.getGroupId(), topAbstractGroup);
        assert (groupNode.getNodeType() == NodeType.GSE);
        ArrayList<Object> absGroupChildren = new ArrayList<Object>();
        topAbstractGroup.setChildren(absGroupChildren);
        ArrayList<Structure> CGSs = groupNode.getChildren();

        // parse CGS
        for (int i = 0; i < CGSs.size(); i++) {
            String subAbstractGroupId = null;
            try {
                subAbstractGroupId = idManager.getID(Catalog.AbstractGroup);
            } catch (Exception e) {
                e.printStackTrace();
            }
            AbstractGroup subAbstractGroup = new AbstractGroup();
            subAbstractGroup.setGroupId(subAbstractGroupId);
            subAbstractGroup.setFather(topAbstractGroupId);
            subAbstractGroup.setLevel(0);
            absGroupChildren.add(subAbstractGroupId);
            groups.put(subAbstractGroup.getGroupId(), subAbstractGroup);

            ArrayList<Object> localPredicates = new ArrayList<Object>();
            subAbstractGroup.setChildren(localPredicates);
            // parse LP
            ArrayList<Structure> LPs = CGSs.get(i).getChildren();
            for (int j = 0; j < LPs.size(); j++) {

                assert (LPs.get(j) instanceof LocalPredicate);
                localPredicates.add(LPs.get(j));
            }
        }

        if (DEBUG) {
            System.out.println("Groups inforamtion: ");
            logger.info("Groups inforamtion: ");
            for (String str : groups.keySet()) {
                AbstractGroup g = groups.get(str);
                System.out.println("\t" + str + " : " + g.getLevel() + " , "
                        + g.getFather());
                logger.info(str + " : " + g.getLevel() + " , "
                        + g.getFather());
            }
        }
        return groups;
    }

    private void analyzeDistribution(Map<String, AbstractGroup> groups) {

    }

    private PredicateInfo parseGroups(Map<String, AbstractGroup> groups,
            PredicateType type, ResultCallback callback, String predicateID) {

        IDManager idManager = MIPAResource.getIDManager();

        Map<String, String> groupToChecker = new HashMap<String, String>();
        Map<LocalPredicate, String> localPredicateToNormalProcess = new HashMap<LocalPredicate, String>();

        // get maximum level
        int maxLevel = 0;
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() > maxLevel)
                maxLevel = g.getLevel();
        }

        // map relevant information
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            String groupId = g.getGroupId();
            if (groupToChecker.containsKey(groupId) == false) {
                try {
                    String checker = idManager.getID(Catalog.Checker);
                    groupToChecker.put(groupId, checker);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String father = g.getFather();
            if (father != null && groupToChecker.containsKey(father) == false) {
                try {
                    String checker = idManager.getID(Catalog.Checker);
                    groupToChecker.put(father, checker);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ArrayList<Object> children = g.getChildren();
            if (g.getLevel() > 0) {
                for (int i = 0; i < children.size(); i++) {
                    String child = (String) children.get(i);
                    if (groupToChecker.containsKey(child) == false) {
                        try {
                            String checker = idManager.getID(Catalog.Checker);
                            groupToChecker.put(child, checker);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                for (int i = 0; i < children.size(); i++) {
                    LocalPredicate child = (LocalPredicate) children.get(i);
                    if (localPredicateToNormalProcess.containsKey(child) == false) {
                        try {
                            String normalProcess = idManager
                                    .getID(Catalog.NormalProcess);
                            localPredicateToNormalProcess.put(child,
                                    normalProcess);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (DEBUG) {
            System.out.println("Debugging message: ");
            System.out.println("\tGroup To Checker: ");
            logger.info("Group To Checker: ");
            for (String s : groupToChecker.keySet()) {
                System.out.println("\t\t" + s + " : " + groupToChecker.get(s));
                logger.info(s + " : " + groupToChecker.get(s));
            }

            System.out.println("\t local predicate: ");
            logger.info("Local predicate: ");
            for (LocalPredicate lp : localPredicateToNormalProcess.keySet()) {
                // System.out.println("\t\t" + lp.getName() + " : " +
                // localPredicateToNormalProcess.get(lp));
                System.out.println("\t\t" + lp.toString() + " : "
                        + localPredicateToNormalProcess.get(lp));
                logger.info(lp.toString() + " : "
                        + localPredicateToNormalProcess.get(lp));
            }
        }

        Structure node = specification.getChildren().get(0);
        for (int i = 0; i < node.getChildren().size(); i++) {
            Structure CGS = node.getChildren().get(i);
            for (int j = 0; j < CGS.getChildren().size(); j++) {
                Structure LP = CGS.getChildren().get(j);
                assert (LP instanceof LocalPredicate);
                String normalProcess = localPredicateToNormalProcess
                        .get(((LocalPredicate) LP));
                ((LocalPredicate) LP).setNormalProcess(normalProcess);
            }
        }
        boolean flag = true;

        switch (type) {
        case OGA:
            allocateAsOGA(groups, groupToChecker,
                    localPredicateToNormalProcess, callback, predicateID,
                    maxLevel);
            break;
        case SCP:
        	flag = allocateAsSCP(groups, groupToChecker,
                    localPredicateToNormalProcess, callback, predicateID,
                    maxLevel);
            break;
        case WCP:
            allocateAsWCP(groups, groupToChecker,
                    localPredicateToNormalProcess, callback, predicateID,
                    maxLevel);
            break;
        case CADA:
            allocateAsCADA(groups, groupToChecker,
                    localPredicateToNormalProcess, callback, predicateID,
                    maxLevel);
            break;
        case SEQUENCE:
            allocateAsSequence(groups, groupToChecker,
                    localPredicateToNormalProcess, callback, predicateID,
                    maxLevel);
            break;
        case SURSEQUENCE:
            allocateAsSurfaceSequence(groups, groupToChecker,
                    localPredicateToNormalProcess, callback, predicateID,
                    maxLevel);
            break;
        case CTL:
        	allocateAsCTL(groups,groupToChecker,
        			localPredicateToNormalProcess, callback, predicateID,
        			maxLevel);
        	break;
        case TCTL:
        	allocateAsTCTL(groups,groupToChecker,
        			localPredicateToNormalProcess, callback, predicateID,
        			maxLevel);
        	break;
        case WINDOWSEQUENCE:
            allocateAsWindowSequence(groups, groupToChecker,
                    localPredicateToNormalProcess, callback, predicateID,
                    maxLevel);
            break;
        default:
            System.out
                    .println("This predicate type have not been implemented yet.");
        }
        
        if(flag == false) {
        	return null;
        }
        
        PredicateInfo info = new PredicateInfo();
        ArrayList<String> checkers = new ArrayList<String>();
        ArrayList<String> normalProcesses = new ArrayList<String>();
        for (String s : groupToChecker.keySet()) {
            checkers.add(groupToChecker.get(s));
        }
        for (LocalPredicate lp : localPredicateToNormalProcess.keySet()) {
            normalProcesses.add(localPredicateToNormalProcess.get(lp));
        }
        info.checkers = checkers;
        info.normalProcesses = normalProcesses;
        return info;
    }

    private void allocateAsTCTL(Map<String, AbstractGroup> groups,
			Map<String, String> groupToChecker,
			Map<LocalPredicate, String> localPredicateToNormalProcess,
			ResultCallback callback, String predicateID, int maxLevel) {
		// TODO Auto-generated method stub
    	IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        // start checker for level > 0
        for (int i = maxLevel; i > 0; i--) {

        }

        // check the level == 0
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String gid = g.getGroupId();

                // current Implementation is that father of sequence checker is null;

                ArrayList<Object> children = g.getChildren();
                ArrayList<String> owners = new ArrayList<String>();
                ArrayList<String> members = new ArrayList<String>();
                owners.add(groupToChecker.get(gid));
                for (int i = 0; i < children.size(); i++) {
                    members.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                // create group
                String groupId = null;
                try {
                    groupId = idManager.getID(Catalog.Group);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Group ng = new Group(groupId, owners, members,
                        PredicateType.TCTL);
                ng.setCoordinatorID(groupId);

                try {
                    coordinator.newCoordinator(ng);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String checkerName = ng.getOwners().get(0);
                String[] normalProcesses = new String[ng.getMembers().size()];
                ng.getMembers().toArray(normalProcesses);
                CheckerFactory.createTCTLChecker(callback, predicateID, checkerName,
                        normalProcesses, specification);

                // create Normal Processes.
                for (int i = 0; i < children.size(); i++) {
                    broker.registerLocalPredicate((LocalPredicate) children
                            .get(i), localPredicateToNormalProcess.get(children
                            .get(i)), ng);
                }
            }
        }
	}

	private void allocateAsWindowSequence(Map<String, AbstractGroup> groups,
            Map<String, String> groupToChecker,
            Map<LocalPredicate, String> localPredicateToNormalProcess,
            ResultCallback callback, String predicateID, int maxLevel) {
        // TODO Auto-generated method stub
        IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        // start checker for level > 0
        for (int i = maxLevel; i > 0; i--) {

        }

        // check the level == 0
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String gid = g.getGroupId();

                // current Implementation is that father of sequence checker is
                // null;

                ArrayList<Object> children = g.getChildren();
                ArrayList<String> owners = new ArrayList<String>();
                ArrayList<String> members = new ArrayList<String>();
                owners.add(groupToChecker.get(gid));
                for (int i = 0; i < children.size(); i++) {
                    members.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                // create group
                String groupId = null;
                try {
                    groupId = idManager.getID(Catalog.Group);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Group ng = new Group(groupId, owners, members,
                        PredicateType.WINDOWSEQUENCE);
                ng.setCoordinatorID(groupId);

                try {
                    coordinator.newCoordinator(ng);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String checkerName = ng.getOwners().get(0);
                String[] normalProcesses = new String[ng.getMembers().size()];
                ng.getMembers().toArray(normalProcesses);
                CheckerFactory.createSequenceWindowChecker(callback,
                        predicateID, checkerName, normalProcesses,
                        specification);

                // create Normal Processes.
                for (int i = 0; i < children.size(); i++) {
                    broker.registerLocalPredicate((LocalPredicate) children
                            .get(i), localPredicateToNormalProcess.get(children
                            .get(i)), ng);
                }
            }
        }
    }
    
    private void allocateAsCTL(Map<String, AbstractGroup> groups,
			Map<String, String> groupToChecker,
			Map<LocalPredicate, String> localPredicateToNormalProcess,
			ResultCallback callback, String predicateID, int maxLevel)
	{
		// TODO need be verified ?
    	
    	IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        // start checker for level > 0
        for (int i = maxLevel; i > 0; i--) {

        }

        // check the level == 0
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String gid = g.getGroupId();

                // current Implementation is that father of sequence checker is null;

                ArrayList<Object> children = g.getChildren();
                ArrayList<String> owners = new ArrayList<String>();
                ArrayList<String> members = new ArrayList<String>();
                owners.add(groupToChecker.get(gid));
                for (int i = 0; i < children.size(); i++) {
                    members.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                // create group
                String groupId = null;
                try {
                    groupId = idManager.getID(Catalog.Group);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Group ng = new Group(groupId, owners, members,
                        PredicateType.CTL);
                ng.setCoordinatorID(groupId);

                try {
                    coordinator.newCoordinator(ng);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String checkerName = ng.getOwners().get(0);
                String[] normalProcesses = new String[ng.getMembers().size()];
                ng.getMembers().toArray(normalProcesses);
                CheckerFactory.createCTLChecker(callback, predicateID, checkerName,
                        normalProcesses, specification);

                // create Normal Processes.
                for (int i = 0; i < children.size(); i++) {
                    broker.registerLocalPredicate((LocalPredicate) children
                            .get(i), localPredicateToNormalProcess.get(children
                            .get(i)), ng);
                }
            }
        }
	}

    private void allocateAsSurfaceSequence(Map<String, AbstractGroup> groups,
            Map<String, String> groupToChecker,
            Map<LocalPredicate, String> localPredicateToNormalProcess,
            ResultCallback callback, String predicateID, int maxLevel) {
        // TODO Auto-generated method stub
        IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        // start checker for level > 0
        for (int i = maxLevel; i > 0; i--) {

        }

        // check the level == 0
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String gid = g.getGroupId();

                // current Implementation is that father of sequence checker is
                // null;

                ArrayList<Object> children = g.getChildren();
                ArrayList<String> owners = new ArrayList<String>();
                ArrayList<String> members = new ArrayList<String>();
                owners.add(groupToChecker.get(gid));
                for (int i = 0; i < children.size(); i++) {
                    members.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                // create group
                String groupId = null;
                try {
                    groupId = idManager.getID(Catalog.Group);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Group ng = new Group(groupId, owners, members,
                        PredicateType.SURSEQUENCE);
                ng.setCoordinatorID(groupId);

                try {
                    coordinator.newCoordinator(ng);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String checkerName = ng.getOwners().get(0);
                String[] normalProcesses = new String[ng.getMembers().size()];
                ng.getMembers().toArray(normalProcesses);
                CheckerFactory.createSequenceSurfaceChecker(callback,
                        predicateID, checkerName, normalProcesses,
                        specification);

                // create Normal Processes.
                for (int i = 0; i < children.size(); i++) {
                    broker.registerLocalPredicate((LocalPredicate) children
                            .get(i), localPredicateToNormalProcess.get(children
                            .get(i)), ng);
                }
            }
        }
    }

    private void allocateAsSequence(Map<String, AbstractGroup> groups,
            Map<String, String> groupToChecker,
            Map<LocalPredicate, String> localPredicateToNormalProcess,
            ResultCallback callback, String predicateID, int maxLevel) {
        // TODO Auto-generated method stub
        IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        // start checker for level > 0
        for (int i = maxLevel; i > 0; i--) {

        }

        // check the level == 0
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String gid = g.getGroupId();

                // current Implementation is that father of sequence checker is
                // null;

                ArrayList<Object> children = g.getChildren();
                ArrayList<String> owners = new ArrayList<String>();
                ArrayList<String> members = new ArrayList<String>();
                owners.add(groupToChecker.get(gid));
                for (int i = 0; i < children.size(); i++) {
                    members.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                // create group
                String groupId = null;
                try {
                    groupId = idManager.getID(Catalog.Group);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Group ng = new Group(groupId, owners, members,
                        PredicateType.SEQUENCE);
                ng.setCoordinatorID(groupId);

                try {
                    coordinator.newCoordinator(ng);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String checkerName = ng.getOwners().get(0);
                String[] normalProcesses = new String[ng.getMembers().size()];
                ng.getMembers().toArray(normalProcesses);
                CheckerFactory.createSequenceChecker(callback, predicateID,
                        checkerName, normalProcesses, specification);

                // create Normal Processes.
                for (int i = 0; i < children.size(); i++) {
                    broker.registerLocalPredicate((LocalPredicate) children
                            .get(i), localPredicateToNormalProcess.get(children
                            .get(i)), ng);
                }
            }
        }
    }

    private void allocateAsOGA(Map<String, AbstractGroup> groups,
            Map<String, String> groupToChecker,
            Map<LocalPredicate, String> localPredicateToNormalProcess,
            ResultCallback callback, String predicateID, int maxLevel) {

        IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        // start top checker
        for (int i = maxLevel; i > 0; i--) {
            for (String s : groups.keySet()) {
                AbstractGroup g = groups.get(s);
                if (g.getLevel() == i) {
                    String father = g.getFather();
                    String[] fatherArray = null;
                    if (father != null) {
                        ArrayList<String> fatherList = new ArrayList<String>();
                        fatherList.add(groupToChecker.get(father));
                        fatherArray = new String[fatherList.size()];
                        fatherList.toArray(fatherArray);
                    }

                    ArrayList<String> childrenList = new ArrayList<String>();
                    ArrayList<Object> children = g.getChildren();
                    for (int j = 0; j < children.size(); j++) {
                        childrenList.add(groupToChecker.get(children.get(j)));
                    }
                    String[] childrenArray = new String[childrenList.size()];
                    childrenList.toArray(childrenArray);
                    CheckerFactory.createOGAChecker(callback, predicateID,
                            groupToChecker.get(g.getGroupId()), fatherArray,
                            childrenArray, 0);
                }
            }
        }
        // start sub checker, **notice** level = 0
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String father = g.getFather();
                assert (father != null);
                ArrayList<String> fatherList = new ArrayList<String>();
                fatherList.add(groupToChecker.get(father));
                String[] fatherArray = new String[fatherList.size()];
                fatherList.toArray(fatherArray);

                ArrayList<String> childrenList = new ArrayList<String>();
                ArrayList<Object> children = g.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    childrenList.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                String[] childrenArray = new String[childrenList.size()];
                childrenList.toArray(childrenArray);
                CheckerFactory.createOGAChecker(null, predicateID,
                        groupToChecker.get(g.getGroupId()), fatherArray,
                        childrenArray, 1);
            }
        }

        ArrayList<String> normalProcesses = new ArrayList<String>();
        for (LocalPredicate lp : localPredicateToNormalProcess.keySet()) {
            normalProcesses.add(localPredicateToNormalProcess.get(lp));
        }
        // set coordinator
        String groupId = null;
        try {
            groupId = idManager.getID(Catalog.Group);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Group coordinatorGroup = new Group(groupId, null, normalProcesses,
                PredicateType.OGA);

        coordinatorGroup.setCoordinatorID(groupId);
        try {
            coordinator.newCoordinator(coordinatorGroup);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // start normal process.

        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String gid = g.getGroupId();
                String normalProcessGroup = null;
                try {
                    normalProcessGroup = idManager.getID(Catalog.Group);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("sub group id: " + normalProcessGroup);
                logger.info("sub group id: " + normalProcessGroup);
                ArrayList<String> fatherList = new ArrayList<String>();
                fatherList.add(groupToChecker.get(gid));

                Group ng = new Group(normalProcessGroup, fatherList,
                        normalProcesses, PredicateType.OGA);

                ng.setCoordinatorID(groupId);
                ArrayList<String> members = new ArrayList<String>();
                ArrayList<Object> children = g.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    members.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                ng.setSubMembers(members);

                for (int i = 0; i < children.size(); i++) {
                    broker.registerLocalPredicate((LocalPredicate) children
                            .get(i), localPredicateToNormalProcess.get(children
                            .get(i)), ng);

                }
            }
        }
    }

    public void parseOGAStructure(Structure s, ResultCallback callback,
            String predicateID) {
        ArrayList<Structure> children = s.getChildren();

        assert (children != null);

        IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        ArrayList<String> topCheckers = new ArrayList<String>();
        ArrayList<String> subCheckers = new ArrayList<String>();
        ArrayList<String> normalProcesses = new ArrayList<String>();
        Map<String, ArrayList<String>> subGroups = new HashMap<String, ArrayList<String>>();
        Map<String, LocalPredicate> normalProcessToLocalPredicate = new HashMap<String, LocalPredicate>();

        String topChecker = null;
        try {
            topChecker = idManager.getID(Catalog.Checker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        topCheckers.add(topChecker);

        assert (s.getNodeType() == NodeType.GSE);
        for (int i = 0; i < children.size(); i++) {
            // parse CGS
            String subChecker = null;
            try {
                subChecker = idManager.getID(Catalog.Checker);
            } catch (Exception e) {
                e.printStackTrace();
            }
            subCheckers.add(subChecker);
            ArrayList<String> members = new ArrayList<String>();
            subGroups.put(subChecker, members);

            ArrayList<Structure> LPs = children.get(i).getChildren();

            for (int j = 0; j < LPs.size(); j++) {
                Structure unit = LPs.get(j);
                assert (unit.getNodeType() == NodeType.LP);
                String memberID = null;
                try {
                    memberID = idManager.getID(Catalog.NormalProcess);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                members.add(memberID);
                normalProcesses.add(memberID);
                normalProcessToLocalPredicate.put(memberID,
                        (LocalPredicate) unit);
            }
        }
        String groupID = null;
        try {
            groupID = idManager.getID(Catalog.Group);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Coordinator does not care about owners in group. We set it to null
        // now.
        Group coordinatorGroup = new Group(groupID, null, normalProcesses,
                PredicateType.OGA);
        coordinatorGroup.setCoordinatorID(groupID);
        try {
            coordinator.newCoordinator(coordinatorGroup);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DEBUG) {
            System.out.println("Top Checker:");
            logger.info("Top Checker:");
            for (int i = 0; i < topCheckers.size(); i++) {
                System.out.println("\t" + topCheckers.get(i));
                logger.info(topCheckers.get(i));
            }

            System.out.println("Sub Checkers:");
            logger.info("Sub Checkers:");
            for (int i = 0; i < subCheckers.size(); i++) {
                System.out.println("\t" + subCheckers.get(i));
                logger.info(subCheckers.get(i));
            }

            System.out.println("Normal Processes:");
            logger.info("Normal Processes:");
            for (int i = 0; i < normalProcesses.size(); i++) {
                System.out.println("\t" + normalProcesses.get(i));
                logger.info(normalProcesses.get(i));
            }

            System.out.println("Mapping:");
            logger.info("Mapping:");
            for (int i = 0; i < subCheckers.size(); i++) {
                System.out.println("\t" + subCheckers.get(i));
                logger.info(subCheckers.get(i));
                ArrayList<String> m = subGroups.get(subCheckers.get(i));
                for (int j = 0; j < m.size(); j++) {
                    System.out.println("\t\t" + m.get(j));
                    logger.info(m.get(j));
                }
            }
        }

        // create top checker in OGA.
        String[] topCheckersArray = new String[topCheckers.size()];
        topCheckers.toArray(topCheckersArray);
        String[] subCheckersArray = new String[subCheckers.size()];
        subCheckers.toArray(subCheckersArray);
        CheckerFactory.createOGAChecker(callback, predicateID, topCheckers
                .get(0), null, subCheckersArray, 0);

        // create sub checkers in OGA.
        for (int i = 0; i < subCheckers.size(); i++) {
            String subCheckerName = subCheckers.get(i);
            ArrayList<String> subMembers = subGroups.get(subCheckerName);
            String[] subMembersArray = new String[subMembers.size()];
            subMembers.toArray(subMembersArray);
            CheckerFactory.createOGAChecker(null, predicateID, subCheckerName,
                    topCheckersArray, subMembersArray, 1);
        }

        // create Normal Processes in OGA.
        for (int i = 0; i < subCheckers.size(); i++) {
            String subGroupID = null;
            try {
                subGroupID = idManager.getID(Catalog.Group);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ArrayList<String> fathers = new ArrayList<String>();
            fathers.add(subCheckers.get(i));

            Group subGroup = new Group(subGroupID, fathers, normalProcesses,
                    PredicateType.OGA);
            subGroup.setCoordinatorID(groupID);
            ArrayList<String> subMembers = subGroups.get(subCheckers.get(i));
            subGroup.setSubMembers(subMembers);

            for (int j = 0; j < subMembers.size(); j++) {
                LocalPredicate lp = normalProcessToLocalPredicate
                        .get(subMembers.get(j));
                broker.registerLocalPredicate(lp, subMembers.get(j), subGroup);
            }
        }
    }

    private boolean allocateAsSCP(Map<String, AbstractGroup> groups,
            Map<String, String> groupToChecker,
            Map<LocalPredicate, String> localPredicateToNormalProcess,
            ResultCallback callback, String predicateID, int maxLevel) {

        IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        // start checker for level > 0
        for (int i = maxLevel; i > 0; i--) {

        }

        // check the level == 0
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String gid = g.getGroupId();

                // current Implementation is that father of SCP checker is null;

                ArrayList<Object> children = g.getChildren();
                ArrayList<String> owners = new ArrayList<String>();
                ArrayList<String> members = new ArrayList<String>();
                owners.add(groupToChecker.get(gid));
                for (int i = 0; i < children.size(); i++) {
                    members.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                // create group
                String groupId = null;
                try {
                    groupId = idManager.getID(Catalog.Group);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Group ng = new Group(groupId, owners, members,
                        PredicateType.SCP);
                ng.setCoordinatorID(groupId);

                try {
                    coordinator.newCoordinator(ng);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String checkerName = ng.getOwners().get(0);
                String[] normalProcesses = new String[ng.getMembers().size()];
                ng.getMembers().toArray(normalProcesses);
                CheckerFactory.createSCPChecker(callback, predicateID,
                        checkerName, normalProcesses);

                // create Normal Processes.
                for (int i = 0; i < children.size(); i++) {
                    boolean flag = broker.registerLocalPredicate((LocalPredicate) children
                            .get(i), localPredicateToNormalProcess.get(children
                            .get(i)), ng);
                    if(flag == false) {
                    	System.out.println("Cannot find the corresponding sensors!");
                    	return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 
     * @param s
     */
    public void parseSCPStructure(Structure s, ResultCallback callback,
            String predicateID) {
        ArrayList<Structure> children = s.getChildren();

        if (s.getNodeType() == NodeType.GSE) {
            for (int i = 0; i < children.size(); i++) {
                parseSCPStructure(children.get(i), callback, predicateID);
            }
        } else if (s.getNodeType() == NodeType.CGS) {
            IDManager idManager = MIPAResource.getIDManager();
            String owner = null;
            try {
                owner = idManager.getID(Catalog.Checker);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<String> owners = new ArrayList<String>();
            ArrayList<String> members = new ArrayList<String>();
            Map<String, LocalPredicate> mapping = new HashMap<String, LocalPredicate>();

            owners.add(owner);
            for (int i = 0; i < children.size(); i++) {
                Structure unit = children.get(i);

                assert (unit.getNodeType() == NodeType.LP);
                String memberID = null;
                try {
                    memberID = idManager.getID(Catalog.NormalProcess);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                members.add(memberID);
                mapping.put(memberID, (LocalPredicate) unit);
            }
            String groupID = null;
            try {
                groupID = idManager.getID(Catalog.Group);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // create group for CGS
            Group g = new Group(groupID, owners, members, PredicateType.SCP);
            g.setCoordinatorID(groupID);

            Coordinator coordinator = MIPAResource.getCoordinator();
            try {
                coordinator.newCoordinator(g);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // create CGS checker.
            String checkerName = g.getOwners().get(0);
            String[] normalProcesses = new String[g.getMembers().size()];
            g.getMembers().toArray(normalProcesses);
            CheckerFactory.createSCPChecker(callback, predicateID, checkerName,
                    normalProcesses);

            // create Normal Processes.
            for (int i = 0; i < members.size(); i++) {
                LocalPredicate lp = mapping.get(members.get(i));
                broker.registerLocalPredicate(lp, members.get(i), g);
            }
        }
    }

    private void allocateAsCADA(Map<String, AbstractGroup> groups,
            Map<String, String> groupToChecker,
            Map<LocalPredicate, String> localPredicateToNormalProcess,
            ResultCallback callback, String predicateID, int maxLevel) {

        IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        // start checker for level > 0
        for (int i = maxLevel; i > 0; i--) {

        }

        // check the level == 0
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String gid = g.getGroupId();

                // current Implementation is that father of CADA checker is
                // null;

                ArrayList<Object> children = g.getChildren();
                ArrayList<String> owners = new ArrayList<String>();
                ArrayList<String> members = new ArrayList<String>();
                owners.add(groupToChecker.get(gid));
                for (int i = 0; i < children.size(); i++) {
                    members.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                // create group
                String groupId = null;
                try {
                    groupId = idManager.getID(Catalog.Group);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Group ng = new Group(groupId, owners, members,
                        PredicateType.CADA);
                ng.setCoordinatorID(groupId);

                try {
                    coordinator.newCoordinator(ng);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String checkerName = ng.getOwners().get(0);
                String[] normalProcesses = new String[ng.getMembers().size()];
                ng.getMembers().toArray(normalProcesses);
                CheckerFactory.createCADAChecker(callback, predicateID,
                        checkerName, normalProcesses);

                // create Normal Processes.
                for (int i = 0; i < children.size(); i++) {
                    broker.registerLocalPredicate((LocalPredicate) children
                            .get(i), localPredicateToNormalProcess.get(children
                            .get(i)), ng);
                }
            }
        }
    }

    private void allocateAsWCP(Map<String, AbstractGroup> groups,
            Map<String, String> groupToChecker,
            Map<LocalPredicate, String> localPredicateToNormalProcess,
            ResultCallback callback, String predicateID, int maxLevel) {
        IDManager idManager = MIPAResource.getIDManager();
        Coordinator coordinator = MIPAResource.getCoordinator();

        // start checker for level > 0
        for (int i = maxLevel; i > 0; i--) {

        }

        // check the level == 0
        for (String s : groups.keySet()) {
            AbstractGroup g = groups.get(s);
            if (g.getLevel() == 0) {
                String gid = g.getGroupId();

                // current Implementation is that father of WCP checker is null;

                ArrayList<Object> children = g.getChildren();
                ArrayList<String> owners = new ArrayList<String>();
                ArrayList<String> members = new ArrayList<String>();
                owners.add(groupToChecker.get(gid));
                for (int i = 0; i < children.size(); i++) {
                    members.add(localPredicateToNormalProcess.get(children
                            .get(i)));
                }
                // create group
                String groupId = null;
                try {
                    groupId = idManager.getID(Catalog.Group);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Group ng = new Group(groupId, owners, members,
                        PredicateType.WCP);
                ng.setCoordinatorID(groupId);

                try {
                    coordinator.newCoordinator(ng);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String checkerName = ng.getOwners().get(0);
                String[] normalProcesses = new String[ng.getMembers().size()];
                ng.getMembers().toArray(normalProcesses);
                CheckerFactory.createWCPChecker(callback, predicateID,
                        checkerName, normalProcesses);

                // create Normal Processes.
                for (int i = 0; i < children.size(); i++) {
                    broker.registerLocalPredicate((LocalPredicate) children
                            .get(i), localPredicateToNormalProcess.get(children
                            .get(i)), ng);
                }
            }
        }
    }

    /**
     * 
     * @param s
     * @param callback
     */
    public void parseWCPStructure(Structure s, ResultCallback callback,
            String predicateID) {
        ArrayList<Structure> children = s.getChildren();

        if (s.getNodeType() == NodeType.GSE) {
            for (int i = 0; i < children.size(); i++) {
                parseWCPStructure(children.get(i), callback, predicateID);
            }
        } else if (s.getNodeType() == NodeType.CGS) {
            IDManager idManager = MIPAResource.getIDManager();
            String owner = null;
            try {
                owner = idManager.getID(Catalog.Checker);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<String> owners = new ArrayList<String>();
            ArrayList<String> members = new ArrayList<String>();
            Map<String, LocalPredicate> mapping = new HashMap<String, LocalPredicate>();

            owners.add(owner);
            for (int i = 0; i < children.size(); i++) {
                Structure unit = children.get(i);

                assert (unit.getNodeType() == NodeType.LP);
                String memberID = null;
                try {
                    memberID = idManager.getID(Catalog.NormalProcess);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                members.add(memberID);
                mapping.put(memberID, (LocalPredicate) unit);
            }
            String groupID = null;
            try {
                groupID = idManager.getID(Catalog.Group);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // create group for CGS
            Group g = new Group(groupID, owners, members, PredicateType.WCP);
            g.setCoordinatorID(groupID);

            Coordinator coordinator = MIPAResource.getCoordinator();
            try {
                coordinator.newCoordinator(g);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // create CGS checker.
            String checkerName = g.getOwners().get(0);
            String[] normalProcesses = new String[g.getMembers().size()];
            g.getMembers().toArray(normalProcesses);
            CheckerFactory.createWCPChecker(callback, predicateID, checkerName,
                    normalProcesses);

            // create Normal Processes.
            for (int i = 0; i < members.size(); i++) {
                LocalPredicate lp = mapping.get(members.get(i));
                broker.registerLocalPredicate(lp, members.get(i), g);
            }
        }
    }

    /**
     * 
     * @param fatherID
     * @param s
     */

    @SuppressWarnings("unused")
    private void parseOGAPStructure(String fatherID, Structure s) {

        ArrayList<Structure> children = s.getChildren();

        assert (children != null);

        IDManager idManager = MIPAResource.getIDManager();
        // get id
        String id = null;
        try {
            id = idManager.getID(Catalog.Checker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (s.getNodeType() == NodeType.GSE) {
            assert (fatherID == null);

            // create GSE checker, and bind to rmi registry

            // parser the others
            for (int i = 0; i < children.size(); i++) {
                Structure unit = children.get(i);
                parseOGAPStructure(id, unit);
            }
        } else if (s.getNodeType() == NodeType.CGS) {
            assert (fatherID != null);

            // create CGS checker, pass father ID to it

            // create group
            ArrayList<String> members = new ArrayList<String>();
            ArrayList<String> owners = new ArrayList<String>();
            Map<String, LocalPredicate> mapping = new HashMap<String, LocalPredicate>();

            owners.add(id);
            for (int i = 0; i < children.size(); i++) {
                Structure unit = children.get(i);

                assert (unit.getNodeType() == NodeType.LP);
                String memberID = null;
                try {
                    memberID = idManager.getID(Catalog.NormalProcess);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                members.add(memberID);
                mapping.put(memberID, (LocalPredicate) unit);

            }
            String groupID = null;
            try {
                groupID = idManager.getID(Catalog.Group);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Group g = new Group(groupID, owners, members, PredicateType.OGA);
            // register group to coordinator.

            // register each local predicate.

            for (int i = 0; i < members.size(); i++) {
                LocalPredicate LP = mapping.get(members.get(i));
                // FIND THE ECA MANAGER ID. directly pass group

            }

        } else {
            assert (false);
        }
    }
}
