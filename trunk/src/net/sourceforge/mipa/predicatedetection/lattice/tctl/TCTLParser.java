/**
 * CTLParser is used to parse the ctl formula given in the form of xml file.
 * 
 * @see net.sourceforge.mipa.predicatedetection.StrctureParser#parseStructure.
 */
package net.sourceforge.mipa.predicatedetection.lattice.tctl;

import static config.Debug.DEBUG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.mipa.predicatedetection.Composite;
import net.sourceforge.mipa.predicatedetection.Connector;
import net.sourceforge.mipa.predicatedetection.Formula;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.NodeType;
import net.sourceforge.mipa.predicatedetection.Structure;
import net.sourceforge.mipa.predicatedetection.StructureParser;
import net.sourceforge.mipa.predicatedetection.TimedConnector;
import net.sourceforge.mipa.ui.predicate.PredicateDot;
import net.sourceforge.mipa.ui.predicate.PredicateTabUI;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TCTLParser
{
//	private static Logger logger = Logger.getLogger(TCTLParser.class);

	private static TCTLParser instance = null;
	private TCTLParser()
	{
		
	}
	public static TCTLParser getInstance()
	{
		if(instance == null)
			instance = new TCTLParser();
		
		return instance;
	}
	
	@SuppressWarnings("unused")
	public Structure parseTCTLFormula(Document predicate, Structure partStructure,HashMap<String, Structure> nameToCGS)
	{
		NodeList elements = predicate.getElementsByTagName("specification");
        if (elements != null) {
            Node specification = elements.item(0);
            Node node = specification.getFirstChild(); 
            node = node.getNextSibling();
            node = node.getFirstChild();
            node = node.getNextSibling();
            TimedConnector operator = null;
            if(node.getNodeName().equals("unary")) {
            	String unary = node.getAttributes().getNamedItem("value").getNodeValue();
            	String leftOperator = node.getAttributes().getNamedItem("leftOperator").getNodeValue();
            	String leftBound = node.getAttributes().getNamedItem("leftBound").getNodeValue();
            	String rightOperator = node.getAttributes().getNamedItem("rightOperator").getNodeValue();
            	String rightBound = node.getAttributes().getNamedItem("rightBound").getNodeValue();
            	operator = new TimedConnector(NodeType.UNARY, unary);
            	switch(unary) {
            	case "EF":
            		operator.setOperator(NodeType.EF);
            		break;
            	case "EG":
            		operator.setOperator(NodeType.EG);
            		break;
            	case "AF":
            		operator.setOperator(NodeType.AF);
            		break;
            	case "AG":
            		operator.setOperator(NodeType.AG);
            		break;
            	}
            	operator.setLeftOperator(leftOperator);
            	operator.setLeftBound(leftBound);
            	operator.setRightOperator(rightOperator);
            	operator.setRightBound(rightBound);
            	partStructure.add(operator);
            }
            node = node.getNextSibling();
            node = node.getNextSibling();
            if(node.getNodeName().equals("TCTLFormula")) {
            	node = node.getFirstChild();
            	node = node.getNextSibling();
            	if(node.getNodeName().equals("cgs")) {
                	String name = node.getAttributes().getNamedItem("name").getNodeValue();
                    Structure CGS = nameToCGS.get(name);
                    operator.add(CGS);
            	}
            }
        }
        return partStructure;
	}
}
