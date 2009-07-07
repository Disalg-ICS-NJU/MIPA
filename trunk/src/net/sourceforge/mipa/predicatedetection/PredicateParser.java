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

import java.rmi.RemoteException;

import net.sourceforge.mipa.components.ContextMapping;

import org.w3c.dom.Document;

/**
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class PredicateParser implements PredicateParserMethod {

    /** local predicate parser reference */
    private LocalPredicateParser localPredicateParser;

    /** checker logic parser reference */
    private CheckerParser checkerParser;

    /**
     * default construction.
     */
    public PredicateParser(ContextMapping contextMapping) {
        localPredicateParser = new LocalPredicateParser(contextMapping);
        checkerParser = new CheckerParser();
    }

    public void parsePredicate(String applicationName, Document predicate)
                                                                          throws RemoteException {

        if (DEBUG) {
            System.out.println("parsing predicate...");
        }

        PredicateType type = PredicateIdentify.predicateIdentify(predicate);

        localPredicateParser.parseLocalPredicate(predicate, applicationName,
                                                 type);

        checkerParser.parseChecker(predicate, applicationName, type);
    }
}
