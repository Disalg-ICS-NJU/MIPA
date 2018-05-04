### Updates
 * The project is exported from googlecode on April 2015.
 * August 01, 2011, MIPA documentation updated. Part of the system design is provided in chapters in Part 2 of the documentation.
 
### MIPA (Middleware Infrastructure for Predicate detection in Asynchronous environments)
Pervasive applications are undergoing changes as more and more mobile devices are augmented with sensing and controlling abilities, besides the basic abilities of computation and communication. We call such devices C3S (Computation, Communication, Control, and Sensing) devices. Examples of C3S devices include mobile robots patrolling in a chemical plant for safety management and smart phones equipped with a variety of sensors.

C3S devices can provide rich context information for the applications, and pervasive applications are typically designed to be context-aware, i.e., intelligently adapting their behavior to the environment. However, enabling context-awareness through C3S devices is faced with severe challenges, as detailed below.

The contexts of interest to a pervasive application often span a geographically large area, and contain rich semantics. This is often beyond the ability of one single C3S device. Thus, a group of autonomous but also coordinating C3S devices should be deployed. Take a chemical plant scenario for example. A group of mobile robots are deployed to periodically patrol the plant for safety management. The robots need to proceed in certain formation to cover all possible spots of hazardous material leak. Appropriate spreading of multiple robots can also enable the robots to collect contexts with better quality, e.g., to sense the average temperature in the plant.

The coordination among the C3S devices is intrinsically asynchronous. There is no global clock available among the C3S devices. Constrained resources and task scheduling of the C3S devices (often embedded systems) may lead to unpredictable computation delay. The growing adoption of wireless communications, which are prone to bandwidth shortage, network congestion, unpredictable routings, and retransmission, leads to unpredictable communication delay. All these characteristics of the C3S devices and their communication networks lead to the intrinsic asynchrony among the contexts they collect.

One possible solution to cope with the asynchrony is clock synchronization. However, clock synchronization may not enable correct and fault-tolerant coordination among the autonomous C3S devices. Thus, it cannot enable context-awareness despite of the asynchrony in pervasive scenarios enriched of coordinating C3S devices. Specifically, each C3S device only has its own local clock, which cannot be perfectly synchronized. The uncertainty caused by the skew among the clocks may lead to incorrect behavior. Besides, clock synchronization schemes make assumptions on process execution speeds and communication delay. These assumptions may not be guaranteed for the autonomous C3S devices. A group of robots are prone to incorrect behavior even if one single assumption is violated. The inaccuracy of synchronization and the potential violation of assumptions make reasoning based on time and timeouts a delicate and error-prone undertaking. Furthermore, periodic clock synchronization may be unaffordable in terms of energy consumption, or be hampered due to device autonomy and administrative boundaries such as privacy concerns and security issues. Consequently, it is more practical to have few or, better, no synchrony assumption in scenarios of coordinating C3S devices.

To enable context-awareness for pervasive applications enriched with C3S devices, we introduce the predicate detection theory and propose the Predicate-Detection-based Context-Awareness (PD-CA) framework, which consists of three essential parts: 
  * Logical time is used to cope with the asynchrony among contexts collected from the asynchronous system of coordinating C3S devices. Temporal orders among asynchronous contextual events resulting from message causality are encoded and decoded via the logical vector clock. Global snapshot of the asynchronous system of C3S devices is redefined under the notion of logical time. Dynamic behavior of the C3S devices are modeled over sequences of global snapshots. 
  * Specification of predicates enables the applications to express their concerns on properties of the contexts. Based on the modeling above, the specification can delineate local contextual properties on local states of one C3S device, global contextual properties on snapshots of the system of C3S devices, and dynamic behavioral properties on sequences of snapshots. 
  * Context-awareness is enabled by detection of the specified contextual property at runtime. The detection is such a persistent process that new emerging contexts trigger the incremental detection of the specified property.

Under the guidance of the PD-CA framework, we develop the Middleware Infrastructure for Predicate detection in Asynchronous environments (MIPA). In context-aware computing scenarios, MIPA first receives contextual properties from the applications. It then decomposes the global contextual properties to local ones, with which MIPA instructs each C3S device to collect the related contexts. MIPA detects the specified contextual properties with the contexts in an online and incremental manner and informs the applications when the properties are satisfied. MIPA adopts a layered architecture to support this context processing process.

MIPA can simplify the development of context-aware applications in asynchronous pervasive computing environments. Based on the PD-CA framework, the context-aware adaptation logic of the application is constructed in a condition-action manner. The contextual property serves as the condition of context-aware behavior.

Please refer to our research papers below. If you have any comments or suggestions, please feel free to contact Yu Huang (http://cs.nju.edu.cn/yuhuang/).

### Key Citations
6. Yiling Yang, Yu Huang, Xiaoxing Ma, Jian Lu, Enabling Context-awareness by Predicate Detection in Asynchronous Pervasive Computing Environments, IEEE Transactions on Computers, 65(2):522-534, Feb. 2016.

5. Yiling Yang, Yu Huang, Jiannong Cao, Xiaoxing Ma, Jian Lu, Design of a Sliding Window over Distributed and Asynchronous Event Streams, IEEE Transactions on Parallel and Distributed Systems, 25(10):2551-2560, Oct. 2014.

4. Yiling Yang, Yu Huang, Jiannong Cao, Xiaoxing Ma, Jian Lu, Formal Specification and Runtime Detection of Dynamic Properties in Asynchronous Pervasive Computing Environments, IEEE Transactions on Parallel and Distributed Systems, 24(8):1546-1555, Aug. 2013.

3. Yu Huang, Yiling Yang, Jiannong Cao, Xiaoxing Ma, Xianping Tao, Jian Lu, Runtime Detection of the Concurrency Property in Asynchronous Pervasive Computing Environments, IEEE Transactions on Parallel and Distributed Systems, 23(4): 744-750, Apr. 2012.

2. Hengfeng Wei, Yu Huang, Jiannong Cao, Xiaoxing Ma, Jian Lu, Formal Specification and Runtime Detection of Temporal Properties for Asynchronous Context, In proc. of the International Conference on Pervasive Computing and Communications (PerCom), Mar. 2012.

1. Yu Huang, Xiaoxing Ma, Jiannong Cao, Xianping Tao and Jian Lu, Concurrent Event Detection for Asynchronous Consistency Checking of Pervasive Context, in proc. of the 7th Annual IEEE Intl. Conf. on Pervasive Computing and Communications (PerCom), 2009.

### INSTALL
  See more in ./INSTALL file.

### Any Questions?
For more detail, please visit https://github.com/alg-nju/mipa
