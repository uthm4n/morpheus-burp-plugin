package com.morpheusdata.burp

import com.morpheusdata.core.AbstractTaskService
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.model.ComputeServer
import com.morpheusdata.model.Container
import com.morpheusdata.model.Instance
import com.morpheusdata.model.Task
import com.morpheusdata.model.TaskConfig
import com.morpheusdata.model.TaskResult
import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.core.util.ConnectionUtils
import com.morpheusdata.response.ServiceResponse
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.StringEscapeUtils
import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Slf4j

@Slf4j
class BurpScanTaskService extends AbstractTaskService {
    MorpheusContext morpheus

    BurpScanTaskService(MorpheusContext morpheus) {
        this.morpheus = morpheus
    }

    @Override
    TaskResult executeLocalTask(Task task, Map opts, Container container, ComputeServer server, Instance instance) {
        TaskConfig config = buildLocalTaskConfig([:], task, [], opts).blockingGet()
        if(instance) {
            config = buildInstanceTaskConfig(instance, [:], task, [], opts).blockingGet()
        }
        if(container) {
            config = buildContainerTaskConfig(container, [:], task, [], opts).blockingGet()
        }

        executeTask(task, config)
    }

    @Override
    TaskResult executeServerTask(ComputeServer server, Task task, Map opts=[:]) {
        TaskConfig config = buildComputeServerTaskConfig(server, [:], task, [], opts).blockingGet()
        executeTask(task, config)
    }

    @Override
    TaskResult executeContainerTask(Container container, Task task, Map opts = [:]) {
        TaskConfig config = buildContainerTaskConfig(container, [:], task, [], opts).blockingGet()
        executeTask(task, config)
    }

    @Override
    TaskResult executeRemoteTask(Task task, Map opts, Container container, ComputeServer server, Instance instance) {
        TaskConfig config = buildRemoteTaskConfig([:], task, [], opts).blockingGet()
        executeTask(task, config)
    }

    @Override
    TaskResult executeRemoteTask(Task task, Container container, ComputeServer server, Instance instance) {
        executeRemoteTask(task,[:],container,server,instance)
    }


