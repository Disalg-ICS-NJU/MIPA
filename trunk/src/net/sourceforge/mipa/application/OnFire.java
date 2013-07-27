package net.sourceforge.mipa.application;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;

import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.Naming;

public class OnFire extends UnicastRemoteObject implements ResultCallback {

	private static final long serialVersionUID = -8919878033198402390L;

	public OnFire() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void callback(String value) throws RemoteException,
			MalformedURLException, NotBoundException {
		if (value.equals("true")) {
			//Naming server = MIPAResource.getNamingServer();
			//RobotsManager robotsManager = (RobotsManager) server
			//		.lookup("RobotsManager");
			//String locationOfFire = robotsManager.getRobots().get(0)
			//		.getLocation();
			String locationOfFire = "813";
			
			/* launch the fire alarm */

			//WorkersManager workersManager = (WorkersManager) server
			//		.lookup("WorkersManager");
			//ArrayList<User> workersArrayList = workersManager.getWorkers();
			ArrayList<User> workersArrayList = new ArrayList<User>();
			workersArrayList.add(new User());
			Iterator<User> iterator = workersArrayList.iterator();
			while (iterator.hasNext()) {
				User user = (User) iterator.next();
				String locationOfWorker = user.getLocation();
				Catalog type = user.getType();
				switch (type) {
				case FireMan:
					user.notify("Please fight the fire in " + locationOfFire);
					break;
				case Worker:
					if (locationOfWorker.equals(locationOfFire)) {
						user.notify("Escape£¡ There is a fire in your workshop!");
					} else {
						user.notify("Attention! There is a fire in "
								+ locationOfFire);
					}
					break;
				default:
					user.notify("There is a fire in " + locationOfFire);
					break;
				}
			}
		}
	}

}
