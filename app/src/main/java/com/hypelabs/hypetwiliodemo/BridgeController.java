//
// MIT License
//
// Copyright (C) 2015 Twilio Inc.
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

import com.hypelabs.hype.Instance;
import com.twilio.chat.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.ref.WeakReference;

public class BridgeController implements HypeController.HypeControllerDelegate, TwilioController.TwilioControllerDelegate{

    private Context context;
    private WeakReference<BridgeControllerDelegate> delegateWeakReference;
    private HypeController hypeController;
    private TwilioController twilioController;
    private InstanceChannelModel instanceChannel;
    private String identifierForvendor;
    private ArrayList sidContainer;

    public BridgeController(HypeController hypeController, TwilioController twilioController, InstanceChannelModel instanceChannel, String identifierForvendor, ArrayList sidContainer) {

        this.hypeController = hypeController;
        this.twilioController = twilioController;
        this.instanceChannel = instanceChannel;
        this.identifierForvendor = identifierForvendor;
        this.sidContainer = sidContainer;

    }

    /**
     * This controller delegate has the purpose of dealing
     * with twilio controller notifications. This delegate notifys upper classes
     * when some client joined a channel, a message was sent to twilio with
     * success, a new message arrived, if a client fails connecting to twilio
     * and Hype framework loses an instance.
     */
    public interface BridgeControllerDelegate{

        /**
         * This notification indicates that the given identifier for vendor
         * joined a channel with the given identity.
         * @param channel Channel that has been joined.
         * @param identity Identity of the device that joined the channel.
         */
        void didJoinChannelWithIdentity(ChannelModel channel, String identity);

        /**
         * This notification indicates that the message was
         * sent with success.
         * @param response Feedback given by twilio.
         */
        void didSendMessage(String response);

        /**
         * This notification indicates that the channel has a new message.
         * @param message Message received.
         */
        void didReceiveMessage(Map<String, String> message);

        /**
         * This notification indicates that the given identifier for vendor
         * joined a channel.
         * @param response Contains the identity of the client that has been joined.
         */
        void didJoinTwilio(Map<String, String> response);

        /**
         * This notification indicates that client could not join twilio channel.
         * @param response Indicates a message describing the why it failed.
         */
        void connectionFail(String response);

        /**
         * This notification indicates that lost a instance of hype framework.
         * @param response Indicates a message describing the why it failed.
         */
        void didLoseInstance(String response);
    }


    public BridgeController(Context context) {

        this.context = context;

    }

    /**
     * This method generates a twilio client.
     */
    public void generateTwilioClient(){


        String identifierForVendor = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        this.identifierForvendor = identifierForVendor;

        this.getTwilioController().generateTwilioClientWithIdentifierForVendor(identifierForVendor);
    }

    /**
     * This method sends a message to the given twilio channnel.
     * @param text Message to send.
     */
    public void sendMessageToTwilioWithTextWithText(String text){

        ChannelModel channel = this.getInstanceChannel().getchannelWithIdentifierVendor(this.identifierForvendor);

        if(channel != null){

            this.sendMessageToTwilioChannelWithText(channel, text);

        }else{

            Map<String, Instance> instancesDict = this.getInstanceChannel().getInstanceIdentifierVendor();
            String identifierForVendor = (String) instancesDict.keySet().toArray()[0];
            Instance instance = instancesDict.get(identifierForVendor);
            this.getHypeController().sendMessageToCloserInstanceWithTextAndIdentifierForVendor(instance, text, identifierForVendor);

        }
    }

    /**
     * This method requests Hype framework to start.
     */
    public void requestHypeToStart(){

        this.getHypeController().requestHypeToStart();
    }

    // HypeControllerDelegate Notifications
    @Override
    public void requestTwilioClientWithidentifierForVendor(String identifierForVendor) {

        generateTwilioClientWithIdentifierForVendor(identifierForVendor);
    }

    @Override
    public void didJoinTwilio(Map<String, String> response) {


        BridgeControllerDelegate delegate = getDelegate();

        if (delegate != null) {
            delegate.didJoinTwilio(response);
        }

    }

    @Override
    public void didSendMessageFromIdentifierVendor(String message, String identifierForVendor) {

        ChannelModel channel = this.getInstanceChannel().getchannelWithIdentifierVendor(identifierForVendor);
        sendMessageToTwilioChannelWithText(channel, message);

    }

    @Override
    public void didFoundInstanceWithIdentifierForVendor(Instance instance, String identifierForVendor) {

        this.getInstanceChannel().setInstanceIdentifierVendor(instance, identifierForVendor);
    }

