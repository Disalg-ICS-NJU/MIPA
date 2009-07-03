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
 * <code>MIPAResource</code> provides basic resource that MIPA uses.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class MIPAResource {
    /** naming server address */
    private static String namingAddress;

    /**
     * returns naming server address.
     * 
     * @return naming server address
     */
    public static String getNamingAddress() {
        return namingAddress;
    }

    /**
     * sets naming server address.
     * 
     * @param namingAddress
     *            naming server address
     */
    public static void setNamingAddress(String namingAddress) {
        MIPAResource.namingAddress = namingAddress;
    }
}
