package net.threescale.service;
/**
 * 
 * @author tomcorcoran
 * POJO holding the elements of a 3scale Mapping: 
 * 	- URL Pattern of incoming endpoint or request
 * 	- Service Id of 3scale Service or API the endpoint belongs to
 *  - HTTP verb
 *  These elements are found on 3scale on the API->Integrations screen and
 *  service id in browser address bar when on this screen. 
 */
public class ThreeScaleMapping {
	private String httpVerb, serviceId, metricOrMethod;
	
	public ThreeScaleMapping() {
		super();
	}

	public ThreeScaleMapping(String httpVerb, String serviceId,
			String metricOrMethod) {
		super();
		this.httpVerb = httpVerb;
		this.serviceId = serviceId;
		this.metricOrMethod = metricOrMethod;
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

	
}
