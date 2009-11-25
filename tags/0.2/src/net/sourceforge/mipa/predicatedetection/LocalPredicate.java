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
package net.sourceforge.mipa.predicatedetection;

import java.util.ArrayList;

/**
 * The <code>LocalPredicate</code> class represents local predicate.
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class LocalPredicate extends Atom {

    private static final long serialVersionUID = -1765170172935638206L;
    
    /** atoms that local predicate related */
    private ArrayList<Atom> atoms;
    
    //private String connection;
    
    public LocalPredicate() {
        atoms = new ArrayList<Atom>();
    }
    
    public Atom getAtomByName(String name) {
        for(int i = 0; i < atoms.size(); i++) {
            if(name.equals(atoms.get(i))) return atoms.get(i);
        }
        return null;
    }

    /**
     * @return the atoms
     */
    public ArrayList<Atom> getAtoms() {
        return atoms;
    }
    
    public void addAtom(Atom atom) {
        atoms.add(atom);
    }
    
    
}