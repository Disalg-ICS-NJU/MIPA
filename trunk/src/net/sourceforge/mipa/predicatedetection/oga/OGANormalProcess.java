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

import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.predicatedetection.AbstractNormalProcess;


/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class OGANormalProcess extends AbstractNormalProcess {

    private static final long serialVersionUID = -663144941748622894L;

    /**
     * construction.
     * 
     * @param name
     * @param checkers
     * @param normalProcesses
     * @param subNormalProcesses Global activity group.
     */
    public OGANormalProcess(String name, String[] checkers, String[] normalProcesses, String[] subNormalProcesses) {
        super(name, checkers, normalProcesses);
        
    }
    @Override
    public void action(boolean value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void receiveMsg(Message message) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void application() {
        // TODO Auto-generated method stub
        
    }
}
