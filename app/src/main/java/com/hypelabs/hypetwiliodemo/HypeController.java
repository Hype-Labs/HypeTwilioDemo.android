//
// MIT License
//
// Copyright (C) 2018 HypeLabs Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package com.hypelabs.hypetwiliodemo;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import com.hypelabs.hype.Error;
import com.hypelabs.hype.Hype;
import com.hypelabs.hype.Instance;
import com.hypelabs.hype.Message;
import com.hypelabs.hype.MessageInfo;
import com.hypelabs.hype.MessageObserver;
import com.hypelabs.hype.NetworkObserver;
import com.hypelabs.hype.StateObserver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.twilio.chat.internal.Utils.toList;

public class HypeController implements StateObserver, NetworkObserver, MessageObserver {

    private Context context;
    private Context getContext() {
        return context;
    }
    private static final String TAG = HypeController.class.getName();
    private WeakReference<HypeControllerDelegate> delegateWeakReference;
    private InstanceChannelModel instanceChannel;
    private String announcement;
    private boolean netAccess;

    /**
     * This controller delegate has the purpose of dealing
     * with hype controller notifications. This delegate notifys upper classes
     * when it neeeds to request a twilio client, needs to join a offline peer
     * to a twilio channel, needs to foward a message from a offline peer to twilio,
     * when founds or loses a new peer.
     */
    public interface HypeControllerDelegate{

        /**
         * This notification indicates that a offline peer
         * wants do connect to twilio.
         * @param identifierForVendor Indicates the identifier vendor of the offline client.
         */
        void requestTwilioClientWithidentifierForVendor(String identifierForVendor);

        /**
         * This notification indicates that the given identifier for vendor
         * joined a channel with the given identity.
         * @param response Channel that has been joined.
         */
        void didJoinTwilio(Map<String, String> response);

        /**
         * This notification occurs when a client fowards a message
         * to twilio from a offline client.
         * @param instance Indicates the instance of the offline client.
         * @param identifierForVendor Indicates the identifier vendor of the offline client.
         */
        void didSendMessageFromIdentifierVendor(String instance, String identifierForVendor);

        /**
         * This notification occurs when the hype framework found an instance.
         * @param instance Indicates the instance of the offline client.
         * @param identifierForVendor Indicates the identifier vendor of the offline client.
         */
        void didFoundInstanceWithIdentifierForVendor(Instance instance, String identifierForVendor);

        /**
         * This notification indicates that client could not join twilio channel.
         * @param instance Indicates a message describing the why it failed.
         */
        void didLoseInstance(Instance instance);

        /**
         * This notification indicates that hype framework received a message.
         * @param response Response received
         */
        void didReceiveMessage(Map<String, String> response);

    }

    public HypeController(Context context) {

        this.context = context;
    }

    private InstanceChannelModel getInstanceChannel(){


        if(this.instanceChannel == null){

            this.instanceChannel = new InstanceChannelModel();
        }

        return this.instanceChannel;

    }

    protected void requestHypeToStart() {

        Hype.setContext(getContext());
        // Add this as an Hype observer
        Hype.addStateObserver(this);
        Hype.addNetworkObserver(this);
        Hype.addMessageObserver(this);

        // Generate an app identifier in the HypeLabs dashboard (https://hypelabs.io/apps/),
        // by creating a new app. Copy the given identifier here.
        Hype.setAppIdentifier("{{app_identifier}}");

        Hype.start();

    }

    protected void requestHypeToStop()
    {
        // The current release has a known issue with Bluetooth Low Energy that causes all
        // connections to drop when the SDK is stopped. This is an Android issue.
        Hype.stop();
    }

