package net.sourceforge.mipa.test;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.Broker;
import net.sourceforge.mipa.components.BrokerInterface;
import net.sourceforge.mipa.components.ContextModeling;
import net.sourceforge.mipa.components.ContextRetrieving;
import net.sourceforge.mipa.components.Coordinator;
import net.sourceforge.mipa.components.CoordinatorImp;
import net.sourceforge.mipa.components.GroupManager;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.components.MessageDispatcher;
import net.sourceforge.mipa.components.MessageType;
import net.sourceforge.mipa.components.NoDelayMessageDispatcher;
import net.sourceforge.mipa.components.rm.ResourceManager;
import net.sourceforge.mipa.components.rm.SimpleResourceManager;
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
import net.sourceforge.mipa.predicatedetection.PredicateParser;
import net.sourceforge.mipa.predicatedetection.PredicateParserMethod;
import net.sourceforge.mipa.predicatedetection.normal.scp.SCPChecker;
import net.sourceforge.mipa.predicatedetection.normal.scp.SCPMessageContent;
import net.sourceforge.mipa.predicatedetection.normal.scp.SCPVectorClock;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CallBackTest {
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
	String predicateID = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		service.startService();
	}
	
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	/**
	 * 支持环境特性谓词满足时的回调
	 * @throws Exception
	 */
	public void testResultCallBack() throws Exception {
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
        resources.add(sensorPlugin.load("config/sensors/light.xml"));
        resources.add(sensorPlugin.load("config/sensors/RFID.xml"));
        resources.add(sensorPlugin.load("config/sensors/light_1.xml"));
        resources.add(sensorPlugin.load("config/sensors/RFID_1.xml"));
        ecaManager.registerResources(resources);

        Thread thread = new Thread(messageDispatcher);
        thread.start();
        
		//Document predicate = parseXml("config/predicate/predicate_wcp_2.xml");
    	OnFire callback = new OnFire();
        //predicateID = broker.registerPredicate(callback, predicate);
        //assertTrue(predicateID != null);
        //assertTrue(groupManager.getPredicateInfo(predicateID)!=null);
    	predicateID = idManager.getID(Catalog.Predicate);
    	String checkerName = idManager.getID(Catalog.Checker);
    	String[] normalProcesses = new String[2];
        normalProcesses[0] = idManager.getID(Catalog.NormalProcess);
        normalProcesses[1] = idManager.getID(Catalog.NormalProcess);
        SCPChecker checker = new SCPChecker(callback, predicateID, checkerName, normalProcesses);
        Message m = new Message();
        m.setSenderID(normalProcesses[0]);
        m.setReceiverID(checkerName);
        m.setType(MessageType.Detection);
        SCPVectorClock lo = new SCPVectorClock(2);
        ArrayList<Long> clock = new ArrayList<Long>();
        clock.add(new Long(1));
        clock.add(new Long(0));
        lo.setVectorClock(clock);
        SCPVectorClock hi = new SCPVectorClock(2);
        clock = new ArrayList<Long>();
        clock.add(new Long(3));
        clock.add(new Long(3));
        hi.setVectorClock(clock);
        SCPMessageContent content = new SCPMessageContent(lo, hi);
        m.setMessageContent(content);
        ArrayList<Message> mList = new ArrayList<Message>();
        mList.add(m);
        checker.check(mList);
        
        m = new Message();
        m.setSenderID(normalProcesses[1]);
        m.setReceiverID(checkerName);
        m.setType(MessageType.Detection);
        lo = new SCPVectorClock(2);
        clock = new ArrayList<Long>();
        clock.add(new Long(2));
        clock.add(new Long(2));
        lo.setVectorClock(clock);
        hi = new SCPVectorClock(2);
        clock = new ArrayList<Long>();
        clock.add(new Long(4));
        clock.add(new Long(4));
        hi.setVectorClock(clock);
        content = new SCPMessageContent(lo, hi);
        m.setMessageContent(content);
        
        mList = new ArrayList<Message>();
        mList.add(m);
        checker.check(mList);
        
        assertTrue(callback.isFlag());

        server.unbind("IDManager");
        server.unbind("MessageDispatcher");
        server.unbind("Broker");
        server.unbind("PredicateParser");
        server.unbind("Coordinator");
        server.unbind(dataSourceId);
        server.unbind(ecaManagerId);
        //service.stopService();
	}
	
	
	class OnFire extends UnicastRemoteObject implements ResultCallback {
		
		private static final long serialVersionUID = 6092626371046495298L;
		boolean flag = false;

		public OnFire() throws RemoteException {
			super();
			flag = false;
		}

		@Override
		public void callback(String value) {
			flag = true;
		}
		
		public boolean isFlag() {
			return flag;
		}
	}
	
	@After
	public void tearDown() {
		
	}
}
