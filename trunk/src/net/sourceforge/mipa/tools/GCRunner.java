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
package net.sourceforge.mipa.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class GCRunner implements Runnable {
	PrintWriter out = null;
	
	public GCRunner() {
		// TODO Auto-generated method stub
	}
	
	public GCRunner(PrintWriter out) {
		// TODO Auto-generated method stub
		this.out = out;
	}
	
    public void run() {
    	//int count = 14400;
        int period = 0;
        while(true) {
        	//count--;
            System.gc();
            if(out != null) {
	            long total = Runtime.getRuntime().totalMemory();
	            long free = Runtime.getRuntime().freeMemory();
	            long mem = (total-free)/1024;
	            out.println("Total-free: "+mem+"KB");
	        	period++;
	        	if(period >= 5) {
	        		out.flush();
	        		period = 0;
	        	}
            }
            try {
                Thread.sleep(5000);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
