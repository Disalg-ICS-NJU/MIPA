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


import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import net.sourceforge.mipa.predicatedetection.State;
import net.sourceforge.mipa.predicatedetection.lattice.AbstractLatticeIDNode;
import net.sourceforge.mipa.predicatedetection.lattice.LocalState;

/**
 * 
 * @author Yiling Yang <csylyang@gmail.com>
 *
 */
public class TCTLLatticeIDNode extends AbstractLatticeIDNode {

    /**
     * 
     */
    private static final long serialVersionUID = -2954674513455562143L;

    /**record whether the node has been checked**/
    private boolean visited;
    
    /**record whether the node is the tail of the lattice**/
    private boolean tailFlag;
    
    private String satisfiedPredicates;


    public TCTLLatticeIDNode(LocalState[] gs, String[] s, long epsilon) {
        super(gs, s, epsilon);
        // TODO Auto-generated constructor stub
        visited = false;
        tailFlag = false;
        satisfiedPredicates = "";
    }

    public boolean getVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
    
    public boolean getTailFlag() {
        return tailFlag;
    }

    public void setTailFlag(boolean tailFlag) {
        this.tailFlag = tailFlag;
    }

    public String getSatisfiedPredicates() {
        return satisfiedPredicates;
    }

    public void addSatisfiedPredicates(char satisfiedPredicates) {
        if(!this.satisfiedPredicates.contains(String.valueOf(satisfiedPredicates))) {
            this.satisfiedPredicates += " "+satisfiedPredicates;
            this.satisfiedPredicates = this.satisfiedPredicates.trim();
        }
    }

    public void setSatisfiedPredicates(String satisfiedPredicates) {
        this.satisfiedPredicates = satisfiedPredicates;
    }
    
    public int hashCode() {
        return (StringUtils.join(ID)).hashCode();
    }
    
    public boolean equals(AbstractLatticeIDNode node) {
        return hashCode() == node.hashCode();
    }
}
