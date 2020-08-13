package eu.arrowhead.client.library.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Event mapping class for application.properties
 * basic path: arrowhead.client.publisher.events.x=y
 * basic path: arrowhead.client.subscriber.events.x=y
 */
public class Events {
    private Map<String, String> events = new HashMap<>();

    public Map<String, String> getEvents() {
        return events;
    }

    public void setEvents(Map<String, String> events) {
        this.events = events;
    }
}
