package com.morpheusdata.burp;

import com.morpheusdata.core.Plugin;
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
        this.setName("Burp Scan");
        this.setAuthor("Uthman Al-Ayek Eqbal");
        this.setDescription("Trigger a Burp Scan from the Morpheus UI");

    }

    @Override
    void onDestroy() {
        morpheus.task.disableTask('burp').blockingGet()
    }
}
