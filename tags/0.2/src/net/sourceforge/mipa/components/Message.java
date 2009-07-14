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

import net.sourceforge.mipa.predicatedetection.VectorClock;

/**
 * message class.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class Message implements Serializable {

    private static final long serialVersionUID = -8694974992774436096L;
    
    /** message type */
    private MessageType type;
    
    /** message sender */
    private String senderID;
    
    /** message receiver */
    private String receiverID;
    
    /** when the message arrives at dispatcher */
    private long reachTime;
    
    /** dispatch time set by dispatcher */
    private long dispatchTime;
    
    /** message content */
    private MessageContent content;
    
    /** vector clock of message */
    private VectorClock timestamp;

    /**
     * @param type the type to set
     */
    public void setType(MessageType type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * @param senderID the senderID to set
     */
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    /**
     * @return the senderID
     */
    public String getSenderID() {
        return senderID;
    }

    /**
     * @param receiverID the receiverID to set
     */
    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    /**
     * @return the receiverID
     */
    public String getReceiverID() {
        return receiverID;
    }

    /**
     * @param reachTime the reachTime to set
     */
    public void setReachTime(long reachTime) {
        this.reachTime = reachTime;
    }

    /**
     * @return the reachTime
     */
    public long getReachTime() {
        return reachTime;
    }

    /**
     * @param dispatchTime the dispatchTime to set
     */
    public void setDispatchTime(long dispatchTime) {
        this.dispatchTime = dispatchTime;
    }

    /**
     * @return the dispatchTime
     */
    public long getDispatchTime() {
        return dispatchTime;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(VectorClock timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the timestamp
     */
    public VectorClock getTimestamp() {
        return timestamp;
    }

    /**
     * @param content the content to set
     */
    public void setContent(MessageContent content) {
        this.content = content;
    }

    /**
     * @return the content
     */
    public MessageContent getContent() {
        return content;
    }
}
