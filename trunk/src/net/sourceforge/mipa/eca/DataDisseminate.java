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

import java.util.ArrayList;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class DataDisseminate {
    
    private DataSource dataSource;
    
    private int pushFreq;
    
    private int count;
    
    private ArrayList<String[]> dataPool;
    
    public DataDisseminate(DataSource dataSource, int freq) {
        this.dataSource = dataSource;
        assert(freq > 0);
        pushFreq = freq;
        count = 0;
        dataPool = new ArrayList<String[]>();
    }
    
    public void update(String sensorAgentName, String[] data) {
        count++;
        dataPool.add(data);
        
        if(count >= pushFreq) {
            while(dataPool.size() > 0) {
                try {
                    dataSource.update(sensorAgentName, dataPool.remove(0));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            //assert(dataPool.size() == 0);
            count = 0;
        }
    }
}
