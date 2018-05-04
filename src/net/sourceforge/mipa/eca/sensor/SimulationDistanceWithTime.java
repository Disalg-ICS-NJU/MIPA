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
package net.sourceforge.mipa.eca.sensor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Array;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class SimulationDistanceWithTime implements Sensor {

    private BufferedReader source;
    
    private long gap;
    
    private boolean stoped;
    
    //private long startTime;
    
    private boolean first=true;
    
    private long drift;
    
    //private long distance;
    
    public SimulationDistanceWithTime(Object args) {
        try {
            source = new BufferedReader(new FileReader((String) Array.get(args, 0)));
            gap = Long.parseLong(source.readLine());
            //startTime = System.currentTimeMillis();
        } catch(Exception e) {
            e.printStackTrace();
        }
        stoped = false;
    }
    /* (non-Javadoc)
     * @see net.sourceforge.mipa.eca.sensor.Sensor#getData()
     */
    @Override
    public String[] getData() {
        // TODO Auto-generated method stub
        if(stoped == true) return null;

        String value = null;
        try {
            value = source.readLine();
//            if(first == true) {
//        		long currentTime = System.currentTimeMillis();
//        		//long distance = currentTime - startTime;
//        		String[] vStrings = value.split("\\s+");
//        		if(vStrings.length > 1) {
//        			long sourceTime = Long.valueOf(value.split("\\s+")[1]);
//        			drift = currentTime - sourceTime;
//        			vStrings[1] = String.valueOf(currentTime);
//        			value = vStrings[0]+ " "+ vStrings[1];
//        		}
//        		first = false;
//        	}
//            else {
//            	String[] vStrings = value.split("\\s+");
//        		if(vStrings.length > 1) {
//        			long sourceTime = Long.valueOf(value.split("\\s+")[1]);
//        			vStrings[1] = String.valueOf(sourceTime+drift);
//        			value = vStrings[0]+ " "+ vStrings[1];
//        		}
//            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        if(value == null) {
            stoped = true;
            try {
                source.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        
        String[] results = value.split("\\s+");
        return results;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mipa.eca.sensor.Sensor#getGap()
     */
    @Override
    public long getGap() {
        // TODO Auto-generated method stub
        return gap;
    }

}
