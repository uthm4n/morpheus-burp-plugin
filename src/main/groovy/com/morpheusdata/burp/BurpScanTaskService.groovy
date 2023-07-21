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
        println config.accountId
        String burpRestUrl = task.taskOptions.find { it.optionType.code == 'burp.apiUrl' }?.value
        String burpRestApiKey =  task.taskOptions.find { it.optionType.code == 'burp.apiKey' }?.value
        String burpScanConfig =  task.taskOptions.find { it.optionType.code == 'burp.scanConfiguration' }?.value  /// do I need to change this to a JSON object? 

        HttpApiClient client = new HttpApiClient()
        try {
            String path = "/${burpRestApiKey}/v0.1/scan/"
            HttpApiClient.RequestOptions requestOptions = new HttpApiClient.RequestOptions()
            requestOptions.headers = ['Content-Type':'application/json']
            requestOptions.body = """{"scan_configurations":[{"config":""" + burpScanConfig + ""","type":"CustomConfiguration"}],"urls":["https://portswigger-labs.net"]}""" // is this even correct? How would it be interpreted in the HTTP body

            def results = client.callApi(burpRestUrl,path,requestOptions,'POST')
        }
        catch (Exception e) {
            System.out.println(e)
        }
    }   
}
