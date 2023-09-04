package com.morpheusdata.burp

import com.morpheusdata.core.AbstractTaskService
import com.morpheusdata.core.ExecutableTaskInterface
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.TaskProvider
import com.morpheusdata.model.Icon
import com.morpheusdata.model.OptionType
import com.morpheusdata.model.TaskType

class BurpScanTaskProvider implements TaskProvider {
    MorpheusContext morpheusContext
    Plugin plugin
    AbstractTaskService service

    BurpScanTaskProvider(Plugin plugin, MorpheusContext morpheusContext) {
        this.plugin = plugin
        this.morpheusContext = morpheusContext
        this.service = new BurpScanTaskService(morpheus)
    }

    @Override
    TaskType.TaskScope getScope() {
        return TaskType.TaskScope.all  
    }

    @Override
    String getDescription() {
        return "Trigger a Burp Scan from within Morpheus and see the results in the UI"
    }

    @Override
    Boolean isAllowExecuteLocal() {
        return true
    }

    @Override
    Boolean isAllowExecuteRemote() {
        return false
    }

    @Override
    Boolean isAllowExecuteResource() {
        return false
    }

    @Override
    Boolean isAllowLocalRepo() {
        return false
    }

    @Override
    Boolean isAllowRemoteKeyAuth() {
        return false
    }

    @Override
    Boolean hasResults() {
        return true
    }

    @Override
    List<OptionType> getOptionTypes() {
        return [
                new OptionType(code: 'burp.apiUrl', name: 'Burp API URL', inputType: OptionType.InputType.TEXT, fieldName: 'apiUrl', fieldLabel: 'REST API Url', required: true, displayOrder: 0),
                new OptionType(code: 'burp.apiKey', name: 'Burp API Key', inputType: OptionType.InputType.TEXT, fieldName: 'apiKey', fieldLabel: 'API Key', required: true, displayOrder: 1),
		        new OptionType(code: 'burp.urlToScan', name: 'URL to scan', inputType: OptionType.InputType.TEXT, fieldName: 'urlToScan', fieldLabel: 'URL to scan', required: true, displayOrder: 2),
   //             new OptionType(code: 'burp.scanConfigurationType', name: 'Scan Configuration Type', inputType: OptionType.InputType.SELECT, fieldName: 'scanConfigurationType', fieldLabel: 'Scan Configuration Type', optionSource: 'burpScanConfigType', displayOrder: 3),
                new OptionType(code: 'burp.scanConfigurationNamed', name: 'Scan Configuration', inputType: OptionType.InputType.SELECT, fieldName: 'scanConfiguration', fieldLabel: 'Scan Configuration', optionSource: 'burpScanConfigNamedList', displayOrder: 4) // visibleOnCode: 'burp.scanConfigurationType:Default' , dependsOn: 'burp.scanConfigurationType', 
        ]
    }

    @Override
    MorpheusContext getMorpheus() {
        return morpheusContext
    }

    @Override
    Plugin getPlugin() {
        return plugin
    }

    @Override
    String getCode() {
        return 'burpScan'
    }

    @Override
    String getName() {
        return 'Burp Scan Task'
    }

    @Override
    Icon getIcon() {
        return new Icon(path:"burp-white.svg", darkPath: "burp-black.svg") 
    }
}