    TaskResult executeTask(Task task, TaskConfig config) {
        // get task options 
        String restApiUrl = task.taskOptions.find { it.optionType.code == 'burp.apiUrl' }?.value
        String restApiKey = task.taskOptions.find { it.optionType.code == 'burp.apiKey' }?.value
        String applicationLoginType = task.taskOptions.find { it.optionType.code == 'burp.applicationLoginType' }?.value
        String applicationLoginUsername = task.taskOptions.find { it.optionType.code == 'burp.applicationLoginUsername' }?.value 
        String applicationLoginPassword = task.taskOptions.find { it.optionType.code == 'burp.applicationLoginPassword' }?.value
        String recordedLoginLabel = task.taskOptions.find { it.optionType.code == 'burp.recordedLoginLabel' }?.value 
        String recordedLoginScript = task.taskOptions.find { it.optionType.code == 'burp.recordedLoginScript' }?.value 
        String scanConfigType = task.taskOptions.find { it.optionType.code == 'burp.scanConfigurationType' }?.value
        String applicationLogin = task.taskOptions.find { it.optionType.code == 'burp.applicationLogin' }?.value
        String defaultScanConfig = task.taskOptions.find { it.optionType.code == 'burp.defaultScanConfigurations' }?.value
        String customScanConfig = task.taskOptions.find { it.optionType.code == 'burp.customScanConfiguration' }?.value
        String urlsToScan = task.taskOptions.find { it.optionType.code == 'burp.urlsToScan' }?.value

        // set up api client for interaction with burp 
        HttpApiClient burpClient = new HttpApiClient()

        try {
            // test connection to burp - to-do: figure out why this doesn't work
            String burpApiUrl = restApiUrl + "/${restApiKey}/v0.1"
            try { 
                def burpOnline = ConnectionUtils.testHostConnectivity(burpApiUrl, null, true, false, null)
                if(burpOnline) {
                    log.info("Burp is accessible, woohoo!")
                }
            } catch(e) {
                log.error("Error connecting to Burp on {}", apiUrl, e)
            }

            // prepare http request
            def body 
            if (scanConfigType == 'Default') {
                if (applicationLogin == 'on' && applicationLoginType == 'UsernameAndPasswordLogin') {
                    body = [
                        'application_logins': [
                        ['password': applicationLoginPassword, 'type': 'UsernameAndPasswordLogin', 'username': applicationLoginUsername]
                    ],
                        'scan_configurations' : [
                            ['name': defaultScanConfig, 'type': 'NamedConfiguration']
                        ],
                        'urls': []
                    ]
                }
                else if (applicationLogin == 'on' && applicationLoginType == 'RecordedLogin') {
                    StringEscapeUtils stringEscape = new StringEscapeUtils()
                    String escapedLoginScript = stringEscape.escapeJava(recordedLoginScript)
                    escapedLoginScript = escapedLoginScript.replace("\\n", "")
                    body = [
                        'application_logins': [
                        ['label': recordedLoginLabel, 'script': escapedLoginScript, 'type': 'RecordedLogin']
                        ],
                        'scan_configurations' : [
                            ['name': defaultScanConfig, 'type': 'NamedConfiguration']
                        ],
                        'urls': []
                    ]
                }
                else {
                    body = [
                        'application_logins': [],
                        'scan_configurations' : [
                            ['name': defaultScanConfig, 'type': 'NamedConfiguration']
                        ],
                        'urls': []
                    ]
                }
            } else if (scanConfigType == 'Custom') {
                StringEscapeUtils stringEscape = new StringEscapeUtils()
                String escapedJSONConfig = stringEscape.escapeJava(customScanConfig)
                escapedJSONConfig = escapedJSONConfig.replace("\\n", "")  // remove the newline characters in preparation for injection into the http body - fussy burp stuff 
                body = [
                    'application_logins': [],
                        'scan_configurations' : [
                            ['config': escapedJSONConfig, 'type': 'CustomConfiguration']
                        ],
                        'urls': []
                ]
            }

            // space for app login + custom scan config support  
            
            else {  // if no application login or scan configuration defined, only inject the urls to scan in the body
                body = [
                    'urls': []
                ]
            }
            body['urls'] << urlsToScan
            HttpApiClient.RequestOptions requestOptions = new HttpApiClient.RequestOptions()
            requestOptions.headers = ['Content-Type':'application/json']
            requestOptions.body = body

            // send api call to trigger scan
            ServiceResponse response = burpClient.callApi(burpApiUrl, '/scan', null, null, requestOptions, 'POST') 
            if (response.success && response.headers['Location']) {
                log.info("Scan task for ${urlsToScan} created successfully")
                String scanID = response.headers['Location']
                log.info("Retrieving scan ID...") 
                log.info("Scan ID = ${scanID}")
                String scanStatusUrl = burpApiUrl + '/scan/' + scanID 
                def scanResults = burpClient.callJsonApi(scanStatusUrl, null, null, null, new HttpApiClient.RequestOptions(headers:['Content-Type':'application/json']), 'GET') 
                while (scanResults.success && scanResults.data?.scan_status != 'succeeded') {        // to-do: improve this by implementing some type of polling / thread solution that can check the results every X minutes
                    log.info("Scan ${scanID} Results: ${scanResults}") 
                    sleep(10000)
                    if (scanResults.data?.scan_status == 'succeeded') { 
                        return new TaskResult(
                            success: true,
                            data:    scanResults.data,
                            output:  scanResults.data
                            )
                    }
                    else if (scanResults.data?.scan_status == 'failed') {
                        return new TaskResult(
                            success: false, 
                            data:    scanResults.data, 
                            output:  "Scan failed. Check Burp for more details..."
                        )
                    }
                }
            }
        }
        catch(Exception e) {
            System.out.println(e.printStackTrace())
        } finally {
            burpClient.shutdownClient()
        }
    }
}
