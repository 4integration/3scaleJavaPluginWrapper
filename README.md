Disclaimer: The code in this repo is not supported by Red Hat/3scale. Rather it's example code of how you can and we did achieve very low latency API Management. We did this by applying a wrapper around the supported 3scale Java Plugin which uses caching and asynchronous calls to 3scale. More details below.

# 3scaleJavaPluginWrapper
In 3scale as well as using API Gateways to intercept incoming API requests and Authorize and Report them, we also expose several code plugins for popular programming languages - Java, .NET, Ruby, Python, Node JS etc. For more on that see https://support.3scale.net/docs/deployment-options/plugin-setup. The Java Plugin is available here: https://github.com/3scale/3scale_ws_api_for_java. This solution describes a caching wrapper for this Java Plugin. 

It caches previous AuthRep (Authorize + Report rolled up into one) results to determine if communication with 3scale API Manager should be synchronous or asynchronous. If the previous call was successful, communication is asynchronous; otherwise synchronous. If client stays within their rate limits, it enables almost latency free API Management.
It assumes you use API Key as the Authentication mode. It can be easily modified to use other modes.

In 3scale we offer a lot of flexibility as to how you implement your solutions. This is one example of how to use the product, specifically the Java Plug In.


Instructions.

1) Git clone and mvn install the 3scale Java Plugin: https://github.com/3scale/3scale_ws_api_for_java

2) Git clone this repo (3scaleJavaPluginWrapper). Make the following modifications
    
- Edit pom.xml if necessary - ensuring the version of the dependency with groupId net.3scale and artifactId 3scale-api has the same version as the Java Plugin in the previous step.

- Edit and update /src/main/resources/props.properties. Each 3scale Service, defined under the API Menu in the 3scale API Manager, has a Service ID and a Service Token. You need to copy these from the 3scale API Manager to /src/main/resources/props.properties. 

To get the Service ID, go into the Definition of each API and you will see it in the browser address bar following the '/services' URL fragment. To get the Service Token(s), click: gear sign on the top right of the screen -> Personal Settings -> Tokens. For every service you have, there will be a Service Token.
methodOrMetricSystemName=<serviceid retrieved from 3scale API Manager>
methodOrMetricSystemName=<serviceToken retrieved from 3scale API Manager>

3) Save these changes and run mvn install.

4) Add a dependency to this repo in your Java Application's POM file.

5) In your API code, add the wrapper call. userKey will likely have been passed in as a query or header parameter. Exit if unauthorized. Something like this :
            
        
        import net.threescale.service.PluginService;
        
        ....
        
        AuthorizeResponse auth = pluginService.authRep("<API Key retrieved from incoming request>", "<3scale method system name>");
        if (auth!=null && (!auth.success())){           
            response.setResponseCode(403);
            return;
        }
        //Authorization succeeded if we reach this point - we either got a null response
        //(previous call was successful - and we don't wait for a response) or auth.success() was true
        //.... continue with/to your API code

Note - this may appear to closely couple your Java code to your 3scale configuration, i.e. your Java code needs to know your 3scale system name. However this can be avoided by using a standardized naming convention for your 3scale method names, e.g. the URL pattern: 
GET /catalogs{id}
could correspond to the 3scale system name: get-catalogs-id

This would also allow you to make the calls to the wrapper inside a dedicated module like a Servlet Filter, Interceptor or other cross cutting concern. We recommend this instead of having the calls to the Plugin inside your API code.
The module would just need to dynamically construct the system name of the method off the URL path and then call the wrapper. This way API Management is separated from API implementation.

6) These are example latencies I achieved using my test. 
In the first, I hit an API endpoint 1000 times. I use 10 threads in JMeter - each one hitting the API 100 times. There is no Java Plugin Wrapper in use on this endpoint. Average latencies are shown.
![direct](https://cloud.githubusercontent.com/assets/5570713/22908137/c5bf5634-f21a-11e6-99e9-3ff9c1232d4f.png)

In the second, I apply the same test to an identical endpoint. Average latencies are shown - 1 ms slower on average than the first endpoint.

![managed](https://cloud.githubusercontent.com/assets/5570713/22908148/cc7751e8-f21a-11e6-8602-2e06680f016f.png)

Summary

This example solution is a wrapper around the 3scale Java Plugin that offers near zero millisecond API Management. It's useful for say microservice to microservice calls where gateway usage adds too much complexity and latency. The code fragment (section 5) should be added as a Servlet filter, an inceptor or cross cutting concern or indeed its own injected class. 

