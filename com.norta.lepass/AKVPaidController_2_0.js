console = {};
console.log = function(log) {
	UMOAKVPaidWebView.JSToNativeHandler("console://localhost?" + log);
};

console.debug = console.log;
console.info = console.log;
console.warn = console.log;
console.error = console.log;

VPAIDWrapper = {};
_creative = {};

window.VPAIDWrapper_Init = function(VPAIDCreative) {
	_creative = VPAIDCreative;

	if(!this.checkVPAIDInterface(VPAIDCreative)) {
		console.log("Invalid VPAID Interface");
		//The VPAIDCreative doesn't conform to the VPAID spec
		var jsCommand = "VPAID://AdError?msg=" + "InvalidCreative";
		VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
		return;
	}
	VPAIDWrapper.handshakeVersion("2.0");
	this.setCallbacksForCreative();

	var jsCommand = "VPAID://InitializeAd";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

// This function checks for the VPAID Ad Interface
this.checkVPAIDInterface = function(VPAIDCreative) {
	if (VPAIDCreative.handshakeVersion && typeof VPAIDCreative.handshakeVersion == "function" &&
		VPAIDCreative.initAd && typeof VPAIDCreative.initAd == "function" &&
		VPAIDCreative.startAd && typeof VPAIDCreative.startAd == "function" &&
		VPAIDCreative.stopAd && typeof VPAIDCreative.stopAd == "function" &&
		//VPAIDCreative.skipAd && typeof VPAIDCreative.skipAd == "function" &&
		//VPAIDCreative.resizeAd && typeof VPAIDCreative.resizeAd == "function" &&
		//VPAIDCreative.pauseAd && typeof VPAIDCreative.pauseAd == "function" &&
		//VPAIDCreative.resumeAd && typeof VPAIDCreative.resumeAd == "function"&&
		//VPAIDCreative.expandAd && typeof VPAIDCreative.expandAd == "function" &&
		//VPAIDCreative.collapseAd && typeof VPAIDCreative.collapseAd == "function" &&
		VPAIDCreative.subscribe && typeof VPAIDCreative.subscribe == "function" &&
		VPAIDCreative.unsubscribe && typeof VPAIDCreative.unsubscribe == "function") {
	    return true;
	}
	return false;
};

//This function registers the callbacks of each of the events
this.setCallbacksForCreative = function() {
	//The key of the object is the event name and the value is a reference to the callback function that is registered with the creative
	var callbacks = {
		AdStarted : VPAIDWrapper.onStartAd,
		AdStopped : VPAIDWrapper.onStopAd,
		AdSkipped : VPAIDWrapper.onSkipAd,
		AdLoaded : VPAIDWrapper.onAdLoaded,
		AdLinearChange : VPAIDWrapper.onAdLinearChange,
		AdSizeChange : VPAIDWrapper.onAdSizeChange,
		AdExpandedChange : VPAIDWrapper.onAdExpandedChange,
		AdSkippableStateChange : VPAIDWrapper.onAdSkippableStateChange,
		AdDurationChange : VPAIDWrapper.onAdDurationChange,
		AdRemainingTimeChange : VPAIDWrapper.onAdRemainingTimeChange,
		AdVolumeChange : VPAIDWrapper.onAdVolumeChange,
		AdImpression : VPAIDWrapper.onAdImpression,
		AdClickThru : VPAIDWrapper.onAdClickThru,
		AdInteraction : VPAIDWrapper.onAdInteraction,
		AdVideoStart : VPAIDWrapper.onAdVideoStart,
		AdVideoFirstQuartile : VPAIDWrapper.onAdVideoFirstQuartile,
		AdVideoMidpoint : VPAIDWrapper.onAdVideoMidpoint,
		AdVideoThirdQuartile : VPAIDWrapper.onAdVideoThirdQuartile,
		AdVideoComplete : VPAIDWrapper.onAdVideoComplete,
		AdUserAcceptInvitation : VPAIDWrapper.onAdUserAcceptInvitation,
		AdUserMinimize : VPAIDWrapper.onAdUserMinimize,
		AdUserClose : VPAIDWrapper.onAdUserClose,
		AdPaused : VPAIDWrapper.onAdPaused,
		AdPlaying : VPAIDWrapper.onAdPlaying,
		AdError : VPAIDWrapper.onAdError,
		AdLog : VPAIDWrapper.onAdLog
	};
	//Looping through the object and registering each of the callbacks with the creative
	for ( var eventName in callbacks) {
		_creative.subscribe(callbacks[eventName], eventName, this);
	}
};

//UMO AdKit Native Invoke
VPAIDWrapper.invokeNativeFromVPaidJS = function(call) {
    UMOAKVPaidWebView.JSToNativeHandler(call);
};


VPAIDWrapper.handshakeVersion = function(playerVPAIDVersion) {
	console.log("handshakeVersion: " + _creative.handshakeVersion(playerVPAIDVersion));
	return ("2.0");
};

//Passthrough for initAd - when the video player wants to call the ad
VPAIDWrapper.initAd = function(width, height, viewMode, desiredBitrate, creativeData, environmentVars) {
	console.log("initAd");
	var envVars = new Object();
	if(environmentVars == "null" || environmentVars == '') {
		envVars.slot = document.getElementById('VPAIDVideoPlayerDiv');
		envVars.videoSlot = document.getElementById('VPAIDVideoPlayer');
		envVars.videoSlot.width = width;
		envVars.videoSlot.height = height;
		envVars.videoSlotCanAutoPlay = true;
    }
	_creative.initAd(width, height, viewMode, desiredBitrate, creativeData, envVars);
};

//Callback for AdPaused
VPAIDWrapper.onAdPaused = function() {
	console.log("onAdPaused");
	var jsCommand = "VPAID://AdPaused";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdPlaying
VPAIDWrapper.onAdPlaying = function() {
	console.log("onAdPlaying");
	var jsCommand = "VPAID://AdPlaying";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdError
VPAIDWrapper.onAdError = function(message) {
	console.log("onAdError: " + message);
	var jsCommand = "VPAID://AdError?msg=" + message;
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdLog
VPAIDWrapper.onAdLog = function(message) {
	console.log("onAdLog: " + message);
};

//Callback for AdUserAcceptInvitation
VPAIDWrapper.onAdUserAcceptInvitation = function() {
	console.log("onAdUserAcceptInvitation");
	var jsCommand = "VPAID://AdUserAcceptInvitation";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdUserMinimize
VPAIDWrapper.onAdUserMinimize = function() {
	console.log("onAdUserMinimize");
	var jsCommand = "VPAID://AdUserMinimize";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdUserClose
VPAIDWrapper.onAdUserClose = function() {
	console.log("onAdUserClose");
	var jsCommand = "VPAID://AdUserClose";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdSkippableStateChange
VPAIDWrapper.onAdSkippableStateChange = function() {
	console.log("Ad Skippable State Changed to: " + _creative.getAdSkippableState());
	var jsCommand = "VPAID://AdSkippableStateChange?skipstate=" + _creative.getAdSkippableState();
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdExpandedChange
VPAIDWrapper.onAdExpandedChange = function() {
	console.log("Ad Expanded Changed to: " + _creative.getAdExpanded());
	var jsCommand = "VPAID://AdExpandedChange?expand=" + _creative.getAdExpanded();
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Passthrough for getAdExpanded
VPAIDWrapper.getAdExpanded = function() {
	console.log("getAdExpanded");
	return _creative.getAdExpanded();
};

//Passthrough for getAdSkippableState
VPAIDWrapper.getAdSkippableState = function() {
	console.log("getAdSkippableState");
	return _creative.getAdSkippableState();
};

//Callback for AdSizeChange
VPAIDWrapper.onAdSizeChange = function() {
	console.log("Ad size changed to: w=" + _creative.getAdWidth() + ", h=" + _creative.getAdHeight());
	var jsCommand = "VPAID://AdSizeChange?width=" + _creative.getAdWidth() + "&height=" + _creative.getAdHeight();
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdDurationChange
VPAIDWrapper.onAdDurationChange = function() {
	console.log("Ad Duration Changed to: " + _creative.getAdDuration());
	var jsCommand = "VPAID://AdDurationChange?time=" + _creative.getAdDuration();
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdRemainingTimeChange
VPAIDWrapper.onAdRemainingTimeChange = function() {
	console.log("Ad Remaining Time Changed to: " + _creative.getAdRemainingTime());
	var jsCommand = "VPAID://AdRemainingTimeChange?time=" + _creative.getAdRemainingTime();
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Passthrough for getAdRemainingTime
VPAIDWrapper.getAdRemainingTime = function() {
	console.log("getAdRemainingTime");
	return _creative.getAdRemainingTime();
};

//Callback for AdImpression
VPAIDWrapper.onAdImpression = function() {
	console.log("Ad Impression");
	var jsCommand = "VPAID://AdImpression";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdClickThru
VPAIDWrapper.onAdClickThru = function(data, id, playerHandles) {
	var jsCommand = "VPAID://AdClickThru";
	if(typeof data === 'object') {
		console.log("Clickthrough portion of the ad was clicked : url=" + data.url + ", id=" + data.id + ", player=" + data.playerHandles);
		if(data.playerHandles) {
			jsCommand = jsCommand + "?url=" + data.url + "&id=" + data.id;
		}
	} else {
		console.log("Click-through portion of the ad was clicked : url=" + data + ", id=" + id + ", player=" + playerHandles);
		if(playerHandles) {
			jsCommand = jsCommand + "?url=" + data + "&id=" + id;
		}
	}
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdInteraction
VPAIDWrapper.onAdInteraction = function(id) {
	console.log("A non-clickthrough event has occured");
	var jsCommand = "VPAID://AdInteraction?id=" + id;
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdVideoStart
VPAIDWrapper.onAdVideoStart = function() {
	console.log("Video 0% completed");
	var jsCommand = "VPAID://AdVideoStart";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdVideoFirstQuartile
VPAIDWrapper.onAdVideoFirstQuartile = function() {
	console.log("Video 25% completed");
	var jsCommand = "VPAID://AdVideoFirstQuartile";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdVideoMidpoint
VPAIDWrapper.onAdVideoMidpoint = function() {
	console.log("Video 50% completed");
	var jsCommand = "VPAID://AdVideoMidpoint";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdVideoThirdQuartile
VPAIDWrapper.onAdVideoThirdQuartile = function() {
	console.log("Video 75% completed");
	var jsCommand = "VPAID://AdVideoThirdQuartile";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdVideoComplete
VPAIDWrapper.onAdVideoComplete = function() {
	console.log("Video 100% completed");
	var jsCommand = "VPAID://AdVideoComplete";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for AdLinearChange
VPAIDWrapper.onAdLinearChange = function() {
	console.log("Ad linear has changed: " + _creative.getAdLinear())
	var jsCommand = "VPAID://AdLinearChange?linear=" + _creative.getAdLinear();
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Passthrough for getAdLinear
VPAIDWrapper.getAdLinear = function() {
	console.log("getAdLinear");
	return _creative.getAdLinear();
};

//Passthrough for startAd()
VPAIDWrapper.startAd = function() {
	console.log("startAd");
	_creative.startAd();
};

//Callback for AdLoaded
VPAIDWrapper.onAdLoaded = function() {
	console.log("ad has been loaded");
	var jsCommand = "VPAID://AdLoaded";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for StartAd()
VPAIDWrapper.onStartAd = function() {
	console.log("Ad has started");
	var jsCommand = "VPAID://AdStarted";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Passthrough for stopAd()
VPAIDWrapper.stopAd = function() {
	_creative.stopAd();
};

//Callback for StopAd
VPAIDWrapper.onStopAd = function() {
	console.log("Ad has stopped");
	var jsCommand = "VPAID://AdStopped";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Callback for SkipAd
VPAIDWrapper.onSkipAd = function() {
	console.log("Ad was skipped");
	var jsCommand = "VPAID://AdSkipped";
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Passthrough for setAdVolume
VPAIDWrapper.setAdVolume = function(val) {
	_creative.setAdVolume(val);
};

//Passthrough for getAdVolume
VPAIDWrapper.getAdVolume = function() {
	return _creative.getAdVolume();
};

//Callback for AdVolumeChange
VPAIDWrapper.onAdVolumeChange = function() {
	console.log("Ad Volume has changed to - " + _creative.getAdVolume());
	var jsCommand = "VPAID://AdVolumeChange?volume=" + _creative.getAdVolume();
	VPAIDWrapper.invokeNativeFromVPaidJS(jsCommand);
};

//Passthrough for resizeAd
VPAIDWrapper.resizeAd = function(width, height, viewMode) {
	console.log("resizeAd");
	_creative.resizeAd(width, height, viewMode);
};

//Passthrough for pauseAd()
VPAIDWrapper.pauseAd = function() {
	console.log("pauseAd");
	_creative.pauseAd();
};

//Passthrough for resumeAd()
VPAIDWrapper.resumeAd = function() {
	_creative.resumeAd();
};

//Passthrough for expandAd()
VPAIDWrapper.expandAd = function() {
	_creative.expandAd();
};

//Passthrough for collapseAd()
VPAIDWrapper.collapseAd = function() {
	_creative.collapseAd();
};

if(window['getVPAIDAd']) {
	var fn = window['getVPAIDAd']();
	UMOAKVPaidWebView.JSToNativeHandler("Logged into VPAIDJS");
	if (fn) {// && typeof fn == 'function') {
		var VPAIDCreative = fn;
		UMOAKVPaidWebView.JSToNativeHandler("Calling VPAID Wrapper");
		window.VPAIDWrapper_Init(VPAIDCreative);
	}
}

