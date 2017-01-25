package net.threescale.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import threescale.v3.api.AuthorizeResponse;
import threescale.v3.api.ParameterMap;
import threescale.v3.api.ServerError;
import threescale.v3.api.ServiceApi;
import threescale.v3.api.impl.ServiceApiDriver;

/**
 * 
 * @author tomcorcoran
 * Implementation of the 3scale Java Plugin Wrapper
 *
 */
@Service
public class PluginServiceImpl implements PluginService{
	private Map <String, ThreeScaleMapping> mappings = null;
	
	/** Previous Authorization results **/
	private Map <String, Boolean> authorizations = new HashMap<String, Boolean>();
	
	
	/**
	 * Constructor where mappings is initialized with the mappings you defined on the API->Integration screen 
	 * on 3scale. In future enhancements, we will pull in these using the 3scale API. 
	 */
	public PluginServiceImpl() {
		super();
		mappings = new HashMap<String, ThreeScaleMapping>();
		mappings.put("/<url-endpoint-1>", new ThreeScaleMapping("<HTTP-Method-1>", "<Service-id-1>", "<method-or-metric-1-system-name>"));
		
	}

	
	/**
	 * Main Implementing method. If the cache (authorizations) has stored 'true' against userKey:MetricOrMethod, make an
	 * asynchronous call to 3scale, otherwise make a synchronous call. 
	 */
	@Override
	public AuthorizeResponse authRep(String userKey, String requestPath) {
		
		//Note - an important enhancement will be to generalize requestPath, 
		//replacing path variables with any placeholders contained in the mapping key. 
		ThreeScaleMapping mapping = mappings.get(requestPath);
		AuthorizeResponse resp = null;
		
		String key = userKey+mapping.getMetricOrMethod();
		Boolean auth = authorizations.get(key);
		if (auth!=null && (auth==true)){
			Thread asyncAuth = new ASyncAuth(userKey, requestPath, mapping);
			asyncAuth.start();
		}
		else{

			return getSyncAuthResponse(userKey, requestPath, mapping);
			
		}
    	return resp;
	}
	
	/**
	 * @author tomcorcoran
	 * Used for asynchronous calls.
	 *
	 */
	class ASyncAuth extends Thread {
		String userKey, requestPath;
		ThreeScaleMapping mapping;
		ASyncAuth(String uKey, String rPath, ThreeScaleMapping mping){
			userKey = uKey;
			requestPath = rPath;
			mapping = mping;
		}
	    public void run(){
	    	AuthorizeResponse resp = getAuthResponse(userKey, requestPath, mapping);
	    }
	    
	  }
	
	
    /**
     * Used for synchronous calls
     */
	private AuthorizeResponse getSyncAuthResponse(String userKey, String requestPath, ThreeScaleMapping mapping){
		return getAuthResponse(userKey, requestPath, mapping);
	}
	
	private AuthorizeResponse getAuthResponse(String userKey, String requestPath, ThreeScaleMapping mapping){
 

    	AuthorizeResponse authorizeResponse = null;
    	ServiceApi serviceApi = new ServiceApiDriver(PluginService.PROVIDER_KEY);
    	
    	ParameterMap paramMap = buildParameterMap(mapping.getServiceId(), userKey, mapping.getMetricOrMethod(), 1);

    	try {
    		authorizeResponse = serviceApi.authrep(paramMap);
		} catch (ServerError e) {
			e.printStackTrace();
		}
    	
    	authorizations.put(userKey+mapping.getMetricOrMethod(), authorizeResponse.success());
    	
       	return authorizeResponse;
		
	}
	
	
    private ParameterMap buildParameterMap(String serviceId, String userKey, String metric, Integer incrementBy){
    	ParameterMap params = new ParameterMap();
		params.add("service_id", serviceId);
		params.add("user_key", userKey);		
    			
		ParameterMap usage = new ParameterMap();
		usage.add(metric, incrementBy.toString());
		params.add("usage", usage);
		
		return params;
    }

    
}
