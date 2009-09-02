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
/**
 * simulation temperature sensor.
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class SimulationTemperature implements Sensor {

	/** data source */
	private BufferedReader source;

	/** time gap between continuous data generations. */
	private long gap;
	
	private boolean stoped;
	
	public SimulationTemperature(String fileName) {
		try {
			source = new BufferedReader(new FileReader(fileName));
			gap = Long.parseLong(source.readLine());
		} catch(Exception e) {
			e.printStackTrace();
		}
		stoped = false;
	}
	
	public long getGap() {
	    return gap;
	}
	
	public String[] getData() {
	    
	    if(stoped == true) return null;

	    String value = null;
	        
	    try {
	        value = source.readLine();
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
	    
	    String[] results = {value};
	    return results;
	}
}