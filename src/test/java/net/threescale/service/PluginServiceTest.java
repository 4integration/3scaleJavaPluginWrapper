package net.threescale.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import threescale.v3.api.AuthorizeResponse;
import threescale.v3.api.ServerError;
import threescale.v3.api.ServiceApi;


public class PluginServiceTest{
    private final String SUCCESSFUL_RESP = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status><authorized>true</authorized><plan>Application Plan</plan></status>";
    private final String FAILED_RESP = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status code=\"409\"><authorized>false</authorized><reason>usage limits are exceeded</reason><plan>Application Plan</plan></status>";
    
    private PluginServiceImpl pluginServiceUnderTest; 
    private ServiceApi mockServiceApi;
    private Properties mockProps;
    private Map <String, Boolean> mockAuthorizations;
    
    @Before 
    public void before() throws Exception {
        
        mockServiceApi = Mockito.mock(ServiceApi.class);
        mockProps = new Properties();
        mockAuthorizations = Mockito.mock(Map.class);
       
        pluginServiceUnderTest = new PluginServiceImpl();
        pluginServiceUnderTest.setAuthorizations(mockAuthorizations);
        pluginServiceUnderTest.setProps(mockProps);
        pluginServiceUnderTest.setServiceApi(mockServiceApi);
    }

    
    @Test (expected = ServerError.class) 
    public void testServerErrorNoServiceId() throws ServerError  {    	
    	mockProps.setProperty("method-token", "servicetoken");
    	pluginServiceUnderTest.setProps(mockProps);     
        pluginServiceUnderTest.authRep("userKey", "method");
    }
    
    @Test (expected = ServerError.class) 
    public void testServerErrorNoServiceToken() throws ServerError  {
        mockProps.setProperty("method-serviceid", "serviceId");      
        pluginServiceUnderTest.setProps(mockProps);
        pluginServiceUnderTest.authRep("userKey", "method");
    }

    
    @Test 
    public void testSuccessfulAuthEmptyCache() throws ServerError  {
    	mockProps.setProperty("method-token", "servicetoken"); 
    	mockProps.setProperty("method-serviceid", "serviceId");        
        
        AuthorizeResponse successfulResponse = new AuthorizeResponse(200, SUCCESSFUL_RESP);
        when(mockServiceApi.authrep(any(), any(), any())).thenReturn(successfulResponse);
        
        AuthorizeResponse response = pluginServiceUnderTest.authRep("userKey", "method");
        //A non-null response  is what we expect from a synchronous call - made when <key>+<method or metric> not found in the cache
        assertNotNull(response);
        assertEquals(response.success(), true);
    }

    @Test 
    public void testFailedAuthEmptyCache() throws ServerError  {
    	mockProps.setProperty("method-token", "servicetoken"); 
    	mockProps.setProperty("method-serviceid", "serviceId");        
        
         AuthorizeResponse failResponse = new AuthorizeResponse(403, FAILED_RESP);
        when(mockServiceApi.authrep(any(), any(), any())).thenReturn(failResponse);
        
        AuthorizeResponse response = pluginServiceUnderTest.authRep("userKey", "method");
        //A non-null response  is what we expect from a synchronous call - made when <key>+<method or metric> not found in the cache
        assertNotNull(response);
        assertEquals(response.success(), false);
    }

    
    @Test 
    public void testValueInCacheAsynchAuth() throws ServerError  {
    	mockProps.setProperty("method-token", "servicetoken"); 
    	mockProps.setProperty("method-serviceid", "serviceId");        
        
    	when(mockAuthorizations.get("userKeymethod")).thenReturn(true);
                
        AuthorizeResponse response = pluginServiceUnderTest.authRep("userKey", "method");
        //A null response  is what we expect from an asynchronous call - made when <key>+<method or metric> is found in the cache. 
        //Return null as we don't wait for the response
        assertNull(response);
     }

 
    
}