package net.sourceforge.mipa;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Calendar;

import net.sourceforge.mipa.application.AbstractApplication;
import net.sourceforge.mipa.application.OnFire;
import net.sourceforge.mipa.application.ResultCallback;


public class NewApplication extends AbstractApplication {
	
    /**
     * <code>Application</code> construction.
     * 
     * @param fileName
     *            a file contains predicate
     */
    public NewApplication() {
        super();
    }
    
    public void run() {
    	/* lookup the robots from the naming server */
    	boolean isStart = false;
    	while(true) {
        	int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	        if(hour >= 8 && hour < 18 && isStart == false) {
	        	isStart = true;
	        	/* set the routing and coordination mechanisms and start the robots */
	        	ResultCallback onFire;
	        	//ResultCallback onSmog;
				try {
					onFire = new OnFire();
					register("config/predicate/predicate_scp.xml", onFire, "fire");
					//register("config/predicate/predicate_oga.xml", onFire, "fire");
					//register("config/predicate/predicate_sursequence.xml", onFire, "fire");
		        	//register("config/predicate/predicate_ctl_eu.xml", onFire, "fire");
		        	
					//onSmog = new OnCoordination();
		        	//register("config/predicate/smog.xml", onSmog, "smog");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        if(hour >= 18 && isStart == true) {
	        	isStart = false;
	        	unregister("fire");
	        	//unregister("smog");
	        	/* stop the robots */
	        }
    	
        }
    }
    
    public static void main(String[] args) {
    	NewApplication app = new NewApplication();
    	app.run();
    }
}
