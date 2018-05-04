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
package net.sourceforge.mipa.predicatedetection.lattice.tctl;

import static config.Config.LOG_DIRECTORY;
import static config.Debug.DEBUG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.text.html.HTMLDocument.HTMLReader.SpecialAction;

import com.sun.corba.se.impl.orbutil.graph.Node;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.components.TimedMessageContent;
import net.sourceforge.mipa.predicatedetection.AbstractFIFOChecker;
import net.sourceforge.mipa.predicatedetection.Composite;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.NodeType;
import net.sourceforge.mipa.predicatedetection.Structure;
import net.sourceforge.mipa.predicatedetection.TimedConnector;
import net.sourceforge.mipa.predicatedetection.lattice.LocalState;
import net.sourceforge.mipa.predicatedetection.lattice.sequence.SequenceLatticeIDNode;

/**
 * 
 * @author Yiling Yang <csylyang@gmail.com>
 * 
 */
public class TimedLatticeChecker extends AbstractFIFOChecker {

	private static final long serialVersionUID = -8857276299013214921L;

	/** output the lattice constructor procedure information */
	private PrintWriter out = null;

	private PrintWriter mappedLatticeOut = null;

	/** store all the received local state */
	private ArrayList<ArrayList<LocalState>> localStateSet;

	private ArrayList<ArrayList<LocalState>> localStateTempSet;

	private TCTLLatticeIDNode startNode;

	private TCTLLatticeIDNode maxNode;

	private TCTLLatticeIDNode[] extremaSurfaceNodes;

	private HashMap<String, TCTLLatticeIDNode> mappedLattice;

	private int latticeNumber = 0;

	private int surfaceNumber = 0;

	private PrintWriter outTime = null;

	private int[] interNumArray;

	private boolean[] interNumFlag;

	public long wastedTime = 0;

	public long responseTime = 0;

	private boolean flag = false;

	private boolean jOptionPane = false;

	private long startTime;

	private int numberOfProcess;

	private long epsilon;

	private boolean isLatticeEmpty = true;

	private HashMap<String, ArrayList<String>> CGSToNPs;

	private String TAHead = "";

	private String TALocation = "";

	private String TAInit = "";

	private String TATransition = "";

	private String TAEnd = "";

	private String result = "false";

	private Structure specification;

	private String TCTLPredicate = "";
	
	private long time_e;

