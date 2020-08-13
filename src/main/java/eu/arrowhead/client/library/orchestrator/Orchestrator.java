package eu.arrowhead.client.library.orchestrator;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.shared.*;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrator
 */
@Component
public class Orchestrator {
    @Autowired
    private ArrowheadService arrowheadService;

    @Autowired
    protected SSLProperties sslProperties;

    @Value("${arrowhead.client.system.interface.secure}")
    private String interfaceSecure;
    @Value("${arrowhead.client.system.interface.insecure}")
    private String interfaceInsecure;

    @Value("${arrowhead.client.orchestration.max-retry:0}")
    private int orchestrationMaxRetry;
    @Value("${arrowhead.client.orchestration.retry-wait:1}")
    private int orchestrationRetryWait;

    @Value("${arrowhead.client.orchestration.enable-version:false}")
    private boolean enableVersion;
    @Value("${arrowhead.client.orchestration.min-version:0}")
    private int minVersion;
    @Value("${arrowhead.client.orchestration.max-version:100}")
    private int maxVersion;

    @Value("${arrowhead.client.orchestration.matchmaking:false}")
    private boolean orchestrationOptionMatchmaking;
    @Value("${arrowhead.client.orchestration.override-store:true}")
    private boolean orchestrationOptionOverrideStore;
    @Value("${arrowhead.client.orchestration.metadata-search:true}")
    private boolean orchestrationOptionMetadataSearch;
    @Value("${arrowhead.client.orchestration.enable-inter-cloud:false}")
    private boolean orchestrationOptionEnableInterCloud;
    @Value("${arrowhead.client.orchestration.enable-qos:false}")
    private boolean orchestrationOptionEnableQos;
    @Value("${arrowhead.client.orchestration.external-service-request:false}")
    private boolean orchestrationOptionExternalServiceRequest;
    @Value("${arrowhead.client.orchestration.only-preferred:false}")
    private boolean orchestrationOptionOnlyPreferred;
    @Value("${arrowhead.client.orchestration.trigger-inter-cloud:false}")
    private boolean orchestrationOptionTriggerInterCloud;
    @Value("${arrowhead.client.orchestration.ping-providers:true}")
    private boolean orchestrationOptionPingProviders;

    /**
     * request a orchestration
     *
     * @param serviceDefinition
     * @param serviceRequirement
     * @param httpMethod
     * @return
     * @throws ArrowheadException
     * @throws NullPointerException
     * @throws InterruptedException
     */
    public List<OrchestrationResultDTO> orchestrate(final String serviceDefinition, final String serviceRequirement, HttpMethod httpMethod) throws ArrowheadException, NullPointerException, InterruptedException {
        return this.orchestrate(serviceDefinition, serviceRequirement, httpMethod, new HashMap<String, String>());
    }

