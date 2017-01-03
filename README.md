# 3scaleJavaPluginWrapper
Caching Wrapper for the 3scale Java Plugin. It caches previous AuthRep result to determine if communication with 3scale SaaS platform should be synchronous or asynchronous. If the previous call was successful, communication is asynchronous; otherwise synchronous. If client stays within their rate limits, it enables effectively latency free API Management.
It assumes you use API Key as the Authentication mode. It can be easily modified to use other modes.

Instructions.

1) Git clone and mvn install the 3scale Java Plugin: https://github.com/3scale/3scale_ws_api_for_java

2) Git clone this repo. Make the following modifications
    - Edit pom.xml - ensuring the version of the dependency with groupId net.3scale and artifactId 3scale-api has the same version as the Java Plugin in the previous step.

    - Edit and update src/main/java/net/threescale/service/PluginService. Set the PROVIDER_KEY to that of your 3scale account. You'll find it in your 3scale account under the gear sign (top right of screen) -> Account. It's the API Key there.

    - Edit and update src/main/java/net/threescale/service/PluginServiceImpl. Go to your Integration screen in 3scale: Dashboard -> API Menu -> Integration. Expand the mapping rules section. In the constructor, PluginServiceImpl(), you'll want to reflect these mappings. For each mapping add the URL pattern, the HTTP verb, Service id - found in the browser address bar url after 'services/' and the method or metric system name. You'll find the latter by clicking Define.
      (We will enhance this to pull in these mappings via the 3scale APIs)

3) Save these changes and mvn install.

4) Add a dependency to this repo in your Java Application's POM file.

5) In your API code, import Plugin Service' and call the its authRep method - exit if unauthorized. Something like this :
            
        import threescale.v3.api.AuthorizeResponse;
        
        import net.threescale.service.PluginService;
        
        ....
        
        AuthorizeResponse authorizeResponse = pluginService.authRep(userKey, request.getServletPath());
        

            if (authorizeResponse!= null && !authorizeResponse.success()){
        
                response.setStatus(403);
        
                return new Greeting(0, "ERROR - UNAUTHROIZED", ""); //Or equivalent!
        
            }