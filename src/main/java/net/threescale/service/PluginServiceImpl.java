package net.threescale.service;


import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;

import threescale.v3.api.AuthorizeResponse;
import threescale.v3.api.ParameterMap;
import threescale.v3.api.ServerError;
import threescale.v3.api.ServiceApi;
import threescale.v3.api.impl.ServiceApiDriver;

@Service
/**
 * 
 * @author tomcorcoran
 *
 */
public class PluginServiceImpl implements PluginService{
    private Properties props = null;
    private Map <String, Boolean> authorizations = new ConcurrentHashMap<String, Boolean>();
    private ExecutorService executor = null;
    private ServiceApi serviceApi = null;

    public PluginServiceImpl() throws IOException {
        InputStream propsInputStream = this.getClass().getResourceAsStream(PROPERTIES_FILE);	
        props = new Properties();
        props.load(propsInputStream);
        executor = Executors.newFixedThreadPool(1000);
        serviceApi = ServiceApiDriver.createApi();
    }



    @Override
    public AuthorizeResponse authRep(String userKey, String methodOrMetric) throws ServerError {
        String serviceId = props.getProperty(methodOrMetric+SERVICE_ID_SUFFIX);
        String serviceToken = props.getProperty(methodOrMetric+SERVICE_TOKEN_SUFFIX);
        
        if (serviceId == null){
            throw new ServerError(props.getProperty(NO_SERVICE_ID_PROP_WARNING)+ methodOrMetric);
        }
        else if (serviceToken == null){
            throw new ServerError(props.getProperty(NO_SERVICE_TOKEN_PROP_WARNING)+ methodOrMetric);
        }
        
        AuthorizeResponse resp = null;
        
        String key = userKey+methodOrMetric;
        Boolean auth = getFromAuthMap(key);
        if (auth!=null && (auth==true)){
            Runnable asyncAuth = new ASyncAuth(userKey, methodOrMetric, serviceId, serviceToken);
            executor.submit(asyncAuth);
            
        }
        else{
            return getSyncAuthResponse(userKey, methodOrMetric, serviceId, serviceToken);        	
        }
        	
        return resp;
    }
	
    class ASyncAuth implements Runnable {    
        String userKey, methodOrMetric, serviceId, serviceToken;
        ASyncAuth(String uKey, String methodMetric, String servId, String servToken){
            userKey = uKey;
            methodOrMetric = methodMetric;
            serviceId = servId;
            serviceToken = servToken;
        }
        public void run(){
            getAuthResponse(userKey, methodOrMetric, serviceId, serviceToken);
        }
        
    }
	
	
    
    private AuthorizeResponse getSyncAuthResponse(String userKey, String methodOrMetric, String serviceId, String serviceToken){
        return getAuthResponse(userKey, methodOrMetric, serviceId, serviceToken);
    }
	
    private AuthorizeResponse getAuthResponse(String userKey, String methodOrMetric, String serviceId, String serviceToken){
        AuthorizeResponse authorizeResponse = null;    	
        ParameterMap paramMap = buildParameterMap(serviceId, userKey, methodOrMetric, 1);
        
        try {					
            authorizeResponse = serviceApi.authrep(serviceToken, serviceId, paramMap);
        } catch (ServerError e) {
            e.printStackTrace();
        }
        
        if (authorizeResponse.success()){
            putToAuthMap(userKey+methodOrMetric, true);
        }
        else{
            removeFromAuthMap(userKey+methodOrMetric);
        }
         
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
    
    private Boolean getFromAuthMap(String key){
        synchronized (authorizations){
            return authorizations.get(key);
        }
    }

    private void removeFromAuthMap(String key){
        synchronized (authorizations){
            authorizations.remove(key);
        }
    }

    private void putToAuthMap(String key, Boolean success){
        synchronized (authorizations){
            authorizations.put(key, success);
        }    	
    }



    public void setAuthorizations(Map<String, Boolean> authorizations) {
        this.authorizations = authorizations;
    }



    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }



    public void setServiceApi(ServiceApi serviceApi) {
        this.serviceApi = serviceApi;
    }


    public void setProps(Properties props) {
        this.props = props;
    }
}
