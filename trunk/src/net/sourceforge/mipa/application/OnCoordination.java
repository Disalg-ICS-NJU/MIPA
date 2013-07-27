package net.sourceforge.mipa.application;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.naming.Naming;

public class OnCoordination extends UnicastRemoteObject implements ResultCallback{

	private static final long serialVersionUID = -1776442542104494930L;

	public OnCoordination() throws RemoteException {
		super();
	}

	@Override
	public void callback(String value) throws RemoteException, MalformedURLException, NotBoundException {
		if (value.equals("true")) {
			Naming server = MIPAResource.getNamingServer();
			RobotsManager robotsManager = (RobotsManager) server
					.lookup("RobotsManager");
			robotsManager.recovery();
		}
	}

}
