package net.sourceforge.mipa.test;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.eca.DataSourceImp;
import net.sourceforge.mipa.eca.EmptyCondition;
import net.sourceforge.mipa.eca.Listener;
import net.sourceforge.mipa.predicatedetection.Atom;
import net.sourceforge.mipa.predicatedetection.Formula;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.NodeType;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalPredicateTest {
	
	boolean flag = false;
	String result = null;
	static DataSourceImp dataSource;
	static EmptyCondition condition;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	/**
	 * 局部环境特性的注册
	 * @throws Exception
	 */
	public void testAttach() throws Exception {
		dataSource = new DataSourceImp();
		NonChecker nonChecker = new NonChecker();
		LocalPredicate localPredicate = new LocalPredicate();
		Atom atom = new Atom(NodeType.ATOM, "light");
		atom.setOperator("great-than");
		atom.setValueType("Double");
		atom.setValue("500");
		Formula formulaNode = new Formula(NodeType.FORMULA, "formula");
		formulaNode.add(atom);
		localPredicate.add(formulaNode);
	    condition = new EmptyCondition(nonChecker, localPredicate);
		dataSource.attach(condition, "light");
		assertTrue(dataSource.getMap().get("light")!=null);
	}

	@Test
	/**
	 * 局部环境特性的注销
	 * @throws Exception
	 */
	public void testDetach() throws Exception {
		dataSource = new DataSourceImp();
		NonChecker nonChecker = new NonChecker();
		LocalPredicate localPredicate = new LocalPredicate();
		Atom atom = new Atom(NodeType.ATOM, "light");
		atom.setOperator("great-than");
		atom.setValueType("Double");
		atom.setValue("500");
		Formula formulaNode = new Formula(NodeType.FORMULA, "formula");
		formulaNode.add(atom);
		localPredicate.add(formulaNode);
	    condition = new EmptyCondition(nonChecker, localPredicate);
		dataSource.attach(condition, "light");
		assertTrue(dataSource.getMap().get("light").size() != 0);
		
		dataSource.detach(condition, "light");
		assertTrue(dataSource.getMap().get("light").size() == 0);
	}

	@Test
	/**
	 * 局部环境特性的高效收集
	 * @throws Exception
	 */
	public void testNotifyCondition() throws Exception {
		dataSource = new DataSourceImp();
		NonChecker nonChecker = new NonChecker();
		LocalPredicate localPredicate = new LocalPredicate();
		Atom atom = new Atom(NodeType.ATOM, "light");
		atom.setName("light");
		atom.setNodeName("light");
		atom.setOperator("great-than");
		atom.setValueType("Double");
		atom.setValue("500");
		Formula formulaNode = new Formula(NodeType.FORMULA, "formula");
		formulaNode.add(atom);
		localPredicate.add(formulaNode);
	    condition = new EmptyCondition(nonChecker, localPredicate);
		dataSource.attach(condition, "light");
		
		String[] strings = new String[1];
		strings[0] = "100";
		
		dataSource.notifyCondition("light", strings);
		assertTrue(result.equals("false"));
		
		strings[0] = "1000";
		
		dataSource.notifyCondition("light", strings);
		assertTrue(result.equals("true"));
	}
	
	class NonChecker implements Listener {

		private static final long serialVersionUID = 4946014221748362494L;

		@Override
		public void update(String eventName, String value) {
			// TODO Auto-generated method stub
			flag = true;
			result = value;
		}
		
	}

}
