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
import java.util.ArrayList;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class ExponentRfidData {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        ArrayList<String> tags = new ArrayList<String>();
        
        int timeGap = 1000;
        String concernedTag = "tag_00001";
        String outputFile = "data/RFID_temp";
        PrintWriter output = null;
        double intervalLambda = 0.1;
        double intervalGapLambda = 0.1;
        
        
        tags.add(concernedTag);
        
        try {
            output = new PrintWriter(outputFile);
            output.println(timeGap);
            output.println(tags.size());
            for(int i = 0; i < tags.size(); i++) {
                output.println(tags.get(i));
            }
            for(int i = 0; i < tags.size(); i++) {
                int count = 100;
                String stream = "";
                for(int j = 0; j < count; j++) {
                    double gapLength = ExponentDistribution.exponent(intervalGapLambda);
                    for(int k = 0; k < (int) gapLength; k++) {
                        stream += "0";
                    }
                    double intervalLength = ExponentDistribution.exponent(intervalLambda);
                    for(int k = 0; k < (int) intervalLength; k++) {
                        stream += "1";
                    }
                }
                output.println(stream);
            }
            output.flush();
            output.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
