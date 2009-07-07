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

import net.sourceforge.mipa.predicatedetection.LocalPredicate;

/**
 * report everything condition.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class EmptyCondition extends Condition {

    private static final long serialVersionUID = 2929083434758138845L;

    /** reference to action of ECA */
    private Listener action;

    public EmptyCondition(Listener action, LocalPredicate localPredicate) {
        this.action = action;
        this.localPredicate = localPredicate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.mipa.eca.Condition#notifyListener(java.lang.String)
     */
    @Override
    public void notifyListener(String eventName, String value) {
        action.update(eventName, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.mipa.eca.Condition#update(java.lang.String,
     * java.lang.String)
     */
    @Override
    public synchronized void update(String eventName, String[] values) {
        boolean result = assign(eventName, values);

        notifyListener(eventName, String.valueOf(result));
    }
}
