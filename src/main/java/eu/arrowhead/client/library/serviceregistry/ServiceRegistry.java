package eu.arrowhead.client.library.serviceregistry;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.client.library.misc.Miscellaneous;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Registry
 */
@Component
public class ServiceRegistry {
    @Autowired
    private ArrowheadService arrowheadService;

    @Value("${arrowhead.client.system.name}")
    private String clientSystemName;
    @Value("${server.address}")
    private String clientSystemAddress;
    @Value("${server.port}")
    private int clientSystemPort;

    @Value("${arrowhead.client.system.interface.secure}")
    private String interfaceSecure;
    @Value("${arrowhead.client.system.interface.insecure}")
    private String interfaceInsecure;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;
    @Value("${token.security.filter.enabled:false}")
    private boolean tokenSecurityFilterEnabled;

    /**
     * Register a service independent of the client
     *
     * @param clientSystemName
     * @param clientSystemAddress
     * @param clientSystemPort
     * @param tokenSecurityFilterEnabled
     * @param sslEnabled
     * @param interfaceInsecure
     * @param interfaceSecure
     * @param serviceDefinition
     * @param serviceUri
     * @param httpMethod
     * @param metadata
     */
    public void registerExternalService(
            String clientSystemName,
            String clientSystemAddress,
            int clientSystemPort,
            boolean tokenSecurityFilterEnabled,
            boolean sslEnabled,
            String interfaceInsecure,
            String interfaceSecure,
            String serviceDefinition,
            String serviceUri,
            HttpMethod httpMethod,
            Map<String, String> metadata
    ) {
        final ServiceRegistryRequestDTO serviceRegistryRequest = new ServiceRegistryRequestDTO();
        final SystemRequestDTO systemRequest = new SystemRequestDTO();

        if (clientSystemAddress.matches("169.[0-9]+.[0-9]+.[0-9]+") || clientSystemAddress.equals("0.0.0.0"))
            throw new ArrowheadException("No link local or meta addresses allowed");

        // set service definition
        serviceRegistryRequest.setServiceDefinition(serviceDefinition);

        // set system description
        systemRequest.setSystemName(clientSystemName);
        systemRequest.setAddress(clientSystemAddress);
        systemRequest.setPort(clientSystemPort);

        // set interfaces and security description
        if (tokenSecurityFilterEnabled) {
            systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
            serviceRegistryRequest.setSecure(String.valueOf(ServiceSecurityType.TOKEN));
            serviceRegistryRequest.setInterfaces(List.of(interfaceSecure));
        } else if (sslEnabled) {
            systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
            serviceRegistryRequest.setSecure(String.valueOf(ServiceSecurityType.CERTIFICATE));
            serviceRegistryRequest.setInterfaces(List.of(interfaceSecure));
        } else {
            serviceRegistryRequest.setSecure(String.valueOf(ServiceSecurityType.NOT_SECURE));
            serviceRegistryRequest.setInterfaces(List.of(interfaceInsecure));
        }

        serviceRegistryRequest.setProviderSystem(systemRequest);
        serviceRegistryRequest.setServiceUri(serviceUri);
        serviceRegistryRequest.setMetadata(new HashMap<>());

        // set meta description
        if (httpMethod != null)
            serviceRegistryRequest.getMetadata().put("http-method", httpMethod.name());

        if (metadata != null && !metadata.isEmpty())
            serviceRegistryRequest.getMetadata().putAll(metadata);

        // Register the service
        arrowheadService.forceRegisterServiceToServiceRegistry(serviceRegistryRequest);
    }

    /* --------------------------------------------------------------------------------------------------------------*/
    /* --------------------------------------------------------------------------------------------------------------*/
    /* --------------------------------------------------------------------------------------------------------------*/

    /**
     * register a service
     * if server.address 0.0.0.0 then it will lookup all available ip´s and register them
     *
     * @param serviceDefinition
     * @param serviceUri
     */
    public void register(String serviceDefinition, String serviceUri) throws SocketException {
        this.register(serviceDefinition, serviceUri, null, null);
    }

    /**
     * register a service
     * if server.address 0.0.0.0 then it will lookup all available ip´s and register them
     *
     * @param serviceDefinition
     * @param serviceUri
     * @param httpMethod
     */
    public void register(String serviceDefinition, String serviceUri, HttpMethod httpMethod) throws SocketException {
        this.register(serviceDefinition, serviceUri, httpMethod, null);
    }