    /**
     * This notification indicates that the given identifier for vendor
     * joined a channel with the given identity.
     * @param identifierForVendor Identifier for vendor of the device joined.
     * @param channel Channel that has been joined.
     * @param identity Identity of the device that joined the channel.
     */
    public void identifierForVendorDidJoinChannelWithIdentity(String identifierForVendor, ChannelModel channel, String identity) {

        Instance instance = this.getInstanceChannel().getinstanceWithIdentifierVendor(identifierForVendor);
        this.getInstanceChannel().setChannelIdentifierVendor(channel, identifierForVendor);
        Map<String, String> channelDict = new HashMap<String, String>();
        channelDict.put("type", "client");
        channelDict.put("identity", identity);

        JSONObject jsonObject = new JSONObject(channelDict);
        byte [] data = new byte[0];
        try {
            data = jsonObject.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Hype.send(data, instance);

    }

    /**
     * This method sends a message to a given instance.
     * @param instance Instance that will receive the message.
     * @param text Message to send.
     * @param identifierForVendor Peer identifier for vendor.
     */
    public void sendMessageToCloserInstanceWithTextAndIdentifierForVendor(Instance instance, String text, String identifierForVendor) {

        Map<String, String> dict = new HashMap<String, String>();

        dict.put("message",text);
        dict.put("type", "send");
        dict.put("identifierForVendor", identifierForVendor);

        JSONObject jsonObject = new JSONObject(dict);
        byte [] data = null;

        try {
            data = jsonObject.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        Hype.send(data, instance);

    }

    /**
     * This method fowards a messages to saved instances.
     * @param message Message that will be foward.
     * @param instances Saved instances.
     */
    public void resendTwilioMessageToInstances(Map<String, String> message, Map<String, Instance> instances) {

        for (Map.Entry<String, Instance> entry : instances.entrySet())
        {
            Instance instance = entry.getValue();
            sendMessageToInstance(message, instance);

        }
    }

    /**
     * This method notifys class when it fails trying to connect to twilio.
     * @param response Error response.
     */
    public void failConnecting(String response){

        this.netAccess = false;
    }

    /**
     * Converts a json object to HasMap.
     * @param object Json Object
     * @return Map
     * @throws JSONException
     */
    public static Map<String, String> toMap(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, (String) value);
        }
        return map;
    }

    @Override
    public void onHypeMessageReceived(Message message, Instance instance) {

        Log.i(TAG, String.format("Hype got a message from: %s", instance.getStringIdentifier()));

        String jsonString = new String(message.getData());

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(jsonString);
        } catch (JSONException e) {
            return ;
        }

        String identifier = instance.getStringIdentifier();

