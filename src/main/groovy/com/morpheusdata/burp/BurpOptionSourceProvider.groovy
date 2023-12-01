package com.morpheusdata.burp

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.OptionSourceProvider
import com.morpheusdata.core.Plugin

class BurpOptionSourceProvider implements OptionSourceProvider{

	Plugin plugin
	MorpheusContext morpheusContext

	BurpOptionSourceProvider(Plugin plugin, MorpheusContext context) {
		this.plugin = plugin
		this.morpheusContext = context
	}

	@Override
	MorpheusContext getMorpheus() {
		return this.morpheusContext
	}

	@Override
	Plugin getPlugin() {
		return this.plugin
	}

	@Override
	String getCode() {
		return 'burp-option-source-provider'
	}

	@Override
	String getName() {
		return 'Burp Option Source Provider'
	}

	@Override
	List<String> getMethodNames() {
		return new ArrayList<String>(['defaultScanConfigList', 'scanConfigType', 'applicationLoginType'])
	}

	def applicationLoginType(args) {
        	return [
            [name: 'Username and Password', value: 'UsernameAndPasswordLogin'],
            [name: 'Recorded Login', value: 'RecordedLogin']
        ]
   	 }
	
    	def scanConfigType(args) {
        	return [
            [name: 'Default', value: 'Default'], 
            [name: 'Custom', value: 'Custom']
        ]
    	 }

	def defaultScanConfigList(args) {
		return [
            [name:'Add requested item to site map', value:'Add requested item to site map'],            
            [name:'Audit checks - all except JavaScript analysis', value:'Audit checks - all except JavaScript analysis'],
            [name:'Audit checks - all except time-based detection methods', value:'Audit checks - all except time-based detection methods'], 
            [name:'Audit checks - BChecks only', value:'Audit checks - BChecks only'],
            [name:'Audit checks - critical issues only', value:'Audit checks - critical issues only'],
            [name:'Audit checks - extensions only', value:'Audit checks - extensions only'],
            [name:'Audit checks - light active', value:'Audit checks - light active'],
            [name:'Audit checks - medium active', value:'Audit checks - medium active'],
            [name:'Audit checks - passive', value:'Audit checks - passive'],
            [name:'Audit coverage - maximum', value:'Audit coverage - maximum'],
            [name:'Audit coverage - thorough', value:'Audit coverage - thorough'],
            [name:'Crawl and Audit - Balanced', value:'Crawl and Audit - Balanced'],
            [name:'Crawl and Audit - CICD Optimized', value:'Crawl and Audit - CICD Optimized'],
            [name:'Crawl and Audit - Deep', value:'Crawl and Audit - Deep'],
            [name:'Crawl and Audit - Fast', value:'Crawl and Audit - Fast'],
            [name:'Crawl and Audit - Lightweight', value:'Crawl and Audit - Lightweight'],
            [name:'Crawl limit - 10 minutes', value:'Crawl limit - 10 minutes'],
            [name:'Crawl limit - 30 minutes', value:'Crawl limit - 30 minutes'],
            [name:'Crawl limit - 60 minutes', value:'Crawl limit - 60 minutes'],
            [name:'Crawl strategy - faster', value:'Crawl strategy - faster'],
            [name:'Crawl strategy - fastest', value:'Crawl strategy - fastest'],
            [name:'Crawl strategy - more complete', value:'Crawl strategy - more complete'],
            [name:'Crawl strategy - most complete', value:'Crawl strategy - most complete'],
            [name:'Minimize false negatives', value:'Minimize false negatives'],
            [name:'Minimize false positives', value:'Minimize false positives'],
            [name:'Never stop audit due to application errors', value:'Never stop audit due to application errors'],
            [name:'Never stop crawl due to application errors', value:'Never stop crawl due to application errors']
        ]
   	 }
}
