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
        return "Adds a new task type called 'Burp Scan' that interacts with Burp Suite Professional's REST API. A new report type is also added to organize the results into a report visiible in Morpheus"
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
                new OptionType(code: 'burp.apiUrl', name: 'Burp API URL', inputType: OptionType.InputType.TEXT, fieldName: 'apiUrl', fieldLabel: 'REST API URL', required: true, displayOrder: 0),
                new OptionType(code: 'burp.apiKey', name: 'Burp API Key', inputType: OptionType.InputType.TEXT, fieldName: 'apiKey', fieldLabel: 'REST API Key', required: true, displayOrder: 1),
		        new OptionType(code: 'burp.urlsToScan', name: 'URL(s) to scan', inputType: OptionType.InputType.TEXT, fieldName: 'urlsToScan', fieldLabel: 'URL(s) to scan', required: true, displayOrder: 2),
                new OptionType(code: 'burp.scanConfigurationType', name: 'Scan Configuration Type', inputType: OptionType.InputType.SELECT, fieldName: 'scanConfigurationType', fieldLabel: 'Scan Configuration Type', optionSource: 'scanConfigType', displayOrder: 3),
                new OptionType(code: 'burp.defaultScanConfigurations', name: 'Default Scan Configuration', inputType: OptionType.InputType.SELECT, fieldName: 'defaultScanConfigurations', fieldLabel: 'Select A Scan Configuration', optionSource: 'defaultScanConfigList', dependsOn: 'scanConfigurationType', visibleOnCode: 'scanConfigurationType:(Default)', displayOrder: 4),
                new OptionType(code: 'burp.customScanConfiguration', name: 'Custom Scan Configuration', inputType: OptionType.InputType.CODE_EDITOR, fieldName: 'customScanConfigurationJSON', fieldLabel: 'Scan Configuration (JSON)', dependsOn: 'scanConfigurationType', visibleOnCode: 'scanConfigurationType:(Custom)', displayOrder: 5) 
 
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
        return 'burpScanTaskService'
    }

    @Override
    String getName() {
        return 'Burp Scan'
    }

    @Override
    Icon getIcon() {
        return new Icon(path:"burp-white.svg", darkPath: "burp-black.svg") 
    }
}
