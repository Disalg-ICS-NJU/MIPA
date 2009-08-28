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
package net.sourceforge.mipa.predicatedetection.oga;

import net.sourceforge.mipa.predicatedetection.VectorClock;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class OGAVectorClock extends VectorClock {

    private static final long serialVersionUID = 8524644883721946851L;
    
    public OGAVectorClock(int number) {
        super(number);
    }
    
    public OGAVectorClock(VectorClock clock) {
        super(clock);
    }

    @Override
    public void increment(int id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean notLessThan(VectorClock timestamp) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void update(VectorClock timestamp) {
        // TODO Auto-generated method stub
        
    }

}
