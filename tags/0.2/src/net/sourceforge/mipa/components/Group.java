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
package net.sourceforge.mipa.components;

import java.util.ArrayList;

import net.sourceforge.mipa.predicatedetection.PredicateType;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class Group {
    private String groupID;

    private int numberOfNormalProcesses;

    private String checkerName;

    private int numberOfFinishedNormalProcesses;

    private PredicateType predicateType;
    
    private ArrayList<String> normalProcesses;

    public Group(String groupID, String checkerName,
                 int numberOfNormalProcesses, PredicateType type) {
        this.groupID = groupID;
        this.checkerName = checkerName;
        this.numberOfFinishedNormalProcesses = 0;
        this.numberOfNormalProcesses = numberOfNormalProcesses;
        this.predicateType = type;
        this.normalProcesses = new ArrayList<String>();
    }

    /**
     * @param groupID
     *            the groupID to set
     */
    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    /**
     * @return the groupID
     */
    public String getGroupID() {
        return groupID;
    }

    /**
     * @param checkerName
     *            the checkerName to set
     */
    public void setCheckerName(String checkerName) {
        this.checkerName = checkerName;
    }

    /**
     * @return the checkerName
     */
    public String getCheckerName() {
        return checkerName;
    }

    /**
     * @param numberOfNormalProcesses
     *            the numberOfNormalProcesses to set
     */
    public void setNumberOfNormalProcesses(int numberOfNormalProcesses) {
        this.numberOfNormalProcesses = numberOfNormalProcesses;
    }

    /**
     * @return the numberOfNormalProcesses
     */
    public int getNumberOfNormalProcesses() {
        return numberOfNormalProcesses;
    }

    /**
     * @param numberOfFinishedNormalProcesses
     *            the numberOfFinishedNormalProcesses to set
     */
    public void setNumberOfFinishedNormalProcesses(
                                                   int numberOfFinishedNormalProcesses) {
        this.numberOfFinishedNormalProcesses = numberOfFinishedNormalProcesses;
    }

    /**
     * @return the numberOfFinishedNormalProcesses
     */
    public int getNumberOfFinishedNormalProcesses() {
        return numberOfFinishedNormalProcesses;
    }

    /**
     * @param predicateType
     *            the predicateType to set
     */
    public void setPredicateType(PredicateType predicateType) {
        this.predicateType = predicateType;
    }

    /**
     * @return the predicateType
     */
    public PredicateType getPredicateType() {
        return predicateType;
    }
    
    public void addNormalProcess(String name) {
        normalProcesses.add(name);
    }
    
    public String[] getNormalProcesses() {
        String[] list = new String[normalProcesses.size()];
        normalProcesses.toArray(list);
        return list;
    }
}
