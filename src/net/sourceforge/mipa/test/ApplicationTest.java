package net.sourceforge.mipa.test;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import net.sourceforge.mipa.application.AbstractApplication;
import net.sourceforge.mipa.application.ResultCallback;
import net.sourceforge.mipa.components.Broker;
import net.sourceforge.mipa.components.BrokerInterface;
import net.sourceforge.mipa.components.ContextModeling;
import net.sourceforge.mipa.components.ContextRetrieving;
import net.sourceforge.mipa.components.Coordinator;
import net.sourceforge.mipa.components.CoordinatorImp;
import net.sourceforge.mipa.components.ExponentDelayMessageDispatcher;
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
import net.sourceforge.mipa.eca.SensorAgent;
import net.sourceforge.mipa.eca.SensorPlugin;
import net.sourceforge.mipa.naming.Catalog;
import net.sourceforge.mipa.naming.IDManager;
import net.sourceforge.mipa.naming.IDManagerImp;
import net.sourceforge.mipa.naming.Naming;
import net.sourceforge.mipa.naming.NamingService;
import net.sourceforge.mipa.predicatedetection.PredicateParser;
import net.sourceforge.mipa.predicatedetection.PredicateParserMethod;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ApplicationTest {
	
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
	
	boolean flag = false;
	
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
	 * 支持环境感知应用注册感兴趣的环境特性
	 * @throws Exception
	 */
	public void testRegister() throws Exception {
		MIPAResource.setCheckMode("normal");
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
        //NoDelayMessageDispatcher messageDispatcher = new NoDelayMessageDispatcher();
        ExponentDelayMessageDispatcher messageDispatcher = new ExponentDelayMessageDispatcher();
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
        ecaManager.registerResources(resources);
        
        MIPAResource.setBroker(broker);

        //支持环境感知应用注册感兴趣的环境特性
        Application application = new Application();
        OnFire onFire = new OnFire();
        predicateID = application.register("config/predicate/predicate_scp.xml", onFire, "fire");
		
        assertTrue(predicateID != null);
        assertTrue(groupManager.getPredicateInfo(predicateID) != null);
        
        server.unbind("IDManager");
        server.unbind("MessageDispatcher");
        server.unbind("Broker");
        server.unbind("PredicateParser");
        server.unbind("Coordinator");
        server.unbind(dataSourceId);
        server.unbind(ecaManagerId);
	}

	@Test
	/**
	 * 支持环境感知应用注销环境特性
	 * @throws Exception
	 */
	public void testUnregister() throws Exception {
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
		
        String dataSourceId = idManager.getID(Catalog.DataSource);
        DataSourceImp dataSource = new DataSourceImp();
        DataSource dataSourceStub 
                            = (DataSource) UnicastRemoteObject
                                                 .exportObject(dataSource,
                                                                0);
        server.bind(dataSourceId, dataSourceStub);
        String ecaManagerId = idManager.getID(Catalog.ECAManager);
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
        resources.add(sensorPlugin.load("config/sensors/light_1.xml"));
        resources.add(sensorPlugin.load("config/sensors/RFID_1.xml"));
        ecaManager.registerResources(resources);

        MIPAResource.setBroker(broker);
        
        //支持环境感知应用注销环境特性
        Application application = new Application();
        OnFire onFire = new OnFire();
        predicateID = application.register("config/predicate/predicate_wcp.xml", onFire, "fire");
        assertTrue(predicateID != null);
        assertTrue(groupManager.getPredicateInfo(predicateID) != null);
        
        application.unregister("fire");
		assertTrue(groupManager.getPredicateInfo(predicateID)==null);
		
		server.unbind("IDManager");
        server.unbind("MessageDispatcher");
        server.unbind("Broker");
        server.unbind("PredicateParser");
        server.unbind("Coordinator");
        server.unbind(dataSourceId);
        server.unbind(ecaManagerId);
	}
	
    class Application extends AbstractApplication {
    	
    	public Application() {
			super();
		}
    }
    
    class OnFire extends UnicastRemoteObject implements ResultCallback {

		private static final long serialVersionUID = 6301745525414662234L;

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
    
    

}