        Map<String,String> response = null;
        try {
            response = toMap(jsonObj);
            if(response.get("type").equals("announcement") && response.get("twilio").equals("NO")){

                processAnnouncementResponsesWithDictionaryAndInstance(response, instance);

            }else if(response.get("type").equals("announcement") && response.get("twilio").equals("YES")){


            }else if(response.get("type").equals("client")){

                processClientWithResponse(response);

            }else if(response.get("type").equals("send")){

                processSendsWithResponse(response);

            }else if(response.get("type").equals("receive")){

                processReceivesWithResponse(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHypeMessageFailedSending(MessageInfo messageInfo, Instance instance, Error error) {
        Log.i(TAG, String.format("Hype failed to send message: %d [%s]", messageInfo.getIdentifier(), error.getDescription()));

    }

    @Override
    public void onHypeMessageSent(MessageInfo messageInfo, Instance instance, float v, boolean b) {
        Log.i(TAG, String.format("Hype is sending a message"));

    }

    @Override
    public void onHypeMessageDelivered(MessageInfo messageInfo, Instance instance, float v, boolean b) {
        Log.i(TAG, String.format("Hype delivered a message"));

    }

    @Override
    public void onHypeInstanceFound(Instance instance) {
        Log.i(TAG, String.format("Hype found instance: %s", instance.getStringIdentifier()));

        if(instance.isResolved()){

            sendResponseToResolvedInstance(instance);
            notifyHypeControlllerOnInstanceResolved(instance);

        }else{

            Hype.resolve(instance);
        }

    }

    @Override
    public void onHypeInstanceLost(Instance instance, Error error) {
        Log.i(TAG, String.format("Hype lost instance: %s [%s]", instance.getStringIdentifier(), error.getDescription()));

        notifyHypeControllerOnInstanceLost(instance);
    }

    @Override
    public void onHypeInstanceResolved(Instance instance) {
        Log.i(TAG, String.format("Hype resolved instance: %s", instance.getStringIdentifier()));

        sendResponseToResolvedInstance(instance);
        notifyHypeControlllerOnInstanceResolved(instance);

    }

    @Override
    public void onHypeInstanceFailResolving(Instance instance, Error error) {
        Log.i(TAG, String.format("Hype failed resolving instance: %s [%s]", instance.getStringIdentifier(), error.getDescription()));

    }

    @Override
    public void onHypeStart() {
        Log.i(TAG, "Hype started!");

    }

    @Override
    public void onHypeStop(Error error) {
        Log.i(TAG, String.format("Hype stopped "));

    }

    @Override
    public void onHypeFailedStarting(Error error) {
        Log.i(TAG, String.format("Hype failed starting [%s]", error.getDescription()));

    }

    @Override
    public void onHypeReady() {
        Log.i(TAG, String.format("Hype is ready"));
    }

    @Override
    public void onHypeStateChange() {
        Log.i(TAG, String.format("Hype changed state to [%d] (Idle=0, Starting=1, Running=2, Stopping=3)", Hype.getState().getValue()));

    }

    @Override
    public String onHypeRequestAccessToken(int i) {

        return "{{access_token}}";
    }

    private void sendMessageToInstance(Map<String, String> message, Instance instance){

        Map<String, String> dict = message;

        if(dict.get("type") == null){

            dict.put("type", "receive");
        }
        JSONObject jsonObject = new JSONObject(dict);
        byte [] data = new byte[0];
        try {
            data = jsonObject.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Hype.send(data, instance);

    }

    private void processAnnouncementResponsesWithDictionaryAndInstance(Map<String, String>response, Instance instance){

        this.getInstanceChannel().setInstanceIdentifierVendor(instance, response.get("vendorIdentifier"));

        HypeControllerDelegate delegate = getDelegate();

        if (delegate != null) {
            delegate.requestTwilioClientWithidentifierForVendor(response.get("vendorIdentifier"));
        }
    }

    private void processSendsWithResponse(Map<String, String> response){

        HypeControllerDelegate delegate = getDelegate();

        if (delegate != null) {
            delegate.didSendMessageFromIdentifierVendor( response.get("message"), response.get("identifierForVendor"));
        }
    }

    private void processClientWithResponse(Map<String, String> response){

            this.announcement = "MIM";
            HypeControllerDelegate delegate = getDelegate();

            if (delegate != null) {
                delegate.didJoinTwilio(response);
            }
    }

    private void processReceivesWithResponse(Map<String, String> response){

        HypeControllerDelegate delegate = getDelegate();

        if (delegate != null) {
            delegate.didReceiveMessage(response);
        }
    }

    private void sendResponseToResolvedInstance(Instance instance) {

        Map<String, String> response = new HashMap<String, String>();
        String identifierForVendor = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        response.put("type", "announcement");

        if(this.netAccess){

            response.put("twilio", "YES");

        }else{

            response.put("twilio", "NO");

        }

        response.put("vendorIdentifier", identifierForVendor);
        JSONObject jsonObject = new JSONObject(response);
        byte [] data = new byte[0];
        try {
            data = jsonObject.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Hype.send(data, instance);

    }

    private void notifyHypeControlllerOnInstanceResolved(Instance instance){

        String identifierForVendor = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        HypeControllerDelegate delegate = getDelegate();

        if (delegate != null) {
            delegate.didFoundInstanceWithIdentifierForVendor(instance, identifierForVendor);
        }
    }
    private void notifyHypeControllerOnInstanceLost(Instance instance){

        HypeControllerDelegate delegate = getDelegate();

        if (delegate != null) {
            delegate.didLoseInstance(instance);
        }
    }

    public HypeControllerDelegate getDelegate() {

        return delegateWeakReference != null ? delegateWeakReference.get() : null;
    }

    public void setHypeControllerDelegate(HypeControllerDelegate hypeControllerDelegate) {

        this.delegateWeakReference = new WeakReference<>(hypeControllerDelegate);
    }

}