    /**
     * request a orchestration
     *
     * @param serviceDefinition
     * @param serviceRequirement
     * @param httpMethod
     * @param metadata
     * @return
     * @throws ArrowheadException
     * @throws InterruptedException
     */
    public List<OrchestrationResultDTO> orchestrate(final String serviceDefinition, final String serviceRequirement, HttpMethod httpMethod, Map<String, String> metadata) throws ArrowheadException, InterruptedException {
        int retryCounter = 1;
        OrchestrationResponseDTO orchestrationResponse = null;
        ServiceQueryFormDTO serviceQueryForm = null;

        // set service information
        if (this.enableVersion)
            serviceQueryForm = new ServiceQueryFormDTO.Builder(serviceDefinition)
                    .interfaces(serviceRequirement)                         // interface
                    .version(this.minVersion, this.maxVersion)                        // set service version
                    .metadata("http-method", httpMethod.name())             // metadata for http method
                    .metadata(metadata)                                     // set other meta descriptions
                    .security(ServiceSecurityType.NOT_SECURE)               // set no security information needed
                    .build();
        else
            serviceQueryForm = new ServiceQueryFormDTO.Builder(serviceDefinition)
                    .interfaces(serviceRequirement)                         // interface
                    .metadata("http-method", httpMethod.name())             // metadata for http method
                    .metadata(metadata)                                     // set other meta descriptions
                    .security(ServiceSecurityType.NOT_SECURE)               // set no security information needed
                    .build();

        Builder orchestrationFormBuilder = arrowheadService.getOrchestrationFormBuilder();

        // set orchestration flags
        OrchestrationFormRequestDTO orchestrationFormRequest = orchestrationFormBuilder
                .requestedService(serviceQueryForm)
                .flag(Flag.MATCHMAKING, this.orchestrationOptionMatchmaking)                            // return only matched service names
                .flag(Flag.OVERRIDE_STORE, this.orchestrationOptionOverrideStore)                       // overwrite orchestration store
                .flag(Flag.METADATA_SEARCH, this.orchestrationOptionMetadataSearch)                     // search with metadata
                .flag(Flag.ENABLE_INTER_CLOUD, this.orchestrationOptionEnableInterCloud)                // check use other clouds
                .flag(Flag.ENABLE_QOS, this.orchestrationOptionEnableQos)                               // use QoS
                .flag(Flag.EXTERNAL_SERVICE_REQUEST, this.orchestrationOptionExternalServiceRequest)
                .flag(Flag.ONLY_PREFERRED, this.orchestrationOptionOnlyPreferred)                       // only check preferred services
                .flag(Flag.TRIGGER_INTER_CLOUD, this.orchestrationOptionTriggerInterCloud)              // request other clouds
                .flag(Flag.PING_PROVIDERS, this.orchestrationOptionPingProviders)                       // ping providers
                .build();

        //perform requests until is successful or maximum of tries reached
        do {
            orchestrationResponse = arrowheadService.proceedOrchestration(orchestrationFormRequest);

            if ((orchestrationResponse == null || orchestrationResponse.getResponse().isEmpty()) && this.orchestrationMaxRetry > 0) {
                System.err.println("Response from Orchestration is empty or not available");
                TimeUnit.SECONDS.sleep(this.orchestrationRetryWait);
            } else
                break;
        } while((retryCounter++) < this.orchestrationMaxRetry);

        // throw exception on empty response
        if (orchestrationResponse == null || orchestrationResponse.getResponse().isEmpty())
            throw new ArrowheadException("Response from Orchestration is empty or not available");

        // return result
        return orchestrationResponse.getResponse();
    }

    /**
     * validate result of orchestration
     *
     * @param orchestrationResult
     * @param serviceDefinition
     * @return
     */
    public boolean validateOrchestrationResult(final OrchestrationResultDTO orchestrationResult, final String serviceDefinition) {
        boolean hasValidInterface = false;

        if (!orchestrationResult.getService().getServiceDefinition().equalsIgnoreCase(serviceDefinition))
            throw new InvalidParameterException("Requested and orchestrated service definition do not match");

        for (final ServiceInterfaceResponseDTO serviceInterface : orchestrationResult.getInterfaces()) {
            if (serviceInterface.getInterfaceName().equalsIgnoreCase(getInterface())) {
                hasValidInterface = true;
                break;
            }
        }

        if (!hasValidInterface)
            throw new InvalidParameterException("Requested and orchestrated interface do not match");
        else
            return true;
    }

    /**
     * return of the interface to be used
     * decision based on the "SSL enabled" property
     *
     * @return
     */
    private String getInterface() {
        return sslProperties.isSslEnabled() ? interfaceSecure : interfaceInsecure;
    }

    //-------------------------------------------------------------------------------------------------

    /**
     * set the secure interface name
     * @param interfaceSecure
     */
    public void setInterfaceSecure(String interfaceSecure) {
        this.interfaceSecure = interfaceSecure;
    }

