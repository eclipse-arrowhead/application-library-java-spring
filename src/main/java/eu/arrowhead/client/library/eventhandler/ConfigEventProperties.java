package eu.arrowhead.client.library.eventhandler;

import eu.arrowhead.client.library.model.Events;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

/**
 * mapping events from application.properties
 * basic path: arrowhead.client.publisher.events.x=y
 * basic path: arrowhead.client.subscriber.events.x=y
 */
@Configuration
@PropertySource("classpath:application.properties")
public class ConfigEventProperties {
	@Bean
	@ConfigurationProperties(prefix = "arrowhead.client.publisher.events")
	public Map<String, String> publisherEvents() {
		return new Events().getEvents();
	}

	@Bean
	@ConfigurationProperties(prefix = "arrowhead.client.subscriber.events")
	public Map<String, String> subscriberEvents() {
		return new Events().getEvents();
	}
}
