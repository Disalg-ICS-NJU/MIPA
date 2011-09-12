package net.sourceforge.mipa.tools;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * 
 * @author yiling Yang <csylyang@gmail.com>
 * 
 */
public class ExponentLightRFIDTemperatureData {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        ArrayList<String> tags = new ArrayList<String>();

        int timeGap = 200;
        String concernedTag = "tag_00001";
        String outputFile_1_1 = "data/RFID_2";
        String outputFile_1_2 = "data/RFID_3";
        String outputFile_2_1 = "data/light_2";
        String outputFile_2_2 = "data/light_3";
//        String outputFile_3_1 = "data/temperature";
//        String outputFile_3_2 = "data/temperature_1";
        PrintWriter output_1_1 = null;
        PrintWriter output_1_2 = null;
        PrintWriter output_2_1 = null;
        PrintWriter output_2_2 = null;
  //      PrintWriter output_3_1 = null;
 //       PrintWriter output_3_2 = null;
        double intervalLambda = 1.0 / 25;
        double intervalGapLambda = 1.0 / 5;

        double falseValue = 100.0;
        double trueValue = 1010.0;
//        double temperatureFalseValue = 25;
 //       double temperatureTrueValue = 35;

        tags.add(concernedTag);

        try {
            output_1_1 = new PrintWriter(outputFile_1_1);
            output_1_2 = new PrintWriter(outputFile_1_2);
            output_2_1 = new PrintWriter(outputFile_2_1);
            output_2_2 = new PrintWriter(outputFile_2_2);
 //           output_3_1 = new PrintWriter(outputFile_3_1);
//            output_3_2 = new PrintWriter(outputFile_3_2);
            output_1_1.println(timeGap);
            output_1_1.println(tags.size());
            output_1_2.println(timeGap);
            output_1_2.println(tags.size());
            output_2_1.println(timeGap);
            output_2_2.println(timeGap);
 //           output_3_1.println(timeGap);
 //           output_3_2.println(timeGap);

            for (int i = 0; i < tags.size(); i++) {
                output_1_1.println(tags.get(i));
                output_1_2.println(tags.get(i));
            }
            for (int i = 0; i < tags.size(); i++) {
                int count = 10;
                String stream = "";
                for (int j = 0; j < count; j++) {
                    double gapLength = ExponentDistribution
                            .exponent(intervalGapLambda);
                    if(((int)gapLength) == 0)
                        gapLength = 1;
                    for (int k = 0; k < (int) gapLength; k++) {
                        stream += "0";
                        output_2_1.println(falseValue);
  //                      output_3_1.println(temperatureFalseValue);
                    }
                    double intervalLength = ExponentDistribution
                            .exponent(intervalLambda);
                    if(((int)intervalLength) == 0)
                        intervalLength = 1;
                    for (int k = 0; k < (int) intervalLength; k++) {
                        stream += "1";
                        output_2_1.println(trueValue);
  //                      output_3_1.println(temperatureTrueValue);
                    }
                    System.out.println(j);
                }
                output_1_1.println(stream);
                output_2_1.flush();
                output_2_1.close();
  //              output_3_1.flush();
  //              output_3_1.close();

                stream = "";
                for (int j = 0; j < count; j++) {
                    double intervalLength = ExponentDistribution
                            .exponent(intervalGapLambda);
                    if(((int)intervalLength) == 0)
                        intervalLength = 1;
                    for (int k = 0; k < (int) intervalLength; k++) {
                        stream += "1";
                        output_2_2.println(trueValue);
 //                       output_3_2.println(temperatureTrueValue);
                    }
                    double gapLength = ExponentDistribution
                            .exponent(intervalLambda);
                    if(((int)gapLength) == 0)
                        gapLength = 1;
                    for (int k = 0; k < (int) gapLength; k++) {
                        stream += "0";
                        output_2_2.println(falseValue);
 //                       output_3_2.println(temperatureFalseValue);
                    }
                }
                output_1_2.println(stream);
                output_2_2.flush();
                output_2_2.close();
  //              output_3_2.flush();
  //              output_3_2.close();
            }
            output_1_1.flush();
            output_1_1.close();
            output_1_2.flush();
            output_1_2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
