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

import java.io.Serializable;

/**
 * 
 * @author tingting Hua<huatingting0820@gmail.com>
 * 
 */
public class LocalState implements Serializable{

	private static final long serialVersionUID = 2360560254269250708L;

	protected int processID;
	
    protected int intervalID;

	protected LatticeVectorClock vc;

	protected boolean localPredicate;
	
	protected String ID;
	
	protected long physicalTime = 0;
	
	private long startTime = 0;
	private long endTime = 0;
	
	public LocalState(int pID, LatticeVectorClock lvc, boolean lp) {
        processID = pID;
        vc = new LatticeVectorClock(lvc);
        localPredicate = lp;
    }

	public LocalState(int pID, int iID, LatticeVectorClock lvc, boolean lp) {
		processID = pID;
		intervalID = iID;
		vc = new LatticeVectorClock(lvc);
		localPredicate = lp;
	}
	
	public LocalState(int pID, int iID, LatticeVectorClock lvc, boolean lp, long physicalTime) {
        processID = pID;
        intervalID = iID;
        vc = new LatticeVectorClock(lvc);
        localPredicate = lp;
        this.physicalTime = physicalTime;
    }

	public LocalState(int pID, int iID, boolean localPredicate, long startTime,	long endTime) {
		processID = pID;
        intervalID = iID;
        this.localPredicate = localPredicate;
        this.startTime = startTime;
        this.endTime = endTime;
	}

	public void setVC(LatticeVectorClock lvc) {

		vc.setVectorClock(lvc.getVectorClock());

	}
	
	public int getintervalID(){
		return intervalID;
	}

	public boolean getlocalPredicate() {
		return localPredicate;
	}

	public LatticeVectorClock getvc() {
		return vc;
	}

	public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String id) {
        ID = id;
    }

    public long getPhysicalTime() {
        return physicalTime;
    }

    public void setPhysicalTime(long physicalTime) {
        this.physicalTime = physicalTime;
    }

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
}
