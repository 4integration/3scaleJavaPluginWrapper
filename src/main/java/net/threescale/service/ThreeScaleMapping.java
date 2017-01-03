package net.threescale.service;

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
