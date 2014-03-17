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

import java.util.HashMap;

import net.sourceforge.mipa.predicatedetection.ctl.CTLParser;
import net.sourceforge.mipa.predicatedetection.lattice.tctl.TCTLParser;
import net.sourceforge.mipa.ui.predicate.PredicateDot;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * local predicate parser module.
 * 
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class StructureParser {
	
	private static Logger logger = Logger.getLogger(StructureParser.class);
	
    HashMap<String,Structure> nameToCGS = new HashMap<String, Structure>();
    /** context mapping */
    // private ContextModeling contextMapping;

    public StructureParser() {
        //System.out.println("Testing constructor of STRUCTUREPARSER.");
        logger.info("Testing constructor of STRUCTUREPARSER.");
    }

    /*
     * public StructureParser(ContextModeling contextMapping) {
     * this.contextMapping = contextMapping; }
     */

    //FIXME CGS should split into each GSE, but this code merges all CGS into one GSE.
    
    /**
     * parse structure of predicate from <code>Document</code>.
     * 
     * @param predicate
     *            a document
     */
    public Structure parseStructure(Document predicate) {
        Structure result = new Composite(NodeType.SPECIFICATION,"specification");
        if(DEBUG){
            System.out.println("========================================");
            System.out.println("Parsing predicate:");
            logger.info("========================================");
            logger.info("Parsing predicate:");
        }
        NodeList elements = predicate.getElementsByTagName("CGSs");
        if(DEBUG){
            System.out.println("--CGSs");
            logger.info("--CGSs");
        }
        if (elements != null) {
            for (int i = 0; i < elements.getLength(); i++) {
                Node CGSs = elements.item(i);
                Structure CGSsNode = new Composite(NodeType.CGSs,"CGSs");
                result.add(CGSsNode);
                for (Node node = CGSs.getFirstChild(); 
                      node != null; 
                      node = node.getNextSibling()) {

                    if (node.getNodeType() == Node.ELEMENT_NODE) {

                        if (node.getNodeName().equals("CGS")) {
                            String name = node.getAttributes().getNamedItem("name").getNodeValue();
                            if(DEBUG){
                                System.out.println("----CGS:"+name);
                                logger.info("----CGS:"+name);
                            }
                            Structure CGSNode = new Composite(NodeType.CGS,
                                                              name);
                            if(!nameToCGS.containsKey(name)) {
                                nameToCGS.put(name, CGSNode);
                                CGSsNode.add(CGSNode);
                            }
                            else {
                                if(DEBUG){
                                    System.out.println("Parse error: A CGS with the same name already exists:"+name);
                                    logger.error("Parse error: A CGS with the same name already exists:"+name);
                                }
                            }
                            for (Node LP = node.getFirstChild(); 
                                  LP != null; 
                                  LP = LP.getNextSibling()) {
                                
                                if (LP.getNodeType() == Node.ELEMENT_NODE) {

                                    if (LP.getNodeName().equals("LP")) {
                                        LocalPredicate lp = parseLocalPredicate(LP);
                                        if(lp == null) {
                                        	return null;
                                        }
                                    	CGSNode.add(lp);
                                        //CGSNode.add(parseLocalPredicate(LP));
                                        
                                        // write UI information into predicate.dot file
                                    	PredicateDot.getInstance().write2PredicateDotLink(CGSNode, lp);
                                    }
                                } // :end if
                            } // :end for
                        } // :end if
                    } // :end if
                } // :end for
            } // :end for
        }
        
        /************************************************************************
         * if the type of predicate is CTL, then jump to parse ctl formula 
         * 
         * @see net.sourceforge.mipa.predicatedetection.CTLParser#parseCTLPart
         */
        if(PredicateIdentify.predicateIdentify(predicate) == PredicateType.CTL)
        {
        	result = CTLParser.getInstance().parseCTLPart(predicate, result,this);
        	return result;
        }
        /************************************************************************/
        if(PredicateIdentify.predicateIdentify(predicate) == PredicateType.TCTL)
        {
        	result = TCTLParser.getInstance().parseTCTLFormula(predicate, result, nameToCGS);
        	return result;
        }
        elements = predicate.getElementsByTagName("specification");
        if (elements != null) {
            if(DEBUG){
                System.out.println("--specification");
                logger.info("--specification");
            } 
            Node specification = elements.item(0);
            Node node = specification.getFirstChild(); 
            node = node.getNextSibling();
            String prefix = node.getAttributes().getNamedItem("value").getNodeValue();
            if(DEBUG){
                System.out.println("----prefix: "+prefix);
                logger.info("----prefix: "+prefix);
            }
            node = node.getNextSibling();
            node = node.getNextSibling();
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("GSE")) {
                    Structure GSENode = null;
                    if(prefix.equals("def")) {
                        GSENode = new Composite(NodeType.DEF, "GSE");
                    }
                    else{
                        GSENode = new Composite(NodeType.POS, "GSE");
                    }
                    result.add(GSENode);
                    if(DEBUG){
                        System.out.println("----GSE");
                        logger.info("----GSE");
                    }
                    for (Node element = node.getFirstChild(); 
                    element != null; 
                    element = element.getNextSibling()) {
                        if (element.getNodeType() == Node.ELEMENT_NODE) {
                            if (element.getNodeName().equals("element")) {
                                Structure elementNode = parseElement(element);
                                GSENode.add(elementNode);
                            }
                        }
                    }
                }
            }
        }
        if(DEBUG){
            System.out.println("Parsing predicate over");
            System.out.println("----------------------------------------");
            System.out.println();
            logger.info("Parsing predicate over");
            logger.info("----------------------------------------");
        }
        return result;
        
            
            
        /*    
        PredicateType type = PredicateIdentify.predicateIdentify(predicate);
        if(!type.equals(PredicateType.SEQUENCE)) {
            
        elements = predicate.getElementsByTagName("GSE");
        if (elements != null) {
            result = new Composite(NodeType.GSE, "GSE");
            if(DEBUG){
                System.out.println("GSE");
            }
            for (int i = 0; i < elements.getLength(); i++) {
                Node GSE = elements.item(i);

                for (Node node = GSE.getFirstChild(); 
                      node != null; 
                      node = node.getNextSibling()) {

                    if (node.getNodeType() == Node.ELEMENT_NODE) {

                        if (node.getNodeName().equals("CGS")) {
                            Structure CGSNode = new Composite(NodeType.CGS,
                                                              "CGS");
                            if(DEBUG){
                                System.out.println("----CGS");
                            }
                            result.add(CGSNode);

                            for (Node LP = node.getFirstChild(); 
                                  LP != null; 
                                  LP = LP.getNextSibling()) {
                                
                                if (LP.getNodeType() == Node.ELEMENT_NODE) {

                                    if (LP.getNodeName().equals("LP"))
                                        CGSNode.add(parseLocalPredicate(LP));
                                } // :end if
                            } // :end for
                        } // :end if
                    } // :end if
                } // :end for
            } // :end for
        } // :end if
        }
        */
    }
    
    private Structure parseElement(Node element) {
        // TODO Auto-generated method stub
        if(DEBUG){
            System.out.println("--------ELEMENT");
            logger.info("--------ELEMENT");
        }
        for (Node node = element.getFirstChild(); 
        node != null; 
        node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("zeroOrMore")) {
                    if(DEBUG){
                        System.out.println("------------ZEROORMORE");
                        logger.info("------------ZEROORMORE");
                    }
                    Connector zeroOrMore = new Connector(NodeType.ZEROORMORE,"zeroOrMore");
                    zeroOrMore.setOperator(NodeType.ZEROORMORE);
                    for (Node subElement = node.getFirstChild(); 
                    subElement != null; 
                    subElement = subElement.getNextSibling()) {
                        if (subElement.getNodeType() == Node.ELEMENT_NODE) {
                            if (subElement.getNodeName().equals("element")) {
                                System.out.print("--------");
                                Structure elementNode = parseElement(subElement);
                                zeroOrMore.add(elementNode);
                            }
                        }
                    }
                    return zeroOrMore;
                }
                else if (node.getNodeName().equals("oneOrMore")) {
                    if(DEBUG){
                        System.out.println("------------ONEORMORE");
                        logger.info("------------ONEORMORE");
                    }
                    Connector oneOrMore = new Connector(NodeType.ONEORMORE,"oneOrMore");
                    oneOrMore.setOperator(NodeType.ONEORMORE);
                    for (Node subElement = node.getFirstChild(); 
                    subElement != null; 
                    subElement = subElement.getNextSibling()) {
                        if (subElement.getNodeType() == Node.ELEMENT_NODE) {
                            if (subElement.getNodeName().equals("element")) {
                                System.out.print("--------");
                                Structure elementNode = parseElement(subElement);
                                oneOrMore.add(elementNode);
                            }
                        }
                    }
                    return oneOrMore;
                }
                else if (node.getNodeName().equals("choice")) {
                    if(DEBUG){
                        System.out.println("------------CHOICE");
                        logger.info("------------CHOICE");
                    }
                    Connector choice = new Connector(NodeType.CHOICE,"choice");
                    choice.setOperator(NodeType.CHOICE);
                    for (Node subElement = node.getFirstChild(); 
                    subElement != null; 
                    subElement = subElement.getNextSibling()) {
                        if (subElement.getNodeType() == Node.ELEMENT_NODE) {
                            if (subElement.getNodeName().equals("element")) {
                                System.out.print("--------");
                                Structure elementNode = parseElement(subElement);
                                choice.add(elementNode);
                            }
                        }
                    }
                    return choice;
                }
                else if (node.getNodeName().equals("optional")) {
                    if(DEBUG){
                        System.out.println("------------OPTIONAL");
                        logger.info("------------OPTIONAL");
                    }
                    Connector optional = new Connector(NodeType.OPTIONAL,"optional");
                    optional.setOperator(NodeType.OPTIONAL);
                    for (Node subElement = node.getFirstChild(); 
                    subElement != null; 
                    subElement = subElement.getNextSibling()) {
                        if (subElement.getNodeType() == Node.ELEMENT_NODE) {
                            if (subElement.getNodeName().equals("element")) {
                                System.out.print("--------");
                                Structure elementNode = parseElement(subElement);
                                optional.add(elementNode);
                            }
                        }
                    }
                    return optional;
                }
                else if (node.getNodeName().equals("cgs")) {
                    String name = node.getAttributes().getNamedItem("name").getNodeValue();
                    Structure CGS = nameToCGS.get(name);
                    if(DEBUG){
                        System.out.println("--------------------CGS:"+name);
                        logger.info("--------------------CGS:"+name);
                    }
                    return CGS;
                }
                else {
                    if(DEBUG){
                        System.out.println("Parse error: incorrect element:"+node.getNodeName());
                        logger.error("Parse error: incorrect element:"+node.getNodeName());
                    }
                }
            }
        }
        return null;
    }

    public LocalPredicate parseLocalPredicate(Node localPredicate) {
        String windowSize = "";
        if (localPredicate.getAttributes().getNamedItem("windowSize") != null) {
            windowSize = localPredicate.getAttributes().getNamedItem(
                    "windowSize").getNodeValue();
            try {
                int size = Integer.valueOf(windowSize);
                if (size < 1) {
                    new NumberFormatException("Window size of node "
                            + localPredicate.toString()
                            + " should be bigger than zero.").printStackTrace();;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.out
                        .println("Window size of node "
                                + localPredicate.toString()
                                + " should be an integer and bigger than zero.");
            }
        }
        LocalPredicate LP = new LocalPredicate();
        LP.setWindowSize(windowSize);
        if (DEBUG) {
            if (windowSize != "") {
                System.out.println("--------LP windowSize: " + windowSize);
                logger.info("--------LP windowSize: " + windowSize);
            }
            else {
                System.out.println("--------LP");
                logger.info("--------LP");
            }
        }
        for (Node node = localPredicate.getFirstChild(); 
              node != null; 
              node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("formula")) {
                    LP.add(parseFormula(LP,node));
                }
                else {
                	System.out.println("Parsing error! Illegal predicate!");
                	return null;
                } 	// :end if
            }// :end if
        }// :end for
        return LP;
    }
    
    public Formula parseFormula(LocalPredicate localPredicate, Node formula)
    {
        Formula formulaNode = new Formula(NodeType.FORMULA,"formula");
        if(DEBUG){
            System.out.println("------------formula");
            logger.info("------------formula");
        }
        for (Node node = formula.getFirstChild(); 
            node != null; 
            node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if(node.getNodeName().equals("quantifier"))
                {
                    Connector quantifierNode = new Connector(NodeType.QUANTIFIER,"quantifier");
                    String operator = node.getAttributes().getNamedItem("value").getNodeValue();
                    quantifierNode.setOperator(operator);
                    if(DEBUG){
                        System.out.println("----------------quantifier: "+operator);
                        logger.info("----------------quantifier: "+operator);
                    }
                    node = node.getNextSibling();
                    node = node.getNextSibling();
                   // if (node.getNodeType() == Node.ELEMENT_NODE) {
                   //     if(node.getNodeName().equals("quantifier"))
                    //    {
                            Formula subFormulaNode = parseFormula(localPredicate,node);
                            formulaNode.setConnetor(quantifierNode);
                            formulaNode.add(subFormulaNode);
                            //quantifierNode.setFather(formulaNode);
                            //subFormulaNode.setFather(formulaNode);
                            
                            // write UI information into predicate.dot file
                            PredicateDot.getInstance().write2PredicateDotLink(formulaNode, subFormulaNode);
                   //     }
                   // }
                }
                else if(node.getNodeName().equals("unary"))
                {
                    Connector unaryNode = new Connector(NodeType.UNARY,"unary");
                    String operator = node.getAttributes().getNamedItem("value").getNodeValue();
                    unaryNode.setOperator(operator);
                    if(DEBUG){
                        System.out.println("----------------unary: "+operator);
                        logger.info("----------------unary: "+operator);
                    }
                    node = node.getNextSibling();
                    node = node.getNextSibling();
                    Formula subFormulaNode = parseFormula(localPredicate,node);
                    formulaNode.setConnetor(unaryNode);
                    formulaNode.add(subFormulaNode);
                    //unaryNode.setFather(formulaNode);
                    //subFormulaNode.setFather(formulaNode);
                    
                    // write UI information into predicate.dot file
                    PredicateDot.getInstance().write2PredicateDotLink(formulaNode, subFormulaNode);
                }
                else if(node.getNodeName().equals("atom"))
                {
                    Atom atom = (Atom)parseAtom(node);
                    formulaNode.add(atom);
                    //atom.setFather(formulaNode);
                    localPredicate.addAtom(atom);
                    
                }
                else if(node.getNodeName().equals("formula"))
                {
                    Formula subFormulaNode_1 = parseFormula(localPredicate,node);
                    node = node.getNextSibling();
                    node = node.getNextSibling();
                    Connector binaryNode = new Connector(NodeType.BINARY,"binary");
                    String operator = node.getAttributes().getNamedItem("value").getNodeValue();
                    binaryNode.setOperator(operator);
                    if(DEBUG){
                        System.out.println("----------------binary: "+operator);
                        logger.info("----------------binary: "+operator);
                    }
                    node = node.getNextSibling();
                    node = node.getNextSibling();
                    Formula subFormulaNode_2 = parseFormula(localPredicate,node);
                    formulaNode.setConnetor(binaryNode);
                    formulaNode.add(subFormulaNode_1);
                    formulaNode.add(subFormulaNode_2);
                    //binaryNode.setFather(formulaNode);
                    //subFormulaNode_1.setFather(formulaNode);
                    //subFormulaNode_2.setFather(formulaNode);
                    
                    // write UI information into predicate.dot file
                    PredicateDot.getInstance().write2PredicateDotLink(formulaNode, subFormulaNode_1);
                    PredicateDot.getInstance().write2PredicateDotLink(formulaNode, subFormulaNode_2);
                }
                else
                {
                    System.out.println("Node "+node.getNodeName()+" existing here is illegal!");
                    logger.error("Node "+node.getNodeName()+" existing here is illegal!");
                }
            }
        }
        return formulaNode;
    }

    public Structure parseAtom(Node atom)
    {
        Atom atomNode = new Atom(NodeType.ATOM,"atom");
        if(DEBUG){
            System.out.println("--------------------atom");
            logger.info("--------------------atom");
        }
        String operator = atom.getAttributes().getNamedItem("operator").getNodeValue();
        String name = atom.getAttributes().getNamedItem("name").getNodeValue();
        String value = atom.getAttributes().getNamedItem("value").getNodeValue();
        if(DEBUG){
            System.out.println("------------------------"+name);
            System.out.println("------------------------"+operator);
            System.out.println("------------------------"+value);
            logger.info("------------------------"+name);
            logger.info("------------------------"+operator);
            logger.info("------------------------"+value);
        }
        atomNode.setOperator(operator);
        atomNode.setName(name);
        atomNode.setValue(value);
        return (Structure)atomNode;
    }
    
    public HashMap<String,Structure> getName2CGS()
    {
    	return this.nameToCGS;
    }
}