	public TimedLatticeChecker(ResultCallback application, String predicateID,
			String checkerName, String[] children, Structure specification) {
		super(application, predicateID, checkerName, children);
		numberOfProcess = children.length;
		epsilon = MIPAResource.getEpsilon();
		localStateSet = new ArrayList<ArrayList<LocalState>>();
		localStateTempSet = new ArrayList<ArrayList<LocalState>>();
		extremaSurfaceNodes = new TCTLLatticeIDNode[numberOfProcess];
		mappedLattice = new HashMap<String, TCTLLatticeIDNode>();
		interNumArray = new int[numberOfProcess];
		interNumFlag = new boolean[numberOfProcess];
		this.specification = specification;
		TCTLPredicate = parseTCTL(specification);
		try {
			PrintWriter pw = new PrintWriter(new File("log/Predicate.q"));
			pw.write(TCTLPredicate);
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// if (DEBUG) {
		// try {
		// out = new PrintWriter(LOG_DIRECTORY + "/timedChecker.log");
		// mappedLatticeOut = new PrintWriter(LOG_DIRECTORY
		// + "/mappedLatticeNode.log");
		// outTime = new PrintWriter(LOG_DIRECTORY
		// + "/SurSequence_Time.log");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		
		try {
				 //out = new PrintWriter(LOG_DIRECTORY + "/timedChecker.log");
				 mappedLatticeOut = new PrintWriter(LOG_DIRECTORY
				 + "/mappedLatticeNode.log");
				 outTime = new PrintWriter(LOG_DIRECTORY
				 + "/TCTL_Time.log");
			} catch (Exception e) {
				 e.printStackTrace();
			}
		
		
		
		String initIndex = "0";
		for (int i = 1; i < numberOfProcess; i++) {
			initIndex += "-0";
		}
		for (int i = 0; i < numberOfProcess; i++) {
			interNumArray[i] = 0;
			interNumFlag[i] = true;
			localStateSet.add(new ArrayList<LocalState>());
			localStateTempSet.add(new ArrayList<LocalState>());
		}
		startTime = Long.MAX_VALUE;
		CGSToNPs = new HashMap<String, ArrayList<String>>();
		getCGSToNPs(specification);
		TAHead = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<!DOCTYPE nta PUBLIC "
				+ "'-//Uppaal Team//DTD Flat System 1.1//EN' "
				+ "'http://www.it.uu.se/research/group/darts/uppaal/flat-1_1.dtd'>"
				+ "<nta><declaration></declaration>"
				+ "<template><name>LAT</name>";
		TAInit = "<init ref=\"id" + initIndex + "\"/>";
		TAEnd = "</template><system>Process = LAT(); system Process;</system></nta>";
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
	}

	@Override
	protected void handle(ArrayList<Message> messages) {
		String normalProcess = messages.get(0).getSenderID();
		int id = nameToID.get(normalProcess).intValue();
		for (int i = 0; i < messages.size(); i++) {
			responseTime = 0;
			wastedTime = 0;
			Message message = messages.get(i);
			TimedMessageContent content = (TimedMessageContent) message
					.getMessageContent();

			// compute interval number of current process
			if (content.getLocalPredicate() == true) {
				if (interNumFlag[id] == true) {
					interNumArray[id]++;
					interNumFlag[id] = false;
				}
			} else {
				interNumFlag[id] = true;
			}
			if (isLatticeEmpty == true) {
				if (localStateTempSet.get(id).size() == 0) {
					startTime = Math.min(startTime, content.getStartTime());

					LocalState localState = new LocalState(id,
							interNumArray[id], content.getLocalPredicate(),
							content.getStartTime(), content.getEndTime());
					localStateTempSet.get(id).add(localState);

					boolean isAllNonEmpty = true;
					for (int j = 0; j < numberOfProcess; j++) {
						if (localStateTempSet.get(j).size() == 0) {
							isAllNonEmpty = false;
							break;
						}
					}
					if (isAllNonEmpty == true) {
						for (int j = 0; j < numberOfProcess; j++) {
							localStateTempSet.get(j).get(0)
									.setStartTime(startTime);
							Iterator<LocalState> iterator = localStateTempSet
									.get(j).iterator();
							while (iterator.hasNext()) {
								LocalState ls = (LocalState) iterator.next();
								ls.setStartTime(ls.getStartTime() - startTime);
								ls.setEndTime(ls.getEndTime() - startTime);
							}
							localStateSet.get(j).add(
									localStateTempSet.get(j).get(0));
						}
						addInitialCGS();
						isLatticeEmpty = false;
						for (int j = 0; j < numberOfProcess; j++) {

							if (localStateTempSet.get(j).size() > 1) {
								Iterator<LocalState> iterator = localStateTempSet
										.get(j).iterator();
								iterator.next();
								while (iterator.hasNext()) {
									LocalState ls = (LocalState) iterator
											.next();
									localStateSet.get(j).add(ls);
									expandLattice(ls, j);
								}
							}
						}
						localStateTempSet = null;
					}
				} else {
					LocalState localState = new LocalState(id,
							interNumArray[id], content.getLocalPredicate(),
							content.getStartTime(), content.getEndTime());
					localStateTempSet.get(id).add(localState);
				}
			} else {
				LocalState localState = new LocalState(id, interNumArray[id],
						content.getLocalPredicate(), content.getStartTime()
								- startTime, content.getEndTime() - startTime);
				localStateSet.get(id).add(localState);
				expandLattice(localState, id);
			}
		}
	}

	private void addInitialCGS() {
		String[] index = new String[numberOfProcess];
		LocalState[] CGS = new LocalState[numberOfProcess];
		for (int i = 0; i < numberOfProcess; i++) {
			index[i] = "0";
			CGS[i] = localStateSet.get(i).get(0);
		}
		startNode = createNode(CGS, index, epsilon);
		maxNode = startNode;
		String initIndex = "0";
		for (int i = 1; i < numberOfProcess; i++) {
			initIndex += "-0";
		}
		for (int i = 0; i < numberOfProcess; i++) {
			extremaSurfaceNodes[i] = startNode;
		}
		mappedLattice.put(getIDString(index), startNode);
		computeCGS(startNode);
		latticeNumber++;
		// String name = ((Composite) specification.getChildren().get(0)
		// .getChildren().get(0)).getNodeName();
		String name = CGSToNPs.keySet().iterator().next().split(" ")[0];
		if (startNode.getSatisfiedPredicates().trim().equals("")) {
			String s = "<declaration>clock x;bool " + name
					+ "=false;</declaration>";
			TAHead += s;
		} else if (startNode.getSatisfiedPredicates().trim().equals(name)) {
			String s = "<declaration>clock x;bool " + name
					+ "=true;</declaration>";
			TAHead += s;
		}
		TALocation += "<location id=\"id" + initIndex + "\">"
				+ "<name>L"+ getIDLocation(startNode.getID()) +"</name>"
				+ "<label kind=\"invariant\">x&lt;="
				+ startNode.getPosEndTime() + "</label></location>";
		check(maxNode);
	}
	
	private String getIDLocation(String[] index) {
		String ID = "";
		for (int j = 0; j < numberOfProcess; j++) {
			ID += index[j];
		}
		return ID.trim();
	}
	
	private String getIDLocation(String index) {
		String[] s = index.split(" ");
		String ID = s[0];
		for (int j = 1; j < numberOfProcess; j++) {
			ID += s[j];
		}
		return ID.trim();
	}

	private String getIDString(String[] index) {
		String ID = "";
		for (int j = 0; j < numberOfProcess; j++) {
			ID += index[j] + " ";
		}
		return ID.trim();
	}

	private String getID(String[] index) {
		String ID = index[0];
		for (int j = 1; j < numberOfProcess; j++) {
			ID += "-" + index[j];
		}
		return ID.trim();
	}

	private String getID(String index) {
		String[] s = index.split(" ");
		String ID = s[0];
		for (int j = 1; j < numberOfProcess; j++) {
			ID += "-" + s[j];
		}
		return ID.trim();
	}

	private void expandLattice(LocalState localState, int id) {
		// 每长一次，都要检测一次；每次都是只生成active surface的点的那部分，写入xml文件中；三值的检测是通过在每个active
		// surface点后加入无穷路径完成
		// 可以复用原来surface维护算法，删去旧的点，因为那部分对应的TA不会变，只要保留之前的TA结构进xml文件即可。旧的lattice节点可以删除

		// the new localState will not affect anything about lattice
		long time_s = System.currentTimeMillis();
		if (localStateSet.get(id).size() != Integer
				.valueOf(maxNode.getID()[id]) + 2) {
			// lattice will not be changed
			repeatCallBack();
			time_e = System.currentTimeMillis();
			outTime.println((time_e - time_s)+" 0");
			outTime.flush();
		} else {
			//System.out.println("In growing...");
			LocalState[] globalState = getCombinedGlobalState(maxNode,
					localState, id);
			// the next global state of maxNode on the NP(id) is CGS, thus new
			// CGS will be grew.
			if (isCGS(globalState, id)) {
				// grow lattice with new added CGSs, and update new maxNode and
				// extremaSurfaceNode, then check()

				// get extremaSurfaceNode at the NP(id) side
				extremaSurfaceNodes[id] = findNewExtremaSurfaceNode(
						extremaSurfaceNodes[id], localState, id);
				mappedLattice.put(getIDString(extremaSurfaceNodes[id].getID()),
						extremaSurfaceNodes[id]);
				latticeNumber++;

				// mappedLatticeOut.println("FindExtre: "+((new
				// Date()).getTime()-t)+"                 ");

				// t = (new Date()).getTime();
				// check on the new added CGS
				computeCGS(extremaSurfaceNodes[id]);
				// mappedLatticeOut.println("CheckExtre: "+((new
				// Date()).getTime()-t)+"                 ");

				TALocation += "<location id=\"id"
						+ getID(extremaSurfaceNodes[id].getID()) + "\">"
						+ "<name>L"+ getIDLocation(extremaSurfaceNodes[id].getID()) +"</name>"
						+ "<label kind=\"invariant\">x&lt;="
						+ extremaSurfaceNodes[id].getPosEndTime()
						+ "</label></location>";
				String name = CGSToNPs.keySet().iterator().next().split(" ")[0];
				String s = "";
				if (extremaSurfaceNodes[id].getSatisfiedPredicates().trim()
						.equals("")) {
					s = name + "=false";
				} else if (extremaSurfaceNodes[id].getSatisfiedPredicates()
						.trim().equals(name)) {
					s = name + "=true";
				}
				ArrayList<TCTLLatticeIDNode> prec = prec(extremaSurfaceNodes[id]);
				for (int i = 0; i < prec.size(); i++) {
					TATransition += "<transition><source ref=\"id"
							+ getID(prec.get(i).getID()) + "\"/>"
							+ "<target ref=\"id"
							+ getID(extremaSurfaceNodes[id].getID()) + "\"/>"
							+ "<label kind=\"guard\">x&gt;="
							+ extremaSurfaceNodes[id].getPosStartTime()
							+ "</label>" + "<label kind=\"assignment\">" + s
							+ "</label></transition>";
				}

				// t = (new Date()).getTime();
				// recursive grow all new CGSs
				ArrayList<TCTLLatticeIDNode> set = new ArrayList<TCTLLatticeIDNode>();
				set.add(extremaSurfaceNodes[id]);
				while (!set.isEmpty()) {
					TCTLLatticeIDNode node = set.remove(0);
					boolean hasSub = false;
					for (int i = 0; i < children.length; i++) {
						if (i == id)
							continue;
						String[] index = copyIndex(node.getID());
						index[i] = Integer
								.toString(Integer.valueOf(index[i]) + 1);
						if (indexIsValid(index)
								&& mappedLattice.get(getIDString(index)) == null) {
							LocalState[] newGlobalState = getCombinedGlobalState(
									node,
									localStateSet.get(i).get(
											Integer.valueOf(index[i])), i);
							if (isCGS(newGlobalState, i)) {
								hasSub = true;
								TCTLLatticeIDNode newNode = createNode(
										newGlobalState, index, epsilon);
								set.add(newNode);
								mappedLattice.put(getIDString(index), newNode);

								// experiment
								latticeNumber++;

								// check on each new added CGSs
								computeCGS(newNode);

								TALocation += "<location id=\"id"
										+ getID(newNode.getID()) + "\">"
										+ "<name>L"+ getIDLocation(newNode.getID()) +"</name>"
										+ "<label kind=\"invariant\">x&lt;="
										+ newNode.getPosEndTime()
										+ "</label></location>";
								s = "";
								if (newNode.getSatisfiedPredicates().trim()
										.equals("")) {
									s = name + "=false";
								} else if (newNode.getSatisfiedPredicates()
										.trim().equals(name)) {
									s = name + "=true";
								}
								prec = prec(newNode);
								for (int j = 0; j < prec.size(); j++) {
									TATransition += "<transition><source ref=\"id"
											+ getID(prec.get(j).getID())
											+ "\"/><target ref=\"id"
											+ getID(newNode.getID())
											+ "\"/>"
											+ "<label kind=\"guard\">x&gt;="
											+ newNode.getPosStartTime()
											+ "</label>"
											+ "<label kind=\"assignment\">"
											+ s
											+ "</label></transition>";
								}
							}
						}
					}
					if (!hasSub) {
						// find maxNode
						maxNode = node;
					}
				}
				// mappedLatticeOut.println("Grow: "+((new
				// Date()).getTime()-t)+"                 ");

				// till now, grow complete
				// t = (new Date()).getTime();
				// update new extremaSurfaceNodes
				for (int i = 0; i < children.length; i++) {
					if (Integer.valueOf(maxNode.getID()[i]) != localStateSet
							.get(i).size() - 1) {
						extremaSurfaceNodes[i] = null;
						continue;
					}
					ArrayList<TCTLLatticeIDNode> backSet = new ArrayList<TCTLLatticeIDNode>();
					backSet.add(maxNode);
					while (!backSet.isEmpty()) {
						TCTLLatticeIDNode newNode = backSet.remove(0);
						prec = precWithID(newNode, i);
						if (prec.isEmpty()) {
							extremaSurfaceNodes[i] = newNode;
							break;
						} else {
							int num = prec.size();
							for (int j = 0; j < num; j++) {
								TCTLLatticeIDNode node = prec.remove(0);
								if (!backSet.contains(node)) {
									backSet.add(node);
								}
							}
						}
					}
				}
				// mappedLatticeOut.println("UpdateExtrema: "+((new
				// Date()).getTime()-t)+"           ");

				// t = (new Date()).getTime();

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
					if (flag == false) {

						// mappedLattice.remove(ind);
						it.remove();
					}
				}
				check(maxNode);
				long time_c = System.currentTimeMillis();
				outTime.println((time_e - time_s) + " "+ (time_c - time_e));
				outTime.flush();
				mappedLatticeOut.flush();
				// mappedLatticeOut.println("Delete old: "+((new
				// Date()).getTime()-t)+"             ");
			} else {
				//System.out.println("In growing...");
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
					if (flag == false) {

						// mappedLattice.remove(ind);
						it.remove();
					}
				}
				extremaSurfaceNodes[id] = null;
				// do surface checking
				
				check(maxNode);
				long time_c = System.currentTimeMillis();
				outTime.println((time_e - time_s) + " "+ (time_c - time_e));	
				outTime.flush();
				mappedLatticeOut.flush();
			}
		}
		// mappedLatticeOut.println((new Date()).getTime() - ti);
		// mappedLatticeOut.flush();
	}

	private String[] copyIndex(String[] index) {
		String[] ind = new String[numberOfProcess];
		for (int i = 0; i < numberOfProcess; i++) {
			ind[i] = index[i];
		}
		return ind;
	}

	// /**
	// * 在当前id这个n-1维的surface所在的空间中，在每一维用二分搜索找到此空间中的extremaSurfaceNode
	// * @param abstractLatticeIDNode
	// * @param newNode
	// * @param localState
	// * @param id
	// * @return
	// */
	// private AbstractLatticeIDNode findNewExtremaSurfaceNode(
	// AbstractLatticeIDNode oriNode,
	// AbstractLatticeIDNode newNode, LocalState localState, int id) {
	// if(isCGS(oriNode.getGlobalState(), id)) {
	// return oriNode;
	// }
	// else {
	// String[] index;
	// String[] indexNew = newNode.getID();
	// String[] indexOri = oriNode.getID();
	// for(int i=0;i<numberOfProcess;i++) {
	// if(i != id) {
	//
	// }
	// }
	// }
	//
	//
	// String[] temp = new String[numberOfProcess];
	// AbstractLatticeIDNode tempNode = null;
	//
	// Set<String> keySet = getMappedLattice().keySet();
	// Iterator<String> it = keySet.iterator();
	// while (it.hasNext()) {
	// String s = it.next();
	// String[] ind = s.split(" ");
	// if (Integer.valueOf(ind[id]) == getLocalStateSet()
	// .get(id).size() - 2) {
	// LocalState[] globalState = getCombinedGlobalState(mappedLattice.get(s),
	// localState,
	// id);
	// if (isCGS(globalState, id)) {
	// String[] index = new String[numberOfProcess];
	// for (int j = 0; j < numberOfProcess; j++) {
	// index[j] = mappedLattice.get(s).getID()[j];
	// }
	// index[id] = Integer.toString(localStateSet.get(id).size() - 1);
	// AbstractLatticeIDNode newNode = createNode(globalState, index, 0);
	// if(tempNode != null) {
	// boolean flag = false;
	// for(int i =0;i<numberOfProcess;i++) {
	// if(Integer.valueOf(index[i]) < Integer.valueOf(temp[i])) {
	// flag = true;
	// break;
	// }
	// }
	// if(flag == true) {
	// for(int i =0;i<numberOfProcess;i++) {
	// temp[i] = index[i];
	// }
	// tempNode = newNode;
	// }
	// }
	// else {
	// for(int i =0;i<numberOfProcess;i++) {
	// temp[i] = index[i];
	// }
	// tempNode = newNode;
	// }
	// }
	// }
	// }
	// return tempNode;
	// }

	private boolean indexIsValid(String[] index) {
		for (int i = 0; i < numberOfProcess; i++) {
			if (Integer.valueOf(index[i]) > Integer.valueOf(localStateSet
					.get(i).size()) - 1) {
				return false;
			}
		}
		return true;
	}

	private TCTLLatticeIDNode findNewExtremaSurfaceNode(
			TCTLLatticeIDNode oriNode, LocalState localState, int id) {
		String[] temp = new String[numberOfProcess];
		TCTLLatticeIDNode tempNode = null;

		Set<String> keySet = getMappedLattice().keySet();
		Iterator<String> it = keySet.iterator();
		while (it.hasNext()) {
			String s = it.next();
			String[] ind = s.split(" ");
			if (Integer.valueOf(ind[id]) == getLocalStateSet().get(id).size() - 2) {
				LocalState[] globalState = getCombinedGlobalState(
						mappedLattice.get(s), localState, id);
				if (isCGS(globalState, id)) {
					String[] index = new String[numberOfProcess];
					for (int j = 0; j < numberOfProcess; j++) {
						index[j] = mappedLattice.get(s).getID()[j];
					}
					index[id] = Integer
							.toString(localStateSet.get(id).size() - 1);
					TCTLLatticeIDNode newNode = createNode(globalState, index,
							epsilon);
					if (tempNode != null) {
						boolean flag = false;
						for (int i = 0; i < numberOfProcess; i++) {
							if (Integer.valueOf(index[i]) < Integer
									.valueOf(temp[i])) {
								flag = true;
								break;
							}
						}
						if (flag == true) {
							for (int i = 0; i < numberOfProcess; i++) {
								temp[i] = index[i];
							}
							tempNode = newNode;
						}
					} else {
						for (int i = 0; i < numberOfProcess; i++) {
							temp[i] = index[i];
						}
						tempNode = newNode;
					}
				}
			}
		}
		return tempNode;

		/*
		 * AbstractLatticeIDNode newNode = null;
		 * ArrayList<AbstractLatticeIDNode> set = new
		 * ArrayList<AbstractLatticeIDNode>(); set.add(oriNode); while
		 * (!set.isEmpty()) { AbstractLatticeIDNode node = set.remove(0);
		 * LocalState[] globalState = getCombinedGlobalState(node, localState,
		 * id); if (isCGS(globalState, id)) { String[] index = new
		 * String[numberOfProcess]; for (int j = 0; j < numberOfProcess; j++) {
		 * index[j] = node.getID()[j]; } index[id] =
		 * Integer.toString(localStateSet.get(id).size() - 1); newNode =
		 * createNode(globalState, index); return newNode; } else { for (int i =
		 * 0; i < numberOfProcess; i++) { if (i == id) continue; String[] index
		 * = new String[numberOfProcess]; for (int j = 0; j < numberOfProcess;
		 * j++) { index[j] = node.getID()[j]; } index[i] =
		 * Integer.toString(Integer.valueOf(index[i]) + 1); String ID = ""; for
		 * (int j = 0; j < numberOfProcess; j++) { ID += index[j] + " "; } ID =
		 * ID.trim(); if (mappedLattice.get(ID) != null) {
		 * set.add(mappedLattice.get(ID)); } } } } return newNode;
		 */
	}

	private boolean isCGS(LocalState[] CGS, int id) {
		long time = Math.max(0, CGS[id].getStartTime() - epsilon);
		for (int i = 0; i < numberOfProcess; i++) {
			long time_1 = CGS[i].getEndTime() + epsilon;
			if (i != id && time_1 < time) {
				return false;
			}
		}
		return true;
	}

	private LocalState[] getCombinedGlobalState(TCTLLatticeIDNode oriNode,
			LocalState localState, int id) {
		LocalState[] globalState = new LocalState[numberOfProcess];
		for (int i = 0; i < numberOfProcess; i++) {
			globalState[i] = oriNode.getGlobalState()[i];
		}
		globalState[id] = localState;
		return globalState;
	}

	protected ArrayList<TCTLLatticeIDNode> prec(TCTLLatticeIDNode node) {
		ArrayList<TCTLLatticeIDNode> set = new ArrayList<TCTLLatticeIDNode>();
		for (int i = 0; i < numberOfProcess; i++) {
			String[] index = new String[numberOfProcess];
			for (int j = 0; j < numberOfProcess; j++) {
				index[j] = node.getID()[j];
			}
			index[i] = Integer.toString(Integer.valueOf(index[i]) - 1);
			String ID = "";
			for (int j = 0; j < numberOfProcess; j++) {
				ID += index[j] + " ";
			}
			ID = ID.trim();
			if (mappedLattice.get(ID) != null) {
				set.add(mappedLattice.get(ID));
			}
		}
		return set;
	}

	private ArrayList<TCTLLatticeIDNode> precWithID(TCTLLatticeIDNode node,
			int id) {
		ArrayList<TCTLLatticeIDNode> set = new ArrayList<TCTLLatticeIDNode>();
		for (int i = 0; i < numberOfProcess; i++) {
			if (i == id)
				continue;
			String[] index = copyIndex(node.getID());
			index[i] = Integer.toString(Integer.valueOf(index[i]) - 1);
			TCTLLatticeIDNode newNode = mappedLattice.get(getIDString(index));
			if (newNode != null) {
				set.add(newNode);
			}
		}
		return set;
	}

	protected ArrayList<TCTLLatticeIDNode> sub(TCTLLatticeIDNode node) {
		ArrayList<TCTLLatticeIDNode> set = new ArrayList<TCTLLatticeIDNode>();
		for (int i = 0; i < numberOfProcess; i++) {
			String[] index = new String[numberOfProcess];
			for (int j = 0; j < numberOfProcess; j++) {
				index[j] = node.getID()[j];
			}
			index[i] = Integer.toString(Integer.valueOf(index[i]) + 1);
			String ID = "";
			for (int j = 0; j < numberOfProcess; j++) {
				ID += index[j] + " ";
			}
			ID = ID.trim();
			if (indexIsValid(index) && mappedLattice.get(ID) != null) {
				set.add(mappedLattice.get(ID));
			}
		}
		return set;
	}

	private ArrayList<TCTLLatticeIDNode> subWithID(TCTLLatticeIDNode node,
			int id) {
		ArrayList<TCTLLatticeIDNode> set = new ArrayList<TCTLLatticeIDNode>();
		for (int i = 0; i < numberOfProcess; i++) {
			if (i == id)
				continue;
			String[] index = new String[numberOfProcess];
			for (int j = 0; j < numberOfProcess; j++) {
				index[j] = node.getID()[j];
			}
			index[i] = Integer.toString(Integer.valueOf(index[i]) + 1);
			String ID = "";
			for (int j = 0; j < numberOfProcess; j++) {
				ID += index[j] + " ";
			}
			ID = ID.trim();
			if (indexIsValid(index) && mappedLattice.get(ID) != null) {
				set.add(mappedLattice.get(ID));
			}
		}
		return set;
	}

	protected TCTLLatticeIDNode[] getExtremaSurfaceNodes() {
		return extremaSurfaceNodes;
	}

	protected TCTLLatticeIDNode getMaxNode() {
		return maxNode;
	}

	public TCTLLatticeIDNode getStartNode() {
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

	public void setStartNode(TCTLLatticeIDNode startNode) {
		this.startNode = startNode;
	}

	public HashMap<String, TCTLLatticeIDNode> getMappedLattice() {
		return mappedLattice;
	}

	public void setMappedLattice(
			HashMap<String, TCTLLatticeIDNode> mappedLattice) {
		this.mappedLattice = mappedLattice;
	}

	public TCTLLatticeIDNode createNode(LocalState[] globalState, String[] s,
			long epsilon) {
		TCTLLatticeIDNode node = new TCTLLatticeIDNode(globalState, s, epsilon);
		return node;
	}

	private void computeCGS(TCTLLatticeIDNode node) {
		Set<String> CGSs = CGSToNPs.keySet();
		Iterator<String> it = CGSs.iterator();
		while (it.hasNext()) {
			String CGS = it.next();
			boolean result = true;
			ArrayList<String> NPs = CGSToNPs.get(CGS);
			for (int i = 0; i < NPs.size(); i++) {
				String np = NPs.get(i);
				// String[] c = np.split("ss");
				// assert (c.length == 2);
				// int npIndex = Integer.valueOf(c[1]);
				int npIndex = 0;
				// System.err.println(np);
				for (int j = 0; j < children.length; j++) {
					// System.err.println("Children: "+children[j]);
					if (children[j].equals(np)) {
						npIndex = j;
						// System.err.println("Find "+j);
						break;
					}
				}
				result = result
						&& (node.getGlobalState()[npIndex].getlocalPredicate());
			}
			if (result == true) {
				node.addSatisfiedPredicates(CGS.charAt(0));
			}
		}
		// // add {}
		// if (node.getSatisfiedPredicates().trim().equals("")) {
		// node.addSatisfiedPredicates('z');
		// }
	}

	public void check(TCTLLatticeIDNode currentNode) {
		mappedLatticeOut.println("The size: "+ mappedLattice.size()+" "+latticeNumber);
		String infiniteLocation = "";
		String infiniteFalseTransition = "";
		String infiniteTrueTransition = "";
		String type = ((TimedConnector) specification.getChildren().get(1))
				.getNodeName();
		Iterator<Entry<String, TCTLLatticeIDNode>> it = mappedLattice
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, TCTLLatticeIDNode> entry = (Map.Entry<String, TCTLLatticeIDNode>) it
					.next();
			String id = entry.getKey();
			infiniteLocation += "<location id=\"id" + getID(id) + "-0"
					+ "\"></location>";
			String name = CGSToNPs.keySet().iterator().next().split(" ")[0];
			long t = Long.MAX_VALUE;
			for(int i = 0;i <numberOfProcess;i++) {
				String idString = entry.getValue().getID()[i];
				if(idString.equals(String.valueOf(localStateSet.get(i).size()-1))) {
					LocalState state = entry.getValue().getGlobalState()[i];
					t = Math.min(t, Math.max(0, state.getEndTime()-epsilon));
				}
			}
			infiniteTrueTransition += "<transition><source ref=\"id"
					+ getID(id) + "\"/><target ref=\"id" + getID(id) + "-0"
					+ "\"/>" + "<label kind=\"guard\">x&gt;="
					+ t
					+ "</label>" + "<label kind=\"assignment\">" + name + "=true"
					+ "</label></transition>";
			infiniteFalseTransition += "<transition><source ref=\"id"
					+ getID(id) + "\"/><target ref=\"id" + getID(id) + "-0"
					+ "\"/>" + "<label kind=\"guard\">x&gt;="
					+ t
					+ "</label>" + "<label kind=\"assignment\">" + name + "=false"
					+ "</label></transition>";
		}
		boolean TATrueResult = false;
		boolean TAFalseResult = false;
		String TATrue = TAHead + TALocation + infiniteLocation + TAInit
				+ TATransition + infiniteTrueTransition + TAEnd;
		String TAFalse = TAHead + TALocation + infiniteLocation + TAInit
				+ TATransition + infiniteFalseTransition + TAEnd;
		// check ta and tctlpredicate
		//System.out.println("In checking...");
		try {
			//System.out.println("TATrue");
			PrintWriter pw = new PrintWriter(new File("log/TATrue.xml"));
			pw.write(TATrue);
			pw.flush();
			pw.close();
			pw = new PrintWriter(new File("log/TAFalse.xml"));
			pw.write(TAFalse);
			pw.flush();
			pw.close();
			
			time_e = System.currentTimeMillis();
			
			// String[] cmd =
			// {"bin/windows/uppaal-4.0.13/bin-Win32/verifyta.exe","TATrue.xml","Predicate.q"};
			// Process pro = Runtime.getRuntime().exec(cmd);
			Process pro = Runtime
					.getRuntime()
					.exec("bin/windows/uppaal-4.0.13/bin-Win32/verifyta.exe log/TATrue.xml log/Predicate.q");
			BufferedReader input = new BufferedReader(new InputStreamReader(
					pro.getInputStream(), "GBK"));
			String line = null;
			while ((line = input.readLine()) != null) {
//				System.out.println(line);
				if (line.endsWith("Property is satisfied.")) {
					TATrueResult = true;
					input.close();
					//System.out.println(line);
					break;
				} else if (line.endsWith("Property is NOT satisfied.")) {
					TATrueResult = false;
					input.close();
					//System.out.println(line);
					break;
				}
			}

			//System.out.println("TAFalse");
			pro = Runtime
					.getRuntime()
					.exec("bin/windows/uppaal-4.0.13/bin-Win32/verifyta.exe log/TAFalse.xml log/Predicate.q");
			input = new BufferedReader(new InputStreamReader(
					pro.getInputStream(), "GBK"));
			line = null;
			while ((line = input.readLine()) != null) {
//				System.out.println(line);
				if (line.endsWith("Property is satisfied.")) {
					TAFalseResult = true;
					input.close();
					//System.out.println(line);
					break;
				} else if (line.endsWith("Property is NOT satisfied.")) {
					TAFalseResult = false;
					input.close();
					//System.out.println(line);
					break;
				}
			}
			//System.out.println("Checking finished...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (TATrueResult == false && TAFalseResult == false) {
			result = "false";
		} else if (TATrueResult == false && TAFalseResult == true) {
			result = "uncertain";
		} else if (TATrueResult == true && TAFalseResult == false) {
			result = "uncertain";
		} else if (TATrueResult == true && TAFalseResult == true) {
			result = "true";
		}
		//System.out.println(result);
	}

	public String parseTCTL(Structure specification) {
		String predicate = "";
		TimedConnector timedConnector = (TimedConnector) specification
				.getChildren().get(1);
		String type = timedConnector.getNodeName();
		if (type.equals("EF")) {
			predicate += "E<>";
		} else if (type.equals("AF")) {
			predicate += "A<>";
		} else if (type.equals("EG")) {
			predicate += "E[]";
		} else if (type.equals("AG")) {
			predicate += "A[]";
		}
		predicate += "((";
		String leftOperator = timedConnector.getLeftOperator();
		String leftBound = timedConnector.getLeftBound();
		String rightOperator = timedConnector.getRightOperator();
		String rightBound = timedConnector.getRightBound();
		if (leftOperator.equals("greater-than")) {
			predicate = predicate + "Process.x>" + leftBound;
		} else if (leftOperator.equals("greater-or-equal")) {
			predicate = predicate + "Process.x>=" + leftBound;
		}
		if (rightOperator.equals("less-than")) {
			if (!rightBound.equals("infinity")) {
				predicate = predicate + " and Process.x<" + rightBound;
			}
		} else if (rightOperator.equals("less-or-equal")) {
			if (!rightBound.equals("infinity")) {
				predicate = predicate + " and Process.x<=" + rightBound;
			}
		}
		String name = ((Composite) specification.getChildren().get(0)
				.getChildren().get(0)).getNodeName();
		if (type.equals("EF") || type.equals("AF")) {
			predicate += ") and Process." + name + ")";
		} else {
			predicate += ") imply Process." + name + ")";
		}
		return predicate;
	}

	public void repeatCallBack() {
		try {
			//application.callback(String.valueOf(result));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
