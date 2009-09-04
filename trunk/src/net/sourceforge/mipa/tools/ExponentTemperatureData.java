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

import java.io.PrintWriter;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class ExponentTemperatureData {

    public static void main(String[] args) {
        int timeGap = 1000;
        
        String outputFile = "data/temperature_temp";
        PrintWriter output = null;
        double intervalLambda = 0.1;
        double intervalGapLambda = 0.1;
        
        double threshold = 20;
        // The two values should be auto adaptive.
        double falseValue = 19;
        double trueValue = 21;
        
        try {
            output = new PrintWriter(outputFile);
            output.println(timeGap);
            
            int count = 100;
            for(int i = 0; i < count; i++) {
                double gapLength = ExponentDistribution.exponent(intervalGapLambda);
                for(int j = 0; j < (int) gapLength; j++) 
                    output.println(falseValue);
                double intervalLength = ExponentDistribution.exponent(intervalLambda);
                for(int j = 0; j < (int) intervalLength; j++) 
                    output.println(trueValue);
            }
            output.flush();
            output.close();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
