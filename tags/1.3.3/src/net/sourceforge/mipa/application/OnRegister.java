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

public class OnRegister extends UnicastRemoteObject implements ResultCallback {

	private static final long serialVersionUID = -8919878033198402390L;
	private boolean flag = false;

	public OnRegister() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void callback(String value) throws RemoteException,
			MalformedURLException, NotBoundException {
		
		if (flag == false && value.equals("true")) {
			System.out.println("The predicate is true now!");
		}
	}
}
