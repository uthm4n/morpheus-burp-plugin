package com.morpheusdata.burp;

import com.morpheusdata.core.Plugin;
import com.morpheusdata.model.Permission;
import com.morpheusdata.model.OptionType;
import com.morpheusdata.model.OptionType.InputType;
import java.util.Arrays;

class BurpScanTaskPlugin extends Plugin {

    @Override
    String getCode() {
        return "burp-scan-task";
    }

    @Override
    void initialize() {
        BurpScanTaskProvider BurpScanTaskProvider = new BurpScanTaskProvider(this, morpheus)
	    this.pluginProviders.put(BurpScanTaskProvider.getCode(), BurpScanTaskProvider)
        BurpOptionSourceProvider optionSourceProvider = new BurpOptionSourceProvider(this,morpheus)
	    this.pluginProviders.put(optionSourceProvider.getCode(),optionSourceProvider)
        this.setName("Burp Scan");
        this.setAuthor("Uthman Al-Ayek Eqbal");
        this.setDescription("Trigger a Burp Scan from a new Morpheus task type")
        this.setPermissions([Permission.build('Burp Scan','burp-scan-task-permission', 'security', [Permission.AccessType.none, Permission.AccessType.full])])

    }

    @Override
    void onDestroy() {
    }
}
