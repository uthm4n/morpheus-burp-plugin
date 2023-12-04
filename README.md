# morpheus-burp-plugin
Adds a new 'Burp Scan' task type into Morpheus that interacts with Burp Suite Professional's REST API - focused on leveraging Burp Scanner and Morpheus' native reporting capabilities. 

# Requirements
- A valid licensed version of [Burp Suite Professional](https://portswigger.net/burp/pro) 
- Proper network communication between your Morpheus application nodes and Burp
- Burp installed on a dedicated machine on the same network that meets the system requirements outlined [here](https://portswigger.net/burp/documentation/desktop/getting-started/system-requirements)

No requirement to set the proxy settings in Morpheus since that will pass all traffic from Morpheus upstream to Burp, which isn't the goal of the plugin

# Instructions
1. In Burp, navigate to Proxy > Proxy Settings > Edit the default proxy listener > Select 'Specific address' to bind the listener to a specific address (e.g. 10.2.x.x) instead of the loopback interface (127.0.0.1)
2. Navigate to Settings > Suite > REST API
3. Ensure that 'Allow access without API Key (using this option is not secure)' is **unchecked**
4. Generate an API key
5. Copy the API key and API URL 
6. Create a new Burp Scan task in Morpheus (Library > Automation > Tasks)
7. Complete the relevant fields
8. Voilà!

# Build instructions
1. [Clone the repo](https://docs.github.com/en/repositories/creating-and-managing-repositories/cloning-a-repository)
2. Build with `./gradlew shadowJar`


## Note: this plugin is undergoing development so please do not use it! 
to-do: pretty gifs, video, forum article 
