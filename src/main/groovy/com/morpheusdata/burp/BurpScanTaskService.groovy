package com.morpheusdata.burp

import com.morpheusdata.core.AbstractTaskService
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.model.ComputeServer
import com.morpheusdata.model.Container
import com.morpheusdata.model.Instance
import com.morpheusdata.model.Task
import com.morpheusdata.model.TaskConfig
import com.morpheusdata.model.TaskResult
import com.morpheusdata.response.ServiceResponse
import groovy.json.JsonSlurper
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
        String burpRestUrl = task.taskOptions.find { it.optionType.code == 'burp.apiUrl' }?.value
        String burpRestApiKey =  task.taskOptions.find { it.optionType.code == 'burp.apiKey' }?.value
        String burpScanConfigName =  task.taskOptions.find { it.optionType.code == 'burp.scanConfiguration' }?.value  /// do I need to change this to a JSON object? 
        String urlToScan = task.taskOptions.find { it.optionType.code == 'burp.urlToScan' }?.value

        HttpApiClient client = new HttpApiClient()
        try {
            String path = "/${burpRestApiKey}/v0.1/scan/"
            HttpApiClient.RequestOptions requestOptions = new HttpApiClient.RequestOptions()
                .headers = ['Content-Type':'application/json']
                .body = "{\"scan_configurations\":[{\"config\":\"" + burpScanConfigName + """\",\"type\":\"NamedConfiguration\"}],\"urls\":[\"${urlToScan}\"]}""" // how could urlToScan be injected into the JSON? 

            ServiceResponse response = client.callApi(burpRestUrl, path, null, null, requestOptions, 'POST') 
            if (response.success) {
                log.info("Scan task for ${urlToScan} created successfully")
                log.info("Getting scan ID...")
                String scanID = response.headers['Location'] // retrieve the scan ID from the Location header of the response
                log.info("Scan ID = ${scanID}")
                String scanStatusUrl = burpRestUrl + path + scanID 
                scanResults = client.callJsonApi(scanStatusUrl, null, null, null, requestOptions.headers, 'GET')
   //             scanResultsAsMap = new JsonSlurper().parseText(scanResults)      // jsonSlurper logic? 
                if (scanResults.success) {
                    return new TaskResult(
                            success: true,
                            data   : scanResultsAsMap.data,
                            output : scanResultsAsMap.content
                        )
                }
                // how are task results retrieved ? 
                // Is it possible for the results window to dynamically update - like an Ansible task - with the scan status ?
                // Can I embed an option list in my plugin? I.e. A select list for all the available default scan configs in Burp 
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }   
    }
}
