package net.sourceforge.mipa.application;

import java.util.Locale.Category;

import net.sourceforge.mipa.naming.Catalog;

public class User {
	Catalog catalog;

	public User() {
		catalog = Catalog.Worker;
	}
	
	public User(Catalog catalog) {
		this.catalog = catalog;
	}
	
	public String getLocation() {
		// TODO Auto-generated method stub
		return "workshop_1";
	}

	public Catalog getType() {
		// TODO Auto-generated method stub
		return catalog;
	}

	public void notify(String string) {
		// TODO Auto-generated method stub
		System.out.println(string);
	}

}
