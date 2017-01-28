package net.threescale.service;

import java.util.regex.Pattern;

/**
 * 
 * @author tomcorcoran
 * POJO holding the elements of a 3scale Mapping: 
 * 	- URL Pattern of incoming endpoint or request
 * 	- Service Id of 3scale Service or API the endpoint belongs to
 *  - HTTP verb
 *  - The Pattern of the URL - it can be literal or Regex
 *  These elements are found on 3scale on the API->Integrations screen and
 *  service id in browser address bar when on this screen. 
 */
public class ThreeScaleMapping {
	private String httpVerb, serviceId, metricOrMethod;
	private Pattern pattern;
	
	public ThreeScaleMapping() {
		super();
	}

	public ThreeScaleMapping(String httpVerb, String serviceId,
			String metricOrMethod, Pattern pattern) {
		super();
		this.httpVerb = httpVerb;
		this.serviceId = serviceId;
		this.metricOrMethod = metricOrMethod;
		this.pattern = pattern;
	}

	public String getHttpVerb() {
		return httpVerb;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getMetricOrMethod() {
		return metricOrMethod;
	}

	public Pattern getPattern() {
		return pattern;
	}

	
}
