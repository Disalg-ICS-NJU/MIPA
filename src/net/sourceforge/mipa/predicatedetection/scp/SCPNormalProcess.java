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
package net.sourceforge.mipa.predicatedetection.scp;

import java.rmi.RemoteException;

import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.predicatedetection.AbstractNormalProcess;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class SCPNormalProcess extends AbstractNormalProcess {

    private static final long serialVersionUID = -2040352661249255553L;

    /**
     * @param name
     */
    public SCPNormalProcess(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see net.sourceforge.mipa.components.Communication#receive(net.sourceforge.mipa.components.Message)
     */
    @Override
    public void receive(Message message) throws RemoteException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sourceforge.mipa.eca.Listener#update(java.lang.String, java.lang.String)
     */
    @Override
    public void update(String eventName, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void action(boolean value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }

}
