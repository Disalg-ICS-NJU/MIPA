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

package net.sourceforge.mipa.components;

import java.rmi.Remote;

/**
 * This interface provides system Naming interface for others modules in mipa system.
 * 
 * @author Jianping YU <jianp.yue@gmail.com>
 */
public class Naming {
	
	/** rmi registry address */
	private static String registryAddress = "";
	
	/**
	 * Sets rmi registry address.
	 * 
	 * @param address a URL.
	 */
	public static void setRegistryAddress(String address) {
		registryAddress = address;
	}
	
	/**
	 * Returns rmi registry address which sets by <code>setRegistryAddress</code>.
	 * 
	 * @return a URL of rmi registry address.
	 */
	public static String getRegistryAddress() {
		return registryAddress;
	}
	
	/**
	 * Binds the specified <code>name</code> to a remote object.
	 * 
	 * @param name a name in Naming Server
	 * @param obj a reference for remote object
	 */
	public static void bind(String name, Remote obj) {
		
		
	}
	
	/**
	 * Returns a reference for the remote object associated with the specified <code>name</code>.
	 * 
	 * @param name a name in Naming Server
	 * @return a reference for a remote object if the specified <code>name</code> object exists, 
	 * <code>null</code> otherwise
	 */
	public static Remote lookup(String name) {
		
		return null;
	}
	
	/**
	 * Rebinds the specified <code>name</code> to a new object.
	 * 
	 * @param name a name in Naming Server
	 * @param obj new object to associate with the <code>name</code>
	 */
	public static void rebind(String name, Remote obj) {
		
		
	}
	
	/**
	 * Destroys the binding for the specified <code>name</code> that is associated with an object.
	 * 
	 * @param name a name in Naming Server
	 */
	public static void unbind(String name) {
		
	}
}
