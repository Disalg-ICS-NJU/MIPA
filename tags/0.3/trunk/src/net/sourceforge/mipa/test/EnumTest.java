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
package net.sourceforge.mipa.test;

import net.sourceforge.mipa.naming.Catalog;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class EnumTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Catalog c = Catalog.DataSource;
        Integer i = new Integer(1);
        String id = c.toString() + i;
        System.out.println(id);

    }

}
