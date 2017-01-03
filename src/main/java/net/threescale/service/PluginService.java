package net.threescale.service;

import threescale.v3.api.AuthorizeResponse;

public interface PluginService {
	public String PROVIDER_KEY = "<your-3scale-provider-key>";/
	public AuthorizeResponse authRep(String userKey, String requestPath);
}
