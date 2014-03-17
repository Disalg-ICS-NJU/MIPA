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

import java.io.Serializable;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class TimedMessageContent extends MessageContent {

	private static final long serialVersionUID = 3007128772811018095L;
    
    private long startTime;
    
    private long endTime;
    
    private boolean localPredicate;

    public TimedMessageContent(boolean localPredicate, long startTime, long endTime) {
		// TODO Auto-generated constructor stub
    	this.setLocalPredicate(localPredicate);
    	this.setStartTime(startTime);
    	this.setEndTime(endTime);
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

	public boolean getLocalPredicate() {
		return localPredicate;
	}

	public void setLocalPredicate(boolean localPredicate) {
		this.localPredicate = localPredicate;
	}    
}
