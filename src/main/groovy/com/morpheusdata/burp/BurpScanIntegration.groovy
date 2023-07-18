package com.morpheusdata.BurpScanIntegration;

import com.morpheusdata.core.Plugin;
import com.morpheusdata.model.OptionType;
import com.morpheusdata.model.OptionType.InputType;
import java.util.Arrays;

class BurpScanIntegration extends Plugin {

    public static final OptionType burpApiUrl = getBurpApiURL();
    public static final OptionType burpApiKey = getBurpApiKey();


    @Override
    String getCode() {
        return "burp-scan-integration";
    }

    @Override
    void initialize() {
        this.setName("BurpScan");
        this.settings.add(burpApiUrl);
        this.settings.add(burpApiKey);
        this.setAuthor("Uthman Al-Ayek Eqbal");
        this.setDescription("Trigger a Burp Scan from the Morpheus UI");

    }

    private static OptionType getBurpApiURL() {
        OptionType burpApiUrl = new OptionType();
        burpApiUrl.setName("REST API URL");
        burpApiUrl.setCode("burp-api-url");
        burpApiUrl.setFieldName("burpApiURL");
        burpApiUrl.setFieldLabel(burpApiUrl.getName());
        burpApiUrl.setDisplayOrder(0);
        burpApiUrl.setHelpText("Burp Suite REST API URL");
        burpApiUrl.setRequired(true);
        burpApiUrl.setInputType(InputType.TEXT);
        return burpApiUrl;

    }
    private static OptionType getBurpApiKey() {
        OptionType burpApiKey = new OptionType();
        burpApiKey.setName("API Key");
        burpApiKey.setCode("burp-api-key");
        burpApiKey.setFieldName("burpApiKey");
        burpApiKey.setFieldLabel(burpApiKey.getName());
        burpApiKey.setDisplayOrder(1);
        burpApiKey.setHelpText("Burp Suite REST API Key");
        burpApiKey.setRequired(true);
        burpApiKey.setInputType(InputType.TEXT);
        return burpApiKey;

    }

    @Override
    void onDestroy() {

    }
}
