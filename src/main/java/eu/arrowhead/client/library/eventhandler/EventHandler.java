package eu.arrowhead.client.library.eventhandler;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Event Handler
 */
@Component
public class EventHandler {
    @Autowired
    private ArrowheadService arrowheadService;

    @Autowired
    private ConfigEventProperties configEventProperties;

    @Autowired
    protected SSLProperties sslProperties;

    @Value("${arrowhead.client.subscriber.base-notification-uri:}")
    private String baseNotificationUri;

    @Value("${arrowhead.client.system.name}")
    private String clientSystemName;

    @Value("${server.address}")
    private String clientSystemAddress;

    @Value("${server.port}")
    private int clientSystemPort;

    /**
     * subscribe publisher
     *
     * @param events
     */
    public void subscribe(Map<String, String> events) {
        this.subscribe(null, events);
    }

    /**
     * subscribe publisher
     *
     * @param providers
     */
    public void subscribe(final Set<SystemResponseDTO> providers) {
        this.subscribe(providers, configEventProperties.subscriberEvents());
    }

    /**
     * subscribe publisher
     *
     * @param providers
     * @param events
     */
    public void subscribe(final Set<SystemResponseDTO> providers, Map<String, String> events) {
        // subscribe to every defined event in application.properties
        events.forEach((event, eventUri) -> {
            if (eventUri.startsWith("/"))
                eventUri = eventUri.substring(1);

            SubscriptionRequestDTO subscription = this.createSubscriptionRequestDTO(event, this.getInitialSystemDto(), eventUri);

            if (providers != null && !providers.isEmpty())
                subscription.setSources(this.getSourcesFromProviders(providers));

            // unsubscribe from existing abonnement and resubscribe to it
            try {
                this.unsubscribe(event);
            } catch(InvalidParameterException e) {}

            arrowheadService.subscribeToEventHandler(subscription);
        });
    }

    /**
     * unsubscribe from publisher
     *
     * @param event
     */
    public void unsubscribe(String event) {
        arrowheadService.unsubscribeFromEventHandler(event, this.clientSystemName, this.clientSystemAddress, this.clientSystemPort);
    }

    /**
     * publish message
     *
     * @param event
     * @param metadata
     * @param payload
     */
    public void publish(String event, Map<String, String> metadata, String payload) {
        // set timestamp (UTC) and create message object
        final String timeStamp = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
        final EventPublishRequestDTO request = new EventPublishRequestDTO(event, this.getInitialSystemDto(), metadata, payload, timeStamp);

        // publish message
        arrowheadService.publishToEventHandler(request);
    }

    /**
     * return source system information
     * @param providers
     * @return
     */
    private Set<SystemRequestDTO> getSourcesFromProviders(Set<SystemResponseDTO> providers) {
        Set<SystemRequestDTO> sources = new HashSet<>(providers.size());

        providers.forEach(provider -> {
            SystemRequestDTO source = new SystemRequestDTO();

            source.setSystemName(provider.getSystemName());
            source.setAddress(provider.getAddress());
            source.setPort(provider.getPort());

            sources.add(source);
        });

        return sources;
    }

    /**
     * fill SubscriptionRequestDTO with information
     * @param eventType
     * @param subscriber
     * @param notificationUri
     * @return
     */
    private SubscriptionRequestDTO createSubscriptionRequestDTO(final String eventType, final SystemRequestDTO subscriber, final String notificationUri) {
        return createSubscriptionRequestDTO(eventType.toUpperCase(), subscriber, null,
                notificationUri, false, null, null, null);
    }

    /**
     * fill SubscriptionRequestDTO with information
     * @param eventType
     * @param subscriber
     * @param notificationUri
     * @return
     */
    private SubscriptionRequestDTO createSubscriptionRequestDTO(
            final String eventType,
            final SystemRequestDTO subscriber,
            final Map<String, String> filterMetaData,
            final String notificationUri,
            boolean matchMetaData,
            String startDate,
            String endDate,
            Set<SystemRequestDTO> sources
    ) {
        return new SubscriptionRequestDTO(
                eventType.toUpperCase(),
                subscriber,
                filterMetaData,
                this.baseNotificationUri + "/" + notificationUri,
                matchMetaData,
                startDate,
                endDate,
                sources
        );
    }

    /**
     * fill SystemRequestDTO with source system information
     * @return
     */
    private SystemRequestDTO getInitialSystemDto() {
        final SystemRequestDTO source = new SystemRequestDTO();

        source.setSystemName(this.clientSystemName);
        source.setAddress(this.clientSystemAddress);
        source.setPort(this.clientSystemPort);

        // set security information
        if (sslProperties.isSslEnabled())
            source.setAuthenticationInfo( Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));

        return source;
    }

    /**
     * return event name from given key
     * @param key
     * @return
     */
    public String getEvent(String key, boolean publisher) {
        return (publisher ? configEventProperties.publisherEvents().get(key) :
                configEventProperties.subscriberEvents().get(key));
    }

    /**
     * get defined events from configuration
     * @return
     */
    public Map<String, String> getConfigEventProperties(boolean publisher) {
        return (publisher ? configEventProperties.publisherEvents() : configEventProperties.subscriberEvents());
    }

    /**
     * set the base notification uri for event requests
     * @param baseNotificationUri
     */
    public void setBaseNotificationUri(String baseNotificationUri) {
        this.baseNotificationUri = baseNotificationUri;
    }

    /**
     * set the client system name of registering service
     * @param clientSystemName
     */
    public void setClientSystemName(String clientSystemName) {
        this.clientSystemName = clientSystemName;
    }

    /**
     * set client system address of registering service
     * @param clientSystemAddress
     */
    public void setClientSystemAddress(String clientSystemAddress) {
        this.clientSystemAddress = clientSystemAddress;
    }

    /**
     * set client system port of registering service
     * @param clientSystemPort
     */
    public void setClientSystemPort(int clientSystemPort) {
        this.clientSystemPort = clientSystemPort;
    }
}
