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
import groovy.json.JsonBuilder
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
        String burpScanConfigNamed =  task.taskOptions.find { it.optionType.code == 'burp.scanConfigurationNamed' }?.value
        String urlToScan = task.taskOptions.find { it.optionType.code == 'burp.urlToScan' }?.value

        HttpApiClient burpClient = new HttpApiClient()
        try {
            String path = "/${burpRestApiKey}/v0.1/scan/"
            def body = [
                    'scan_configurations' : [
                        ['name': burpScanConfigNamed, 'type': 'NamedConfiguration']
                    ],
                    'urls': []
            ]
            body['urls'] << urlToScan
            HttpApiClient.RequestOptions requestOptions = new HttpApiClient.RequestOptions()
            requestOptions.headers = ['Content-Type':'application/json']
            requestOptions.body = body
            ServiceResponse response = burpClient.callApi(burpRestUrl, path, null, null, requestOptions, 'POST') 
            if (response.success) {
                log.info("Scan task for ${urlToScan} created successfully")
                log.info("Getting scan ID...")
                String scanID = response.headers['Location'] 
                log.info("Scan ID = ${scanID}")
                String scanStatusUrl = burpRestUrl + path + scanID 
                def scanResults = burpClient.callJsonApi(scanStatusUrl, null, null, null, new HttpApiClient.RequestOptions(headers:['Content-Type':'application/json']), 'GET') 
                while (scanResults.success && scanResults.data?.scan_status != 'succeeded') {
                    log.info("Scan Results: ${scanResults.data}") 
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