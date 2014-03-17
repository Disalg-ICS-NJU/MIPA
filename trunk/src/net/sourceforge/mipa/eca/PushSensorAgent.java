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

import static config.Debug.DEBUG;

import org.apache.log4j.Logger;

import net.sourceforge.mipa.eca.preprocessing.DataDisseminate;
import net.sourceforge.mipa.eca.sensor.Sensor;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class PushSensorAgent extends SensorAgent {
    
    /** sensor class */
    private Sensor sensor;
    
    /** time gap between two data generations. */
    private long gap;
    
    protected final static int heartBeat = 1;
    private long time;
    private long lastTime;
    private boolean first = true;

    private String[] values;
    
    //private static Logger logger = Logger.getLogger(PushSensorAgent.class);

    public PushSensorAgent(DataDisseminate dataDisseminate, 
                             String name,
                             String valueType, 
                             Sensor sensor) {
        
        super(dataDisseminate, name, valueType);
        this.sensor = sensor;
        gap = sensor.getGap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.mipa.eca.SensorAgent#generateData()
     */
    @Override
    public void generateData() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.mipa.eca.SensorAgent#getData()
     */
    @Override
    public String getData() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            while(true) {
            	values = sensor.getData();
            	if(values != null) {
	            	if(values.length > 1) {
	            		time = Long.valueOf(values[1]);
//		            	long currentTime = System.currentTimeMillis();
//		            	if(currentTime >= time) {
//		            		dataDisseminate.update(this.name, values);
//		            	}
//		            	else {
//		            		long distance = time - currentTime;
//		            		Thread.sleep(distance);
//		            		dataDisseminate.update(this.name, values);
//		            	}
	            		if(first == true) {
	            			lastTime = time;
	            			dataDisseminate.update(this.name, values);
	            			first = false;
	            		}
	            		else {
	            			long distance = time - lastTime;
	            			lastTime = time;
	            			Thread.sleep(distance);
		            		dataDisseminate.update(this.name, values);
	            		}
	            	}
	            	else {
	            		Thread.sleep(gap);
	            		dataDisseminate.update(this.name, values);
	            	}
                } else {
                	System.out.println("Sensor " + name + " Stopped!");
                    if(DEBUG) {
                        System.out.println("Sensor " + name + " Stopped!");
                        //logger.info("Sensor " + name + " Stopped!");
                    }
                    return;
                }	                
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
