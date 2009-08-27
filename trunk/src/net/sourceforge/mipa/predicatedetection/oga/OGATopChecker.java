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
package net.sourceforge.mipa.predicatedetection.oga;

import java.rmi.RemoteException;

import net.sourceforge.mipa.ResultCallback;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.predicatedetection.AbstractChecker;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class OGATopChecker extends AbstractChecker {

    private static final long serialVersionUID = -5005195916176717460L;
    
    public OGATopChecker(ResultCallback application, String checkerName, String[] children) {
        super(application, checkerName, children);
        
    }

    @Override
    public void receive(Message message) throws RemoteException {
        // TODO Auto-generated method stub
        
    }

}
