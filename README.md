# 3scaleJavaPluginWrapper
This is a caching wrapper for the 3scale Java Plugin. It caches previous AuthRep result to determine if communication with 3scale SaaS platform should be synchronous or asynchronous. If the previous call was successful, communication is asynchronous; otherwise synchronous. If client stays within their rate limits, it enables almost latency free API Management.
It assumes you use API Key as the Authentication mode. It can be easily modified to use other modes.

In 3scale we offer a lot of flexibility as to how you implement your solutions. This is one example of how to use the product, specifically the Java Plug In.


Instructions.

1) Git clone and mvn install the 3scale Java Plugin: https://github.com/3scale/3scale_ws_api_for_java

2) Git clone this repo. Make the following modifications
    
- Edit pom.xml - ensuring the version of the dependency with groupId net.3scale and artifactId 3scale-api has the same version as the Java Plugin in the previous step.

- Edit and update src/main/java/net/threescale/service/PluginService. Set the PROVIDER_KEY to that of your 3scale account. You'll find it in your 3scale account under the gear sign (top right of screen) -> Account. It's the API Key there.

- Edit and update src/main/java/net/threescale/service/PluginServiceImpl. Go to your Integration screen in 3scale: Dashboard 
-> API Menu -> Integration. Expand the mapping rules section. In the constructor, PluginServiceImpl(), you'll want to 
reflect these mappings. For each mapping:
1) create a Pattern object with the URL pattern - this can be a static path - or a Regex pattern. Note the format of these 	
variable fragments of the path differ from how they are declared on 3scale. So instead of using '/{id}', here with Regex 
you'll use '/(\\w+)' for alphanumeric values or '/(\\d+)' for numerics
2) Create a ThreeScaleMapping object as shown in the PluginServiceImpl class and place it in the mappings collection. 
(You'll find the Service Id when on the 3scale site in the browser address bar url after 'services/'. You'll find the  
method or metric system name - by clicking Define on the Integration page. We will enhance this to pull in these mappings via the 3scale APIs)

Note. Performance may be affected if there are a large number of mappings, particularly Regex patterns. If you see a performance degrade, there are options for improvement. One would be to split the component into more than one - with a smaller number of patterns to loop through at runtime. The ultimate would be to have no lookup at all - rather pass in the 3scale method/metric and service id from the API code. We don't advocate these unless necessary however as they compromise design somewhat - increasing the coupling between the calling code and the service. 

3) Save these changes and mvn install.

4) Add a dependency to this repo in your Java Application's POM file.

5) In your API code, add the wrapper call. userKey will likely have been passed in as a query or header parameter. Exit if unauthorized. Something like this :
            
        import threescale.v3.api.AuthorizeResponse;
        
        import net.threescale.service.PluginService;
        
        ....
        
        AuthorizeResponse authorizeResponse = pluginService.authRep(userKey, request.getServletPath());
        

	    	if (authorizeResponse == null){
	    		response.setStatus(403);
	    		return new Greeting(0, "ERROR - Forbidden Request", "");
	    	}

	    .... continue with your API code

6) These are example latencies I achieved usring my test. 
In the first, I hit an API endpoint 1000 times. I use 10 threads in JMeter - each one hitting the API 100 times. There is no Java Plugin Wrapper in use on this endpoint. Average latencies are shown.
![direct](https://cloud.githubusercontent.com/assets/5570713/22400230/293de02c-e57d-11e6-93c4-a7cdf836fd9a.png)

In the second, I apply the same test to an identical endpoint - except for the inclusion of this Java Plugin Wrapper. Average latencies are shown - 1 ms slower on average than .
![managed](https://cloud.githubusercontent.com/assets/5570713/22400240/32d1c842-e57d-11e6-9f2d-c054478381e0.png)


Summary

This solution is a wrapper around the 3scale Java Plugin that offers near zero millisecond API Management. It's useful for say microservice to microservice calls where gateway usage adds too much complexity and latency. The code fragment (section 5) could be added as a Servlet filter, an inceptor or cross cutting concern or indeed its own injected class. 

