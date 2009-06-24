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


/**
 * This interface provides system register interface for others modules in mipa system.
 * 
 * @author Jianping YU
 */
public interface NamingServer {
	/**
	 * binds the object with its name is 'name'.
	 * 
	 * @param name The name in Naming server
	 * @param object The object wants to be binded
	 */
	public void binding(String name, Object object);
	
	/**
	 * looks up object with its name is 'name'.
	 * 
	 * @param name Object name
	 * @return The finded object If the object with 'name' in naming server, NULL otherwise.
	 */
	public Object lookup(String name);

}
