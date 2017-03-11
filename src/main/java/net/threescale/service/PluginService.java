package net.threescale.service;

import threescale.v3.api.AuthorizeResponse;
import threescale.v3.api.ServerError;

public interface PluginService {
    public String PROPERTIES_FILE = "/props.properties";		
    public String SERVICE_ID_SUFFIX = "-serviceid";		
    public String SERVICE_TOKEN_SUFFIX = "-token";		
    public String NO_SERVICE_ID_PROP_WARNING = "NO_SERVICE_ID_PROP_WARNING";		
    public String NO_SERVICE_TOKEN_PROP_WARNING = "NO_SERVICE_TOKEN_PROP_WARNING";		

    public AuthorizeResponse authRep(String userKey, String methodOrMetricSystemName) throws ServerError;
    
}