    @Override
    public void didLoseInstance(Instance instance) {

        Map<String, Instance> dict = this.getInstanceChannel().instanceIdentifierVendor;

        for (Map.Entry<String, Instance> entry : dict.entrySet())
        {
            if(entry.getValue() == instance){

                dict.remove(entry.getKey());

            }
        }
        ChannelModel channel = this.getInstanceChannel().getchannelWithIdentifierVendor(identifierForvendor);

        if(dict.size() == 0 && channel == null){

            BridgeControllerDelegate delegate = getDelegate();

            if (delegate != null) {

                delegate.didLoseInstance("off");

            }
        }

    }

    @Override
    public void didReceiveMessage(Map<String, String> response) {

        manageMenssageReceptionsWithReceivedMessage(response);

    }

    // TwilioControllerDelegate Notifications
    @Override
    public void didJoinChannelWithIdentifierForVendor(ChannelModel channel, String identifierForVendor, String identity) {

        if(this.identifierForvendor == identifierForVendor){

            BridgeControllerDelegate delegate = getDelegate();

            if (delegate != null) {
                delegate.didJoinChannelWithIdentity(channel, identity);
            }

            this.getInstanceChannel().setChannelIdentifierVendor(channel, identifierForVendor);

        }else{

            this.getHypeController().identifierForVendorDidJoinChannelWithIdentity(identifierForVendor, channel, identity);
            this.getInstanceChannel().setChannelIdentifierVendor(channel, identifierForVendor);
        }

    }

    @Override
    public void didSendMessage(String response) {

        BridgeControllerDelegate delegate = getDelegate();

        if (delegate != null) {
            delegate.didSendMessage(response);
        }
    }

    @Override
    public void didReceiveMessage(Message message) {

        Map<String, String> receivedMessage  = new HashMap<String, String>();

        receivedMessage.put("sid", message.getSid());
        receivedMessage.put("body", message.getMessageBody());
        receivedMessage.put("author", message.getAuthor());

        manageMenssageReceptionsWithReceivedMessage(receivedMessage);
    }

    @Override
    public void failConnecting(String response) {


        this.getHypeController().failConnecting(response);

        BridgeControllerDelegate delegate = getDelegate();

        if (delegate != null) {
            delegate.connectionFail(response);
        }

    }

    public BridgeControllerDelegate getDelegate() {

        return delegateWeakReference != null ? delegateWeakReference.get() : null;
    }

    public void setBridgeControllerDelegate(BridgeControllerDelegate bridgeControllerDelegate) {

        this.delegateWeakReference = new WeakReference<>(bridgeControllerDelegate);
    }

    private Context getContext() {
        return context;
    }

    private ArrayList getSidContainer(){


        if(this.sidContainer == null){

            this.sidContainer = new ArrayList();

        }

        return this.sidContainer;
    }

    private HypeController getHypeController(){


        if(this.hypeController == null){

            this.hypeController = new HypeController(getContext());
            this.hypeController.setHypeControllerDelegate(this);
        }
        return this.hypeController;

    }

    private TwilioController getTwilioController(){


        if(this.twilioController == null){

            this.twilioController = new TwilioController(getContext());
            this.twilioController.setTwilioControllerDelegate(this);

        }

        return this.twilioController;

    }

    private InstanceChannelModel getInstanceChannel(){


        if(this.instanceChannel == null){

            this.instanceChannel = new InstanceChannelModel();
        }

        return this.instanceChannel;

    }

    private void generateTwilioClientWithIdentifierForVendor(String identifierForVendor){

        this.getTwilioController().generateTwilioClientWithIdentifierForVendor(identifierForVendor);
    }

    private void sendMessageToTwilioChannelWithText(ChannelModel channel, String text){


        this.getTwilioController().sendMessageToTwilioChannelWithText(channel, text);

    }

    private void manageMenssageReceptionsWithReceivedMessage(Map<String, String>receivedMessage){

        String twilioSid = receivedMessage.get("sid");
        boolean flag = this.getSidContainer().contains(twilioSid);

        if(flag){
            Map<String, Instance> instances = this.getInstanceChannel().instanceIdentifierVendor;
            this.getHypeController().resendTwilioMessageToInstances(receivedMessage, instances);

        }else{

            this.sidContainer.add(twilioSid);

            BridgeControllerDelegate delegate = getDelegate();

            if (delegate != null) {
                delegate.didReceiveMessage(receivedMessage);
            }
        }
    }

}
