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

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>ContextMapping</code> manages mapping between local predicate and
 * context.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class ContextMapping {
    /** mapping event name to entity id */
    private Map<String, String> map;

    public ContextMapping() {
        map = new HashMap<String, String>();

    }

    /**
     * maps eventName to entityId.
     * 
     * @param eventName
     *            event name
     * @param entityId
     *            entity id
     */
    public synchronized void map(String eventName, String entityId)
                                                                   throws EventNameBoundTwiceException {
        if (map.containsKey(eventName) == true)
            throw new EventNameBoundTwiceException(eventName);

        map.put(eventName, entityId);
    }

    /**
     * gets mapping result.
     * 
     * @param eventName
     *            event name
     * @return mapping result(a <code>String</code>)
     * @throws EventNameNotFoundException
     */
    public String getMapping(String eventName)
                                              throws EventNameNotFoundException {
        if (map.containsKey(eventName) == false)
            throw new EventNameNotFoundException(eventName);
        return map.get(eventName);
    }
}