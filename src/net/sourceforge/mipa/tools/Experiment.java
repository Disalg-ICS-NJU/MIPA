package net.sourceforge.mipa.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Experiment {

    static BufferedWriter out;
    static BufferedWriter outWindowedLattice;
    static BufferedWriter outUpdateNumber;
    static BufferedWriter outOriLattice;

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            long mipa = 0;
            int surfNum = 0;
            long mipaTime = 0;
            long uppaalTime = 0;
            int num = 0;
            int latticeSize = 0;
            long uppaal = 0;
            
            BufferedReader source = new BufferedReader(new FileReader(
                    "log/heap-MIPA.log"));
            String s = source.readLine();
            while (s != null) {
            	num++;
                String[] string = s.split(" ")[1].split("K");
                mipa += Integer.valueOf(string[0]);
                s = source.readLine();
            } 
            System.out.println("MIPA space cost: " +  mipa*1.0/num);
            
            num = 0;
            source = new BufferedReader(new FileReader(
                    "log/mappedLatticeNode.log"));
            s = source.readLine();
            while (s != null) {
            	num++;
                String[] string = s.split(" ");
                surfNum += Integer.valueOf(string[2]);
                latticeSize = Integer.valueOf(string[3]);
                s = source.readLine();
            }
            System.out.println("Active surface size: "+ surfNum*1.0/num);
            System.out.println("Lattice size: "+ latticeSize);
           
            num = 0;
            source = new BufferedReader(new FileReader(
            "log/TCTL_Time.log"));
            s = source.readLine();
            while (s != null) {
            	num++;
                String[] string = s.split(" ");
                mipaTime += Integer.valueOf(string[0]);
                uppaalTime += Integer.valueOf(string[1]);
                s = source.readLine();
            }
            System.out.println("mipaTime: "+ mipaTime*1.0/num);
            System.out.println("uppaalTime: "+ uppaalTime*1.0/num);
            
            num = 0;
            source = new BufferedReader(new FileReader(
            "log/DataCollector01.tsv"));
            s = source.readLine();
            s = source.readLine();
            while (s != null) {
                String[] string = s.trim().split("\"");
                if(string[3].equals(" ")) {
                	s = source.readLine();
                	continue;
                }
                num++;
                uppaal += Integer.valueOf(string[3]);
                s = source.readLine();
            }
            System.out.println("uppaal space cost: "+ uppaal*1.0/num+ " "+num);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
