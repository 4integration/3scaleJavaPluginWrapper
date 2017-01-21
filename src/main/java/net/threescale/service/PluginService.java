package net.threescale.service;

import threescale.v3.api.AuthorizeResponse;
/**
 * 
 * @author tomcorcoran
 * Interface defining the contract and constants for the 3scale Java Plugin Wrapper
 *
 */
public interface PluginService {
	public String PROVIDER_KEY = "<your-3scale-provider-key>";

	/**
	 * 
	 * @param userKey - will likely have been passed in from the client in a query param or header. 
	 * 	It identifies the client and is used by 3scale to authorize and rate limit. Other modes such 
	 *	 as AppId/AppKey pair and could be used in enhanced versions.   
	 * @param requestPath - the URL path of the request
	 * @return AuthorizeResponse - a 3scale Plugin defined object that contains a flag indicating 
	 * 	whether 3scale authorized the request.
	 */
	public AuthorizeResponse authRep(String userKey, String requestPath);
}
