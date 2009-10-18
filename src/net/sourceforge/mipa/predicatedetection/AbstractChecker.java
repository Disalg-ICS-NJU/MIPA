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
package net.sourceforge.mipa.predicatedetection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.mipa.ResultCallback;
import net.sourceforge.mipa.components.Communication;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public abstract class AbstractChecker implements Serializable, Communication {

    private static final long serialVersionUID = -5023931031473945453L;
    
    protected ResultCallback application;
    
    protected String name;
    
    protected String[] children;
    
    protected Map<String, Integer> nameToID;

    public AbstractChecker(ResultCallback application, 
                            String checkerName, 
                            String[] children) {
        this.application = application;
        this.name = checkerName;
        this.children = children;
        
        nameToID = new HashMap<String, Integer>();
        for(int i = 0; i < children.length; i++) {
            nameToID.put(children[i], new Integer(i));
        }
    }
}