    /**
     * register a service
     * if server.address 0.0.0.0 then it will lookup all available ip´s and register them
     *
     * @param serviceDefinition
     * @param serviceUri
     * @param httpMethod
     * @param metadata
     */
    public void register(String serviceDefinition, String serviceUri, HttpMethod httpMethod, Map<String, String> metadata) throws SocketException {
        SystemRequestDTO systemRequestDTO = new SystemRequestDTO();

        systemRequestDTO.setAddress(this.clientSystemAddress);

        // registration attempts with address 0.0.0.0 will be mapped to all available addresses
        if (this.clientSystemAddress.equals("0.0.0.0")) {
            for (InetAddress addr : Miscellaneous.getInetAddressesFromNics()) {
                systemRequestDTO.setAddress(addr.getHostAddress());

                this.register(systemRequestDTO, serviceDefinition, serviceUri, httpMethod, metadata);
            }
        } else
            this.register(systemRequestDTO, serviceDefinition, serviceUri, httpMethod, metadata);
    }

    /**
     * register a service
     *
     * @param systemRequest
     * @param serviceDefinition
     * @param serviceUri
     * @param httpMethod
     * @param metadata
     */
    private void register(SystemRequestDTO systemRequest, String serviceDefinition, String serviceUri,
                         HttpMethod httpMethod, Map<String, String> metadata) {
        final ServiceRegistryRequestDTO serviceRegistryRequest = new ServiceRegistryRequestDTO();
        serviceRegistryRequest.setServiceDefinition(serviceDefinition);

        // set system description
        systemRequest.setSystemName(this.clientSystemName);
        systemRequest.setPort(this.clientSystemPort);

        // set interface and security description
        if (tokenSecurityFilterEnabled) {
            systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
            serviceRegistryRequest.setSecure(String.valueOf(ServiceSecurityType.TOKEN));
            serviceRegistryRequest.setInterfaces(List.of(this.interfaceSecure));
        } else if (sslEnabled) {
            systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
            serviceRegistryRequest.setSecure(String.valueOf(ServiceSecurityType.CERTIFICATE));
            serviceRegistryRequest.setInterfaces(List.of(this.interfaceSecure));
        } else {
            serviceRegistryRequest.setSecure(String.valueOf(ServiceSecurityType.NOT_SECURE));
            serviceRegistryRequest.setInterfaces(List.of(this.interfaceInsecure));
        }

        serviceRegistryRequest.setProviderSystem(systemRequest);
        serviceRegistryRequest.setServiceUri(serviceUri);
        serviceRegistryRequest.setMetadata(new HashMap<>());

        // set meta description
        if (httpMethod != null)
            serviceRegistryRequest.getMetadata().put("http-method", httpMethod.name());

        if (metadata != null && !metadata.isEmpty())
            serviceRegistryRequest.getMetadata().putAll(metadata);

        // register a service
        arrowheadService.forceRegisterServiceToServiceRegistry(serviceRegistryRequest);
    }

    /**
     * unregister a service
     *
     * @param serviceDefinition
     */
    public void unregister(String serviceDefinition) {
        arrowheadService.unregisterServiceFromServiceRegistry(serviceDefinition);
    }

    /**
     * set the client system name of the registering service
     * @param clientSystemName
     */
    public void setClientSystemName(String clientSystemName) {
        this.clientSystemName = clientSystemName;
    }

    /**
     * set the client system addresse of the registering service
     * @param clientSystemAddress
     */
    public void setClientSystemAddress(String clientSystemAddress) {
        this.clientSystemAddress = clientSystemAddress;
    }

    /**
     * set the client system port of the registering service
     * @param clientSystemPort
     */
    public void setClientSystemPort(int clientSystemPort) {
        this.clientSystemPort = clientSystemPort;
    }

    /**
     * set the secure interface name for the registering service
     * @param interfaceSecure
     */
    public void setInterfaceSecure(String interfaceSecure) {
        this.interfaceSecure = interfaceSecure;
    }

    /**
     * set the insecure interface name for the registering service
     * @param interfaceInsecure
     */
    public void setInterfaceInsecure(String interfaceInsecure) {
        this.interfaceInsecure = interfaceInsecure;
    }

    /**
     * set if ssl is enabled on the registering service
     * @param sslEnabled
     */
    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    /**
     * set if token security filter has to be enable
     * @param tokenSecurityFilterEnabled
     */
    public void setTokenSecurityFilterEnabled(boolean tokenSecurityFilterEnabled) {
        this.tokenSecurityFilterEnabled = tokenSecurityFilterEnabled;
    }
}