    /**
     * set the insecure interface name
     * @param interfaceInsecure
     */
    public void setInterfaceInsecure(String interfaceInsecure) {
        this.interfaceInsecure = interfaceInsecure;
    }

    /**
     * set minimum version for requested service
     * @param minVersion
     */
    public void setMinVersion(int minVersion) {
        this.minVersion = minVersion;
    }

    /**
     * set maximum version for requested service
     * @param maxVersion
     */
    public void setMaxVersion(int maxVersion) {
        this.maxVersion = maxVersion;
    }

    /**
     * set the maximum trials until the orchestration request stops if it fails
     * @param orchestrationMaxRetry
     */
    public void setOrchestrationMaxRetry(int orchestrationMaxRetry) {
        this.orchestrationMaxRetry = orchestrationMaxRetry;
    }

    /**
     * set the maximal wait time to wait after an orchestration request fails
     * @param orchestrationRetryWait
     */
    public void setOrchestrationRetryWait(int orchestrationRetryWait) {
        this.orchestrationRetryWait = orchestrationRetryWait;
    }

    /**
     * set orchestration flag for matchmaking
     * @param orchestrationOptionMatchmaking
     */
    public void setOrchestrationOptionMatchmaking(boolean orchestrationOptionMatchmaking) {
        this.orchestrationOptionMatchmaking = orchestrationOptionMatchmaking;
    }

    /**
     * set orchestration flag for overriding store
     * @param orchestrationOptionOverrideStore
     */
    public void setOrchestrationOptionOverrideStore(boolean orchestrationOptionOverrideStore) {
        this.orchestrationOptionOverrideStore = orchestrationOptionOverrideStore;
    }

    /**
     * set orchestration flag for searching with metadata
     * @param orchestrationOptionMetadataSearch
     */
    public void setOrchestrationOptionMetadataSearch(boolean orchestrationOptionMetadataSearch) {
        this.orchestrationOptionMetadataSearch = orchestrationOptionMetadataSearch;
    }

    /**
     * set orchestration flag for enable intercloud searching
     * @param orchestrationOptionEnableInterCloud
     */
    public void setOrchestrationOptionEnableInterCloud(boolean orchestrationOptionEnableInterCloud) {
        this.orchestrationOptionEnableInterCloud = orchestrationOptionEnableInterCloud;
    }

    /**
     * set orchestration flag for enabling QoS
     * @param orchestrationOptionEnableQos
     */
    public void setOrchestrationOptionEnableQos(boolean orchestrationOptionEnableQos) {
        this.orchestrationOptionEnableQos = orchestrationOptionEnableQos;
    }

    /**
     * set orchestration flag for request external services
     * @param orchestrationOptionExternalServiceRequest
     */
    public void setOrchestrationOptionExternalServiceRequest(boolean orchestrationOptionExternalServiceRequest) {
        this.orchestrationOptionExternalServiceRequest = orchestrationOptionExternalServiceRequest;
    }

    /**
     * set orchestration flag for return only preferred services
     * @param orchestrationOptionOnlyPreferred
     */
    public void setOrchestrationOptionOnlyPreferred(boolean orchestrationOptionOnlyPreferred) {
        this.orchestrationOptionOnlyPreferred = orchestrationOptionOnlyPreferred;
    }

    /**
     * set orchestration flag for trigger inter cloud requests
     * @param orchestrationOptionTriggerInterCloud
     */
    public void setOrchestrationOptionTriggerInterCloud(boolean orchestrationOptionTriggerInterCloud) {
        this.orchestrationOptionTriggerInterCloud = orchestrationOptionTriggerInterCloud;
    }

    /**
     * set orchestration flag to ping providers to test if available
     * @param orchestrationOptionPingProviders
     */
    public void setOrchestrationOptionPingProviders(boolean orchestrationOptionPingProviders) {
        this.orchestrationOptionPingProviders = orchestrationOptionPingProviders;
    }
}
