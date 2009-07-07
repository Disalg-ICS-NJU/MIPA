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

/**
 * The <code>LocalPredicate</code> class represents local predicate.
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class LocalPredicate implements Serializable {
    //TODO implement completely predicate
    
    private static final long serialVersionUID = 1032369328241304696L;

    private String operator;
    
    private String name;
    
    private String value;
    
    private String valueType;

    /**
     * @param operator the operator to set
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * @return the operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * @param valueType the valueType to set
     */
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    /**
     * @return the valueType
     */
    public String getValueType() {
        return valueType;
    }
    
    /*
    //FIXME this part is terribly coded.
    public boolean value(String value) {
        if(valueType.equals("String") == true) {
            if("contain".equals(operator) == true) {
                if(this.value.equals(value) == true) return true;
                else return false;
            } else if("not-contain".equals(operator) == true) {
                if(this.value.equals(value) == false) return true;
                else return false;
            } else {
                System.out.println("The operation of String has not been defined.");
            }
        } else if(valueType.equals("Float") == true) {
            float valueFloat = Float.parseFloat(value);
            float threshold = Float.parseFloat(this.value);
            if("equal".equals(operator) == true) {
            
            } else if("great-than".equals(operator) == true) {
            
            } else if("less-than".equals(operator) == true) {
            
            } else {
                System.out.println("The operation of Float has not been defined.");
            }
        } else {
            System.out.println("value type undefined.");
        }
        return true;
    }
    */
}
