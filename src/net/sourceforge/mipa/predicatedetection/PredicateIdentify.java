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

import static config.Debug.DEBUG;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Identify predicate type.
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class PredicateIdentify {
	
	private static Logger logger = Logger.getLogger(PredicateIdentify.class);	
	
    public static PredicateType predicateIdentify(Document predicate) {
        
        if(DEBUG) {
            System.out.println("\tidentify predicate...");
            logger.info("Identify predicate...");
        }
        Element root = predicate.getDocumentElement();
        
        String type = root.getAttribute("type");
        if(DEBUG) {
	        System.out.println("predicate type is " + type);
	        logger.info("Predicate type is " + type);
        }
        return PredicateType.valueOf(type);
    }
}
