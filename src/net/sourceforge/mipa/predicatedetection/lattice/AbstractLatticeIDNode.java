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

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Yiling Yang <csylyang@gmail.com>
 *
 */
public class AbstractLatticeIDNode implements Serializable {

    private static final long serialVersionUID = -2194410751685873911L;

    protected String[] ID;

    protected LocalState[] globalState;
    
    private long posStartTime = 0;
    
    private long posEndTime = Long.MAX_VALUE;
    
    private long defStartTime = 0;
    
    private long defEndTime = Long.MAX_VALUE;

    public AbstractLatticeIDNode(LocalState[] gs, String[] s, long epsilon) {
        ID = s;
        globalState = new LocalState[gs.length];
        for (int i = 0; i < gs.length; i++) {
            globalState[i] = gs[i];
        }
        if(gs[0].getEndTime() != 0) {
        	for (int i = 0; i < gs.length; i++) {
        		posStartTime = Math.max(posStartTime, gs[i].getStartTime() - epsilon);
        		posEndTime = Math.min(posEndTime, gs[i].getEndTime() + epsilon);
        		defStartTime = Math.max(defStartTime, gs[i].getStartTime() + epsilon);
        		defEndTime = Math.min(defEndTime, gs[i].getEndTime() - epsilon);
            }
        	defEndTime = Math.max(0, defEndTime);
        	boolean firstNode = true;
        	for (int i = 0; i < gs.length; i++) {
        		if(gs[i].getStartTime() != 0) {
        			firstNode = false;
        			break;
        		}
        	}
        	if(firstNode == true) {
        		posStartTime = defStartTime = 0;
        	}
        }
    }

    public String[] getID() {
        return ID;
    }

    public LocalState[] getGlobalState() {
        return globalState;
    }
    
    public int hashCode() {
        return (StringUtils.join(ID)).hashCode();
    }
    
    public boolean equals(AbstractLatticeIDNode node) {
        return hashCode() == node.hashCode();
    }

	public long getPosStartTime() {
		return posStartTime;
	}

	public void setPosStartTime(long posStartTime) {
		this.posStartTime = posStartTime;
	}

	public long getPosEndTime() {
		return posEndTime;
	}

	public void setPosEndTime(long posEndTime) {
		this.posEndTime = posEndTime;
	}

	public long getDefStartTime() {
		return defStartTime;
	}

	public void setDefStartTime(long defStartTime) {
		this.defStartTime = defStartTime;
	}

	public long getDefEndTime() {
		return defEndTime;
	}

	public void setDefEndTime(long defEndTime) {
		this.defEndTime = defEndTime;
	}
}
