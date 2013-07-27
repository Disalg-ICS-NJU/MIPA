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
package net.sourceforge.mipa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import net.sourceforge.mipa.application.AbstractApplication;
import net.sourceforge.mipa.application.OnRegister;
import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.MIPAResource;

public class SCPApplication extends AbstractApplication {

	public SCPApplication() {
		super();
	}

	public void run() throws NotBoundException, IOException {
		System.out.println("Context-aware application starts successfully.");
		while(true) {
			System.out.println("----------------------------");
			System.out.println("1: Register predicate");
			System.out.println("2: UnRegister predicate");
			System.out.println("3: Exit");
			System.out.println("Please enter the function number:");
			BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
			String input = in.readLine();
			if(input.trim().equals("1")) {
				MIPAResource.setCheckMode("normal");
				ResultCallback onRegister = new OnRegister();
				register("config/predicate/predicate_scp.xml", onRegister, "scp");
			}else if(input.trim().equals("2")) {
				unregister("scp");
			}
			else if(input.trim().equals("3")) {
				System.exit(0);
			}
		}
	}

	public static void main(String[] args) throws NotBoundException, IOException {
		SCPApplication app = new SCPApplication();
		app.run();
	}
}
