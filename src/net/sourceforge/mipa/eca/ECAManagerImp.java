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
package net.sourceforge.mipa.eca;

import static config.Config.EXPERIMENT;
import static config.Debug.DEBUG;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import net.sourceforge.mipa.components.BrokerInterface;
import net.sourceforge.mipa.components.CheckMode;
import net.sourceforge.mipa.components.Coordinator;
import net.sourceforge.mipa.components.Group;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.predicatedetection.Atom;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.NormalProcess;
import net.sourceforge.mipa.predicatedetection.lattice.scp.SCPLatticeNormalProcess;
import net.sourceforge.mipa.predicatedetection.lattice.sequence.SequenceLatticeNormalProcess;
import net.sourceforge.mipa.predicatedetection.lattice.tctl.TCTLLatticeNormalProcess;
import net.sourceforge.mipa.predicatedetection.lattice.wcp.WCPLatticeNormalProcess;
import net.sourceforge.mipa.predicatedetection.normal.cada.CADANormalProcess;
import net.sourceforge.mipa.predicatedetection.normal.oga.OGANormalProcess;
import net.sourceforge.mipa.predicatedetection.normal.scp.SCPNormalProcess;
import net.sourceforge.mipa.predicatedetection.normal.wcp.WCPNormalProcess;


class NormalProcessResource {
	public String normalProcessID;
	
	public LocalPredicate lp;
	
