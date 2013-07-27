package net.sourceforge.mipa.test;

import static org.junit.Assert.*;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.mipa.application.ResultCallback;
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
import net.sourceforge.mipa.eca.Condition;
import net.sourceforge.mipa.eca.DataSource;
import net.sourceforge.mipa.eca.DataSourceImp;
import net.sourceforge.mipa.eca.ECAManager;
import net.sourceforge.mipa.eca.ECAManagerImp;
import net.sourceforge.mipa.eca.SensorAgent;
import net.sourceforge.mipa.eca.SensorPlugin;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.IDManagerImp;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.naming.NamingService;
import net.sourceforge.mipa.predicatedetection.Atom;
import net.sourceforge.mipa.predicatedetection.PredicateIdentify;
import net.sourceforge.mipa.predicatedetection.PredicateParser;
import net.sourceforge.mipa.predicatedetection.PredicateParserMethod;
import net.sourceforge.mipa.predicatedetection.PredicateType;
import net.sourceforge.mipa.predicatedetection.Structure;
import net.sourceforge.mipa.predicatedetection.StructureParser;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class PredicateDecompositionTest {
	
	static NamingService service = new NamingService();
	static StructureParser structureParser;
	
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
	String predicateID = null;
	boolean flag = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		service.startService();
		structureParser = new StructureParser();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	/**
	 * 支持全局环境特性的分解
	 * @throws Exception
	 */
	public void testPredicateDecomposition() throws Exception {
		server = MIPAResource.getNamingServer();
		MIPAResource.setCheckMode("normal");
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
        NoDelayMessageDispatcher messageDispatcher = new NoDelayMessageDispatcher();
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
        resources.add(sensorPlugin.load("config/sensors/light.xml"));
        resources.add(sensorPlugin.load("config/sensors/RFID.xml"));
        resources.add(sensorPlugin.load("config/sensors/light_1.xml"));
        resources.add(sensorPlugin.load("config/sensors/RFID_1.xml"));
        ecaManager.registerResources(resources);

		Document predicate = parseXml("config/predicate/predicate_scp.xml");
		PredicateType type = PredicateIdentify.predicateIdentify(predicate);
		assertEquals(PredicateType.SCP, type);
		Structure s = structureParser.parseStructure(predicate);
		OnFire callback = new OnFire();
		groupManager.createGroups(s,type,callback);
        assertTrue(broker.getNormalProcessToECAManager().get("NormalProcess0").equals(ecaManagerId));
        assertTrue(broker.getNormalProcessToECAManager().get("NormalProcess1").equals(ecaManagerId));
        Map<String, ArrayList<Condition>> map = dataSource.getMap();
        
        assertTrue(map.get("light")!=null);
        assertTrue(map.get("light").get(0).getLocalPredicate() instanceof Atom);
        Atom lightAtom = (Atom) map.get("light").get(0).getLocalPredicate();
        assertTrue(lightAtom.getName().equals("light"));
        assertTrue(lightAtom.getOperator().equals("great-than"));
        assertTrue(lightAtom.getValue().equals("500"));

        assertTrue(map.get("RFID")!=null);
        assertTrue(map.get("RFID").get(0).getLocalPredicate() instanceof Atom);
        Atom RFIDAtom = (Atom) map.get("RFID").get(0).getLocalPredicate();
        assertTrue(RFIDAtom.getName().equals("RFID"));
        assertTrue(RFIDAtom.getOperator().equals("not-contain"));
        assertTrue(RFIDAtom.getValue().equals("tag_00001"));
        
        server.unbind("IDManager");
        server.unbind("MessageDispatcher");
        server.unbind("Broker");
        server.unbind("PredicateParser");
        server.unbind("Coordinator");
        server.unbind(dataSourceId);
        server.unbind(ecaManagerId);
	}
	
	private Document parseXml(String fileName) {
        Document doc = null;
        try {
            File f = new File(fileName);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }
	
	class OnFire extends UnicastRemoteObject implements ResultCallback {

		private static final long serialVersionUID = 6960306665975693229L;

		public OnFire() throws RemoteException {
			super();
			flag = false;
		}

		@Override
		public void callback(String value) {
			System.err.println("Callback!");
			flag = true;
		}
		
		public boolean isFlag() {
			return flag;
		}
	}

}
