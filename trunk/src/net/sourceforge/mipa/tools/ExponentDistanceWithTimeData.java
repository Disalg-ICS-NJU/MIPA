package net.sourceforge.mipa.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * 
 * @author yiling Yang <csylyang@gmail.com>
 * 
 */
public class ExponentDistanceWithTimeData {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        ArrayList<String> tags = new ArrayList<String>();

        int[] index = {
        		
        		};
        for(int k = 0; k<index.length;k++) {	
	        String outputFile = "data/distance_"+index[k];
	        
	        int timeGap = 1;
	        PrintWriter output = null;;
	        double intervalLambda = 1.0 / 10000;
	        double intervalGapLambda = 1.0 / 5000;
	
	        double falseValue = 100.0;
	        double trueValue = 1000.0;
	        long currentValue = 0;
	        try {
	            output = new PrintWriter(outputFile);;
	            output.println(timeGap);;
	            output.println(falseValue+" 0");
	            
	            
	//            int count = 1;
	            int count = 120; 
	            for(int i = 0; i < count; i++) {
	                double gapLength = ExponentDistribution.exponent(intervalGapLambda);
	                long n = Math.round(gapLength);
	                for(int j = 0; j < (int) (n*1.0/1000); j++) 
	                    output.println(falseValue+ " "+ (currentValue + (j+1)*1000));
	                if (gapLength%1000 != 0) {
	                	output.println(falseValue+ " "+ (currentValue+n));
					}
	                currentValue += n;
	                
	                double intervalLength = ExponentDistribution.exponent(intervalLambda);
	                n = Math.round(intervalLength);
	                for(int j = 0; j < (int) (n*1.0/1000); j++) 
	                    output.println(trueValue+ " "+ (currentValue + (j+1)*1000));
	                if (intervalLength%1000 != 0) {
	                	output.println(trueValue+ " "+ (currentValue+n));
					}
	                currentValue += n;
	//                double intervalLength = ExponentDistribution.exponent(intervalLambda);
	//                for(int j = 0; j < (int) intervalLength; j++) 
	//                    output.println(trueValue);
	            }
	            output.flush();
	            output.close();
	            BufferedReader bReader = new BufferedReader(new FileReader(outputFile));
	            String string = bReader.readLine();
	            string = bReader.readLine();
	            int last = -1;
	            int falseNum = 0;
	            int trueNum = 0;
	            while(string!=null) { 
	            	double v = Double.valueOf(string.split(" ")[0]);
	            	if(v<500.0) {
	            		falseNum++;
	            	}
	            	else {
	            		trueNum++;
	            	}
	            	int cur = Integer.valueOf(string.split(" ")[1]);
	            	if(cur<=last) {
	            		System.out.println("Illegal data!");
	            	}
	            	string = bReader.readLine();
	            }
	            System.out.println(index[k]+" F: "+falseNum+" T:¡¡"+ trueNum+ "Total: "+(falseNum+trueNum));
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
        }
    }

    
//    public static void main(String[] args) throws IOException {
//        // TODO Auto-generated method stub
//        ArrayList<String> tags = new ArrayList<String>();
//
//        int[] index = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28};
//        for(int k = 0; k<index.length;k++) {	
//	        String outputFile = "data/all data/distance_"+index[k];
//	        
//	            BufferedReader bReader = new BufferedReader(new FileReader(outputFile));
//	            String string = bReader.readLine();
//	            string = bReader.readLine();
//	            int last = -1;
//	            int falseNum = 0;
//	            int trueNum = 0;
//	            while(string!=null) { 
//	            	double v = Double.valueOf(string.split(" ")[0]);
//	            	if(v<500.0) {
//	            		falseNum++;
//	            	}
//	            	else {
//	            		trueNum++;
//	            	}
//	            	int cur = Integer.valueOf(string.split(" ")[1]);
//	            	if(cur<=last) {
//	            		System.out.println("Illegal data!");
//	            	}
//	            	string = bReader.readLine();
//	            }
//	            System.out.println(index[k]+" F: "+falseNum+" T:¡¡"+ trueNum+ "Total: "+(falseNum+trueNum));
//        }
//    }
}
