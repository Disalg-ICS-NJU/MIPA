package net.sourceforge.mipa.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import net.sourceforge.mipa.components.ExponentDelayMessageDispatcher;
import net.sourceforge.mipa.components.FileDelayMessageDispatcher;
import net.sourceforge.mipa.components.GroupManager;
import net.sourceforge.mipa.components.MIPAResource;
import net.sourceforge.mipa.components.Message;
import net.sourceforge.mipa.components.MessageDispatcher;
import net.sourceforge.mipa.components.MessageType;
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
import net.sourceforge.mipa.predicatedetection.NormalProcess;
import net.sourceforge.mipa.predicatedetection.PredicateParser;
import net.sourceforge.mipa.predicatedetection.PredicateParserMethod;
import net.sourceforge.mipa.predicatedetection.normal.scp.SCPChecker;
import net.sourceforge.mipa.predicatedetection.normal.scp.SCPMessageContent;
import net.sourceforge.mipa.predicatedetection.normal.scp.SCPNormalProcess;
import net.sourceforge.mipa.predicatedetection.normal.scp.SCPVectorClock;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimulatedDelayTest {
	
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
	 * 支持环境数据收集设备向工具分发环境数据的网络模拟
	 * @throws Exception
	 */
	public void testSimulatedDelay() throws Exception {
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
        //ExponentDelayMessageDispatcher messageDispatcher = new ExponentDelayMessageDispatcher();
        FileDelayMessageDispatcher messageDispatcher = new FileDelayMessageDispatcher("config/file_message_delay");
        MessageDispatcher messageDispatcherStub 
                                = (MessageDispatcher) UnicastRemoteObject
                                                            .exportObject(messageDispatcher,
                                                                          0);
        server.bind("MessageDispatcher", messageDispatcherStub);
        MIPAResource.setMessageDispatcher(messageDispatcher);
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
        
        Thread thread = new Thread(messageDispatcher);
        thread.start();
        
        String NPname1 = idManager.getID(Catalog.NormalProcess);
        String NPname2 = idManager.getID(Catalog.NormalProcess);
        String Checkername1 = idManager.getID(Catalog.Checker);
        String[] NPs = new String[2];
        String[] checkers = new String[1];
        NPs[0]= NPname1;
        NPs[1]= NPname2;
        checkers[0] = Checkername1;
        SCPNormalProcess scpNormalProcess1 = new SCPNormalProcess(NPname1, checkers, NPs);
        SCPNormalProcess scpNormalProcess2 = new SCPNormalProcess(NPname2, checkers, NPs);
        NormalProcess npStub1 = (NormalProcess) UnicastRemoteObject.exportObject(
        		scpNormalProcess1, 0);
        NormalProcess npStub2 = (NormalProcess) UnicastRemoteObject.exportObject(
        		scpNormalProcess2, 0);
        server.bind(NPname1, npStub1);
        server.bind(NPname2, npStub2);
        
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
        scpNormalProcess1.send(MessageType.Control, NPname2, content);
        //Thread.sleep(1000);
        scpNormalProcess1.send(MessageType.Control, NPname2, content);
        Thread.sleep(1000);
        long receive1 = scpNormalProcess2.getMsgBuf().get(0).getPhysicalReceiveTime();
        long send1 = scpNormalProcess2.getMsgBuf().get(0).getPhysicalSendTime();
        long receive2 = scpNormalProcess2.getMsgBuf().get(1).getPhysicalReceiveTime();
        long send2 = scpNormalProcess2.getMsgBuf().get(1).getPhysicalSendTime();
        BufferedReader bf = new BufferedReader(new FileReader(new File("config/file_message_delay")));
        long time1 = Long.valueOf(bf.readLine());
        long time2 = Long.valueOf(bf.readLine());
        assertTrue(Math.abs(receive1-send1-time1)<20);
        assertTrue(Math.abs(receive2-send2-time2)<20);
	}

	
	class OnFire extends UnicastRemoteObject implements ResultCallback {

		public OnFire() throws RemoteException {
			super();
		}

		@Override
		public void callback(String value) {
		}

	}
}
