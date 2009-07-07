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

import java.io.Serializable;

import net.sourceforge.mipa.predicatedetection.LocalPredicate;

/**
 * Condition of ECA mechanism.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public abstract class Condition implements Serializable {

    private static final long serialVersionUID = -5349061659963216502L;

    /** local predicate which <code>Condition</code> should concern */
    protected LocalPredicate localPredicate;
    /**
     * called by DataSource for notifying event change.
     * 
     * @param eventName
     *            event name
     * @param values
     *            event values
     * @see DataSource
     */
    public abstract void update(String eventName, String[] values);

    /**
     * notify the action of ECA mechanism.
     * 
     * @param eventName
     *            event name
     * @param value
     *            event value
     */
    public abstract void notifyListener(String eventName, String value);
    
    /**
     * calculate the local predicate value.
     * 
     * @param eventName
     *            event name
     * @param values
     *            event values
     * @return local predicate result
     */
    protected boolean assign(String eventName, String[] values) {
        
        //FIXME should calculate for every atom which in local predicate
        
        
        String operator = localPredicate.getOperator();
        String name = localPredicate.getName();
        String value = localPredicate.getValue();
        String valueType = localPredicate.getValueType();
        
        assert (eventName.equals(name));

        // FIXME This part is terribly coded.
        if (valueType.equals("String") == true) {
            // String operators
            if (operator.equals("contain") == true) {
                for (int i = 0; i < values.length; i++)
                    if (value.equals(values[i]) == true)
                        return true;
            } else if(operator.equals("not-contain") == true) {
                for(int i = 0; i < values.length; i++)
                    if(value.equals(values[i]) == true)
                        return false;
                return true;
            } else {
                System.out.println("The operator of String has not been defined.");
            }
            
        } else if (localPredicate.getValueType().equals("Double") == true) {
            // Float operators
            double sensorValue = Double.parseDouble(values[0]);
            double threshold = Double.parseDouble(value);
            
            if(operator.equals("great-than") == true) {
                if(sensorValue > threshold) return true;
            } else if(operator.equals("equals") == true) {
                if(sensorValue == threshold) return true;
            } else if(operator.equals("less-than") == true) {
                if(sensorValue < threshold) return true;
            } else {
                System.out.println("The operator of Float has not been defined.");
            }
        } else {
            System.out.println("value type is undefined.");
        }

        return false;
    }
}
