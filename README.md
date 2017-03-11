Disclaimer: The code in this repo is not supported by Red Hat/3scale. Rather it's example code of how you can and we did achieve very low latency API Management. We did this by applying a wrapper around the 3scale Java Plugin which uses caching and asynchronous calls to 3scale. More details below.

# 3scaleJavaPluginWrapper
This is a caching wrapper for the 3scale Java Plugin. It caches previous AuthRep results to determine if communication with 3scale API Manager should be synchronous or asynchronous. If the previous call was successful, communication is asynchronous; otherwise synchronous. If client stays within their rate limits, it enables almost latency free API Management.
It assumes you use API Key as the Authentication mode. It can be easily modified to use other modes.

In 3scale we offer a lot of flexibility as to how you implement your solutions. This is one example of how to use the product, specifically the Java Plug In.


Instructions.

1) Git clone and mvn install the 3scale Java Plugin: https://github.com/3scale/3scale_ws_api_for_java

2) Git clone this repo. Make the following modifications
    
- Edit pom.xml if necessary - ensuring the version of the dependency with groupId net.3scale and artifactId 3scale-api has the same version as the Java Plugin in the previous step.

- Edit and update /src/main/resources/props.properties. Each 3scale Service, defined under the API Menu, has a Service ID and a Service Token. You need to copy these from the 3scale API Manager. To get the Service ID, go into the Definition of each API and you will see it in the browser address bar following the '/services' URL fregment. To get the Service Token(s), click: gear sign on the top right of the screen -> Personal Settings -> Tokens. For every service you have, there will be a Service Token.

The next thing you need to do is retrieve your 3scale Method and Metric system names and map each to the service id and service token to which they belong. Inside each Service, click Integration. Go to the Mapping Rules section and click Define. You will see a list of methods and metrics. Copy the system names of each one that you want to use the Plugin Wrapper to manage. Make 2 entries for each one in props.properties:

<method or metric system name>-serviceid=<that service's service id>

<method or metric system name>-token=<that service's service token>

3) Save these changes and run mvn install.

4) Add a dependency to this repo in your Java Application's POM file.

5) In your API code, add the wrapper call. userKey will likely have been passed in as a query or header parameter. Exit if unauthorized. Something like this :
            
        
        import net.threescale.service.PluginService;
        
        ....
        
    	boolean authorized = pluginService.authRep(userKey, <method or metric system name>);
    	if (!authorized){
    		response.setStatus(403);
    		return new Result(0, "ERROR - UNAUTHROIZED", "");
    	}


	    .... continue with/to your API code

Note - this may appear to closely couple your Java code to your 3scale configuration, i.e. your Java code needs to know your 3scale system name. However this can be avoided by using a standardized naming convention for your 3scale method names, e.g. the URL pattern: 
GET /catalogs{id}/products/{id}/listings{month} 

could correspond to the 3scale system name: get-catalogs-id-products-id-listings-month. 

This would also allow you to make the calls to the wrapper inside a dedicated module like a Servlet Filter, Interceptor or other cross cutting concern. 
The module would just need to dynamically construct the system name of the methoid off the URL path and then call the wrapper. This way API Management is separated from API implementation.

6) These are example latencies I achieved using my test. 
In the first, I hit an API endpoint 1000 times. I use 10 threads in JMeter - each one hitting the API 100 times. There is no Java Plugin Wrapper in use on this endpoint. Average latencies are shown.
![direct](https://cloud.githubusercontent.com/assets/5570713/22908137/c5bf5634-f21a-11e6-99e9-3ff9c1232d4f.png)

In the second, I apply the same test to an identical endpoYou don't want the internal Java code needing to know about a Java system name of a mapping it relates to. But if a convention like the following was used, it would be acceptable, e.g. the URL pattern: GET /catalogs{id}/products/{id}/listings{month} could correspond to the 3scale system name: get-catalogs-id-products-id-listings-month. The naming convention is enough for the Java coder to know what the 3scale system name is. Therefore no looping, no regex to get system name. Instead pass it in.int - except for the inclusion of this Java Plugin Wrapper. Average latencies are shown - 1 ms slower on average than the first endpoint.
![managed](https://cloud.githubusercontent.com/assets/5570713/22908148/cc7751e8-f21a-11e6-8602-2e06680f016f.png)

Summary

This solution is a wrapper around the 3scale Java Plugin that offers near zero millisecond API Management. It's useful for say microservice to microservice calls where gateway usage adds too much complexity and latency. The code fragment (section 5) could be added as a Servlet filter, an inceptor or cross cutting concern or indeed its own injected class. 

