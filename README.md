# Arrowhead Client Library (Java Spring-Boot)
##### The project provides client library for the Arrowhead Framework 4.1.3

Arrowhead Client Library contains all the common arrowhead dependencies and data transfer objects and also an 'ArrowheadService' with the purpose of easily interacting with the framework.

### Requirements

The project has the following dependencies:
* JRE/JDK 11 [Download from here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
* Maven 3.5+ [Download from here](http://maven.apache.org/download.cgi) | [Install guide](https://www.baeldung.com/install-maven-on-windows-linux-mac)
* GitHub Packages [Configuring Maven for GitHub Packages](https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-apache-maven-for-use-with-github-packages)

### Changes in this version
* Orchestrator class implemented (request orchestration with this class):
    * use with @Autowired injection
    * orchestration flags can defined via application.properties (changes during runtime always possible)
    * searching with version can be disabled or enabled by application.properties (changes during runtime always possible)
    * minimal and maximal version can defined in application.properties (changes during runtime always possible)
    * it is possible to define how many times the orchestration can fail and how long it has to wait until next try (changes during runtime always possible)
* Service Registry class implemented (request register/unregister a service with this class):
    * use with @Autowired injection
    * register a service or any other services which are not part of the client
    * unregister a service
    * external services cannot register with 0.0.0.0 or 169.x.x.x addresses
    * services with 0.0.0.0 address will be automatically registered with all available addresses
    * defines for this class from application.properties can be changed during runtime
* Event Handler class implemented (publish an/subscribe to/unsubscribe from event with this class):
    * use with @Autowired injection
    * use to register a/subscribe to/unsubscribe from an event
    * predefined options from application.properties can be change during runtime
* ConfigEventProperties class implemented:
    * map defined events in application.properties to Key-Value-Map for Event Handler (automapping)
    * it is used for publisher and subscriber
* implemented SenML class for easy access and as easy-to-use template

#### Configuration properties for Arrowhead Client
Here you can see the configuration properties which you can use to customize the core service instances. The
 following scopes exist:
 * arrowhead.client.consumer: Consumer
 * arrowhead.client.systems: all
 * arrowhead.client.orchestration: services which need the orchestrator
 * arrowhead.client.subscriber: subscriber
 * arrowhead.client.subscriber: publisher

| Property | Description | Data Type | Default value* | Required |
| --- | --- | --- | --- | --- |
| arrowhead.client.system.name | System name | String | | | yes |
| arrowhead.client.system.interface.secure | secure interface name | String | | yes |
| arrowhead.client.system.interface.insecure | insecure interface name | String | | yes |
| arrowhead.client.orchestration.min-version | minimum version of requested services | Integer | 0 | no |
| arrowhead.client.orchestration.max-version | maximum version of a requested service | Integer | 100 | no |
| arrowhead.client.orchestration.enable-version | enable search with version | Boolean | false | no |
| arrowhead.client.orchestration.max-retry | maximum of trials after orchestration request canceled | Integer | 0 | no |
| arrowhead.client.orchestration.retry-wait | time in seconds to wait after orchestration fails to try again | Integer | 1 | no |
| arrowhead.client.orchestration.matchmaking | Orchestration Flag; Interface name must match | Boolean | false | no |
| arrowhead.client.orchestration.override-store | Orchestration Flag; Override Orchestration Store | Boolean | true | no |
| arrowhead.client.orchestration.metadata-search | Orchestration Flag; search services with metadata | Boolean | true | no |
| arrowhead.client.orchestration.enable-inter-cloud | Orchestration Flag; search in other Arrowhead Clouds | Boolean | false | no |
| arrowhead.client.orchestration.enable-qos | Orchestration Flag; activate QoS | Boolean | false | no |
| arrowhead.client.orchestration.external-service-request | Orchestration Flag; request external services | Boolean | false | no |
| arrowhead.client.orchestration.only-preferred | Orchestration Flag; return only preferred services | Boolean | false | no |
| arrowhead.client.orchestration.trigger-inter-cloud | Orchestration Flag; trigger other clouds for requests | Boolean | false | no |
| arrowhead.client.orchestration.ping-providers | Orchestration Flag; ping providers and only return active services | Boolean | true | no |
| arrowhead.client.subscriber.base-notification-uri | root URI for RestController; Events will published after subscription to this root (base-notification-uri/eventname) | String | NULL | no |
| arrowhead.client.subscriber.events.* | events to subscribe (arrowhead.client.subscriber.events.x=y); x stands for an event (case-insensitive); y is the uri of the restcontroller mapping method (case-insensitive) | String | | yes |
| arrowhead.client.publisher.events.* | events to publish (arrowhead.client.publisher.events.x=y); x is the key identifier in map; y is the name of an event | String | | yes |
| server.address | IP/Hostname of current system (0.0.0.0 will replaced with all available IP´s) | String | | yes |
| server.port | Port of the service on the system | Integer | | yes | 

\* empty means necessary

#### Example
Use the injection interface ``@Autowired`` to bind the variable to the core service class.

```Java
public class MyClass {
    @Autowired
    private ServiceRegistry serviceRegistry;

    public void registerMyService() {
        Map<String, String> metadata = new HashMap<>();

        //Checking the availability of necessary core systems
        checkCoreSystemReachability(CoreSystem.SERVICE_REGISTRY);

        if (sslEnabled && tokenSecurityFilterEnabled) {
            checkCoreSystemReachability(CoreSystem.AUTHORIZATION);			
    
            //Initialize Arrowhead Context
            arrowheadService.updateCoreServiceURIs(CoreSystem.AUTHORIZATION);			
        
            setTokenSecurityFilter();
        }

        // set metadata for service
        metadata.put("country", "Germany");
        metadata.put("location", "Dresden");
        metadata.put("organisation", "HTW Dresden");
        metadata.put("department", "production");
        metadata.put("system", "MES");
        metadata.put("replica", "3");

        // register the service
        serviceRegistry.registerService("mes-data", "/mes", HttpMethod.GET, metadata);
    }
}
```