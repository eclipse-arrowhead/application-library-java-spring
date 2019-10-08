package eu.arrowhead.client.library.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.http.HttpService;

@Component
public class ArrowheadBeans {

	//=================================================================================================
	// members
	
	@Autowired
	private static ArrowheadService arrowheadService;
	
	@Autowired
	private static SSLProperties sslProperties;
	
	@Autowired
	private static HttpService httpService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	/** 
	 * @return the ArrowheadService spring managed bean
	 */
	public static ArrowheadService getArrowheadService() {
		return arrowheadService;
	}
	
	//-------------------------------------------------------------------------------------------------
	/** 
	 * @return the SSLProperties spring managed bean
	 */
	public static SSLProperties getSSLProperties() {
		return sslProperties;
	}
	
	//-------------------------------------------------------------------------------------------------
	/** 
	 * @return the HttpService spring managed bean
	 */
	public static HttpService getHttpService() {
		return httpService;
	}
}
