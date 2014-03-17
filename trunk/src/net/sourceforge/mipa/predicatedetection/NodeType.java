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

/**
 * @author jpyu
 *
 */
public enum NodeType {
    GSE,
    CGS,
    DEF,
    POS,
    LP,
    FORMULA,
    ATOM,
    QUANTIFIER,
    BINARY,
    UNARY,
    POLONARY,
    UNIVERSAL,
    EXISTENTIAL,
    NOT,
    CONJUNCTION,
    DISJUNCTION,
    IMPLY,
    SPECIFICATION,
    CGSs,
    ZEROORMORE,
    ONEORMORE,
    CHOICE,
    OPTIONAL,
    CTLFORMULA,
    EX,
    EU,
    EF,
    EG,
    AX,
    AU,
    AF,
    AG,
    TCTLFORMULA
}
