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
public class EmptyCondition implements Condition {

    /** reference to action of ECA */
    private Listener action;

    /** local predicate which <code>Condition</code> should concern */
    private LocalPredicate localPredicate;

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

    /**
     * calculate the local predicate value.
     * 
     * @param eventName
     *            event name
     * @param values
     *            event values
     * @return local predicate result
     */
    private boolean assign(String eventName, String[] values) {
        
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
            
        } else if (localPredicate.getValueType().equals("Float") == true) {
            // Float operators
            float sensorValue = Float.parseFloat(values[0]);
            float threshold = Float.parseFloat(value);
            
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
