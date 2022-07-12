/*
 * The MIT License (MIT)
 * Copyright (c) 2015 Joel De La Torriente - jjdltc - https://github.com/jjdltc
 * See a full copy of license in the root folder of the project
 */
package com.jjdltc.cordova.plugin.sftp;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

import java.util.UUID;

import android.os.AsyncTask;

public class JJsftp extends CordovaPlugin {
    
    private AsyncTask<Void, Integer, Long> staticAsync = null;
    private enum ACTIONS {
        download,
        upload,
        cancel
    };

    private String udid = null;
    private Long latency = 0L;

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        boolean result = true;
        
        JSONObject hostData = this.setHostData(args);
        JSONArray actionArr = this.setActionArr(args);

        this.udid = UUID.randomUUID().toString();

        if((hostData==null || action == null) && action!="cancel"){
            this.processResponse(callbackContext, false, "Some parameters were missed - hostData or actionArr not found-");
            return false;
        }
        
        switch (ACTIONS.valueOf(action)) {
            case download:
                this.download(hostData, actionArr);
                this.processResponse(callbackContext, true, "Download is added to to list");
            break;
            case upload:
                this.upload(hostData, actionArr);
                this.processResponse(callbackContext, true, "upload is added to to list");
            break;
            case cancel:
                boolean cancellSuccess = this.cancelStaticAsync();
                String msg = (cancellSuccess)?"Cancellation request sent":"Cancellation request sent but we are unable to execute (may not be such a process or is already cancelled)";
                this.processResponse(callbackContext, cancellSuccess, msg);
            break;
            default:
                this.processResponse(callbackContext, false, "Some parameters were missed - action not found -");
                result = false;
            break;
        }
        
		
        return result;
    }
    
    /**
     * 
     * @param ctx               The plugin CallbackContext
     * @param success           Boolean that define if the JS plugin should fire the success or error function
     * @param msg               The String msg to by sended
     * @throws JSONException
     */
    private void processResponse(CallbackContext ctx, boolean success, String msg) throws JSONException{
        JSONObject response = new JSONObject();
        JSONObject data     = new JSONObject();

        data.put("id", this.udid);

        response.put("success", success);
        response.put("message", msg);
        response.put("data", data);
        response.put("latency", this.latency);
        Log.d("asdfasdfasdf", " time taken: " + Long.toString(this.latency) + " ms");

        if(success){
            ctx.success(response);
        }
        else{
            ctx.error(response);
        }
    }
    
    /**
     * Use an custom AsyncTask 'asyncSFTPAction' to execute the download
     * 
     * @param hostData          JSONObject with the host data to connect (processed by 'setHostData' function)
     * @param actionArr         JSONArray with the action list to execute (processed by 'setActionArr' function)
     */
    private void download(JSONObject hostData, JSONArray actionArr){
        this.staticAsync = new asyncSFTPAction(hostData, actionArr, "download", this.webView, this.udid);
        this.staticAsync.execute();
    }
    
    /**
     * Use an custom AsyncTask 'asyncSFTPAction' to execute the upload
     * 
     * @param hostData          JSONObject with the host data to connect (processed by 'setHostData' function)
     * @param actionArr         JSONArray with the action list to execute (processed by 'setActionArr' function)
     */
    private void upload(JSONObject hostData, JSONArray actionArr){
        try{
            this.staticAsync = new asyncSFTPAction(hostData, actionArr, "upload", this.webView, this.udid);
            this.staticAsync.execute();
            this.latency = this.staticAsync.get();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Cancel the actual action, if no action exists do nothing
     * 
     * @return                  true id the cancelation was asked, false otherwise
     */    
    private boolean cancelStaticAsync(){
        return (this.staticAsync!=null)?this.staticAsync.cancel(true):false;
    }
    
    /**
     * Validate if the options sent by user are not null or empty and also if accomplish the base structure
     * 
     * @param arguments         The arguments passed by user with the JSONObject that has the host options
     * @return                  A valid 'hostData' JSONObject with its inner host options to connect
     * @throws JSONException
     */
    private JSONObject setHostData(JSONArray arguments) throws JSONException{
        JSONObject hostData = arguments.optJSONObject(0);
        boolean validArgs   = true;
        String[] keys       = new String[]{
            "host",
			"port",
            "user",
            "pswd"
        };

        if(hostData==null){
            return null;            
        }

        if(hostData.opt("port")==null){
            hostData.put("port", 22);
        }
        
        for (int i = 0; i < keys.length; i++) {
            if(hostData.opt(keys[i])==null){
                validArgs = false;
                break;
            }
        }

        return (validArgs)?hostData:null;
    }
    
    /**
     * Validate if the options sent by user are not null or empty and also if accomplish the base structure
     * 
     * @param arguments         The arguments passed by user with the JSONArray of JSONObject with the local and remote path of the files
     * @return                  A valid 'actionArr' JSONArray with its inner JSONObject paths
     * @throws JSONException 
     */
    private JSONArray setActionArr(JSONArray arguments) throws JSONException{
        JSONArray actionArr = arguments.optJSONArray(1);
        boolean validArr    = true;
        String[] keys       = new String[]{
              "remote"
            , "local"
        };
        
        if(actionArr==null){
            return null;            
        }
        
        for (int i = 0; i < actionArr.length(); i++) {
            JSONObject tempActionObj = actionArr.optJSONObject(i);
            if(tempActionObj==null){
                validArr = false;
            }
            else{
                for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
                    if(tempActionObj.opt(keys[keyIdx])==null){
                        validArr = false;
                        break;
                    }
                    else{
                        if(keys[keyIdx]=="local"){
                            String local    = tempActionObj.optString("local").replace("file://", "");
                            tempActionObj.put("local", local);
                        }
                    }
                }
            }

            if(!validArr){
                break;
            }
        }
        
        return (validArr)?actionArr:null;
    }
}
