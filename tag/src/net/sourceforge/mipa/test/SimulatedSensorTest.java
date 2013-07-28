package net.sourceforge.mipa.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import net.sourceforge.mipa.components.Broker;
import net.sourceforge.mipa.components.BrokerInterface;
import net.sourceforge.mipa.components.ContextModeling;
import net.sourceforge.mipa.components.ContextRetrieving;
import net.sourceforge.mipa.components.Coordinator;
import net.sourceforge.mipa.components.CoordinatorImp;
import net.sourceforge.mipa.components.GroupManager;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.components.MessageDispatcher;
import net.sourceforge.mipa.components.NoDelayMessageDispatcher;
import net.sourceforge.mipa.components.rm.ResourceManager;
import net.sourceforge.mipa.components.rm.SimpleResourceManager;
import net.sourceforge.mipa.eca.DataSource;
import net.sourceforge.mipa.eca.DataSourceImp;
import net.sourceforge.mipa.eca.ECAManager;
import net.sourceforge.mipa.eca.ECAManagerImp;
import net.sourceforge.mipa.eca.EmptyCondition;
import net.sourceforge.mipa.eca.Listener;
import net.sourceforge.mipa.eca.SensorAgent;
import net.sourceforge.mipa.eca.SensorPlugin;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.IDManagerImp;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.naming.NamingService;
import net.sourceforge.mipa.predicatedetection.Atom;
import net.sourceforge.mipa.predicatedetection.Formula;
import net.sourceforge.mipa.predicatedetection.LocalPredicate;
import net.sourceforge.mipa.predicatedetection.NodeType;
import net.sourceforge.mipa.predicatedetection.PredicateParser;
import net.sourceforge.mipa.predicatedetection.PredicateParserMethod;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimulatedSensorTest {
	static NamingService service = new NamingService();
	Naming server;
	IDManagerImp idManager;
	ContextModeling contextModeling;
	ContextRetrieving contextRetrieving;
	ResourceManager resourceManager;
	GroupManager groupManager;
	Broker broker;
	ECAManagerImp ecaManager;
	String ecaManagerId;
	String dataSourceId;
	static EmptyCondition condition;
	String result = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		service.startService();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	/**
	 * 支持环境数据收集设备及其环境数据的模拟
	 * @throws Exception
	 */
	public void testLoad() throws Exception {
		server = MIPAResource.getNamingServer();
        idManager = new IDManagerImp();
        IDManager managerStub = (IDManager) UnicastRemoteObject
                                                     .exportObject(idManager,
                                                                   0);
        server.bind("IDManager", managerStub);
        contextModeling = new ContextModeling();
        contextRetrieving = new ContextRetrieving();
        resourceManager =
                                new SimpleResourceManager(contextModeling,
                                                              contextRetrieving);
        //RandomDelayMessageDispatcher messageDispatcher = new RandomDelayMessageDispatcher();
        NoDelayMessageDispatcher messageDispatcher = new NoDelayMessageDispatcher();
        //ExponentDelayMessageDispatcher messageDispatcher = new ExponentDelayMessageDispatcher();
        MessageDispatcher messageDispatcherStub 
                                = (MessageDispatcher) UnicastRemoteObject
                                                            .exportObject(messageDispatcher,
                                                                          0);
        server.bind("MessageDispatcher", messageDispatcherStub);
        groupManager 
                            = new GroupManager(resourceManager);
        broker = new Broker(resourceManager, groupManager);
        BrokerInterface brokerStub = 
                        (BrokerInterface) UnicastRemoteObject
                                                .exportObject(broker, 0);
        server.bind("Broker", brokerStub);
        groupManager.setBroker(broker);
        MIPAResource.setBroker(broker);
        PredicateParser predicateParser = new PredicateParser(groupManager);
        PredicateParserMethod predicateParserStub 
                                    = (PredicateParserMethod) UnicastRemoteObject
                                                                    .exportObject(predicateParser,
                                                                                  0);
        server.bind("PredicateParser", predicateParserStub);
        broker.setPredicateParser(predicateParserStub);
        CoordinatorImp coordinator = new CoordinatorImp();
        Coordinator coordinatorStub = (Coordinator) UnicastRemoteObject.exportObject(coordinator, 0);
        server.bind("Coordinator", coordinatorStub);
		
        dataSourceId = idManager.getID(Catalog.DataSource);
        DataSourceImp dataSource = new DataSourceImp();
        DataSource dataSourceStub 
                            = (DataSource) UnicastRemoteObject
                                                 .exportObject(dataSource,
                                                                0);
        server.bind(dataSourceId, dataSourceStub);
        ecaManagerId = idManager.getID(Catalog.ECAManager);
        ecaManager = new ECAManagerImp(broker,
                                                                 dataSource,
                                                                 ecaManagerId);
        ECAManager ecaManagerStub 
                            = (ECAManager) UnicastRemoteObject
                                                  .exportObject(ecaManager,
                                                                0);
        server.bind(ecaManagerId, ecaManagerStub);
        SensorPlugin sensorPlugin = new SensorPlugin(dataSourceStub);
        ArrayList<SensorAgent> resources = new ArrayList<SensorAgent>();
        SensorAgent lightAgent = sensorPlugin.load("config/sensors/light.xml");
        resources.add(lightAgent);
        ecaManager.registerResources(resources);
        
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
		assertTrue(dataSource.getMap().get("light")!=null);
		BufferedReader br = new BufferedReader(new FileReader(new File("data/light")));
		br.readLine();
		Thread.sleep(1000);
		if (nonChecker.valuesArrayList.size() != 0) {
			for(int i = 0;i<nonChecker.valuesArrayList.size();i++) {
				String string = br.readLine();
				boolean f = Double.valueOf(string)>500?true:false;
				boolean g = Boolean.valueOf(nonChecker.valuesArrayList.get(i));
				assertTrue(g==f);
			}
		}
	}
	
	class NonChecker implements Listener {

		private static final long serialVersionUID = 4946014221748362494L;
		public ArrayList<String> valuesArrayList = new ArrayList<String>();

		@Override
		public void update(String eventName, String value) {
			result = value;
			synchronized (valuesArrayList) {
				valuesArrayList.add(value);
			}
		}
	}
}