	public Condition condition;
}

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class ECAManagerImp implements ECAManager {

    /** name of ECAManager */
    private String ecaManagerName;

    private BrokerInterface broker;

    private DataSource dataSource;
    
    private HashMap<String, NormalProcessResource> resourceMap;
    
    private static Logger logger = Logger.getLogger(ECAManagerImp.class);

    public ECAManagerImp(BrokerInterface broker, DataSource dataSource,
            String ecaManagerName) {
        this.setBroker(broker);
        this.ecaManagerName = ecaManagerName;
        this.dataSource = dataSource;
        this.resourceMap = new HashMap<String, NormalProcessResource>();
    }

    /**
     * @param ecaManagerName
     *            the ecaManagerName to set
     */
    public void setECAManagerName(String ecaManagerName) {
        this.ecaManagerName = ecaManagerName;
    }

    /**
     * @return the ecaManagerName
     */
    public String getECAManagerName() {
        return this.ecaManagerName;
    }

    /**
     * @param contextRegister
     *            the contextRegister to set
     */
    public void setBroker(BrokerInterface broker) {
        this.broker = broker;
    }

    /**
     * @return the contextRegister
     */
    public BrokerInterface getBroker() {
        return broker;
    }

    public void unregisterNormalProcess(String npID, Group g) throws RemoteException {
    	
    	NormalProcessResource NPResource = resourceMap.get(npID);
    	Condition condition = NPResource.condition;
    	LocalPredicate localPredicate = NPResource.lp;
    	//detach the data source
    	ArrayList<Atom> arrayList = localPredicate.getAtoms();
        for (int i = 0; i < arrayList.size(); i++) {
            Atom atom = arrayList.get(i);
            if (DEBUG) {
                System.out.print(" " + atom.getName());
                logger.info(atom.getName());
            }
            try {
            	dataSource.detach(condition, atom.getName());
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    	//stop normal process
    	try {
    		Coordinator coordinator = MIPAResource.getCoordinator();
    		coordinator.memberStopReady(g.getCoordinatorID(), npID);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	
        resourceMap.remove(npID);
    }
    
    @Override
    public void registerLocalPredicate(LocalPredicate localPredicate,
            String name, Group g) throws RemoteException {
        // EXPERIMENT
        //TimeInfo timeInfo = new TimeInfo();
        if (EXPERIMENT) {
            //timeInfo.item_1_begin = System.nanoTime();
        }

        try {
            Naming server = MIPAResource.getNamingServer();

            Coordinator coordinator = MIPAResource.getCoordinator();

            if (DEBUG) {
                System.out.println("Get normal process: " + name);
                logger.info("Get normal process: " + name);
            }

            String[] checkers = new String[g.getOwners().size()];
            String[] normalProcesses = new String[g.getMembers().size()];
            g.getOwners().toArray(checkers);
            g.getMembers().toArray(normalProcesses);

            Listener action = null;
            NormalProcess npStub = null;

            CheckMode checkMode = MIPAResource.getCheckMode();
            if (checkMode == CheckMode.NORMAL) {
                // NORMAL mode code puts here!
                switch (g.getType()) {
                case SCP:
                    SCPNormalProcess scpNP = new SCPNormalProcess(name,
                            checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                            scpNP, 0);
                    action = scpNP;

                    break;
                case OGA:
                    String[] subMembers = new String[g.getSubMembers().size()];
                    g.getSubMembers().toArray(subMembers);
                    OGANormalProcess ogaNP = new OGANormalProcess(name,
                            checkers, normalProcesses, subMembers);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                            ogaNP, 0);
                    action = ogaNP;
                    break;
                case WCP:
                    WCPNormalProcess wcpNP = new WCPNormalProcess(name,
                            checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                            wcpNP, 0);
                    action = wcpNP;
                    break;
                case LP:

                    break;
                case CADA:
                    CADANormalProcess cadaNP = new CADANormalProcess(name,
                            checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                            cadaNP, 0);
                    action = cadaNP;
                    break;
                case SEQUENCE:
                case CTL:
                    break;
                default:
                    System.out.println("Type " + g.getType()
                            + " has not been defined.");
                    logger.error("Type " + g.getType()
                            + " has not been defined.");
                }
            } else if (checkMode == CheckMode.LATTICE) {
                // LATTICE mode code puts here!
                switch (g.getType()) {
                case WCP:
                    WCPLatticeNormalProcess wcpNP = new WCPLatticeNormalProcess(
                            name, checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                            wcpNP, 0);
                    action = wcpNP;
                    break;
                case SCP:
                    SCPLatticeNormalProcess scpNP = new SCPLatticeNormalProcess(
                            name, checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                            scpNP, 0);
                    action = scpNP;
                    break;

                case OGA:

                    break;
                case LP:

                    break;
                case CADA:

                    break;
                case SEQUENCE:
                    SequenceLatticeNormalProcess sequenceNP = new SequenceLatticeNormalProcess(
                            name, checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                            sequenceNP, 0);
                    action = sequenceNP;
                    break;
                case SURSEQUENCE:
                    SequenceLatticeNormalProcess surSequenceNP = new SequenceLatticeNormalProcess(
                            name, checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                            surSequenceNP, 0);
                    action = surSequenceNP;
                    break;
                case WINDOWSEQUENCE:
                    SequenceLatticeNormalProcess winSequenceNP = new SequenceLatticeNormalProcess(
                            name, checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                            winSequenceNP, 0);
                    action = winSequenceNP;
                    break;
                case CTL:
                    SequenceLatticeNormalProcess ctlSequenceNP = new SequenceLatticeNormalProcess(
                            name, checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                    		ctlSequenceNP, 0);
                    action = ctlSequenceNP;
                    break;
                case TCTL:
                    TCTLLatticeNormalProcess tctlSequenceNP = new TCTLLatticeNormalProcess(
                            name, checkers, normalProcesses);
                    npStub = (NormalProcess) UnicastRemoteObject.exportObject(
                    		tctlSequenceNP, 0);
                    action = tctlSequenceNP;
                    break;
                default:
                    System.out.println("Type " + g.getType()
                            + " has not been defined.");
                    logger.error("Type " + g.getType()
                            + " has not been defined.");
                }
            }

            server.bind(name, npStub);
            coordinator.memberFinished(g.getCoordinatorID(), name);

            Condition everything = new EmptyCondition(action, localPredicate);

            // attaching condition to data source.

            if (DEBUG) {
                System.out.println("Local predicate includes:");
                logger.info("Local predicate includes:");
            }
            ArrayList<Atom> arrayList = localPredicate.getAtoms();
            for (int i = 0; i < arrayList.size(); i++) {
                Atom atom = arrayList.get(i);
                if (DEBUG) {
                    System.out.print(" " + atom.getName());
                    logger.info(atom.getName());
                }
                dataSource.attach(everything, atom.getName());
            }
            
            NormalProcessResource NPResource = new NormalProcessResource();
            NPResource.normalProcessID = name;
            NPResource.lp = localPredicate;
            NPResource.condition = everything;
            resourceMap.put(name, NPResource);
            
            if (DEBUG) {
                System.out.println(".");
                System.out
                        .println("binding condition to data source successful.");
                logger.info("Binding condition to data source successful.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (EXPERIMENT) {
            //timeInfo.item_1_end = System.nanoTime();
        }

        if (EXPERIMENT) {
            try {
                //PrintWriter out = new PrintWriter(new FileWriter(
                //        "log/eca_time_cost", true), true);
                //out.println((timeInfo.item_1_end - timeInfo.item_1_begin));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * registers local resource to resource manager.
     * 
     * @param resources
     *            local resources
     */
    public void registerResources(ArrayList<SensorAgent> resources) {
        // TODO auto scan local resource

        try {
            for (int i = 0; i < resources.size(); i++) {
                SensorAgent resource = resources.get(i);
                broker.registerResource(resource.getName(), resource
                        .getValueType(), ecaManagerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public HashMap<String, NormalProcessResource> getResourceMap() {
		return resourceMap;
	}

	public void setResourceMap(HashMap<String, NormalProcessResource> resourceMap) {
		this.resourceMap = resourceMap;
	}
}
