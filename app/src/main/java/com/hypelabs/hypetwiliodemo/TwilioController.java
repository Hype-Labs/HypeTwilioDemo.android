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
import android.util.Log;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.ChannelListener;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Member;
import com.twilio.chat.Message;
import com.twilio.chat.StatusListener;
import java.util.HashMap;
import java.util.Map;
import java.lang.ref.WeakReference;

public class TwilioController {

    final static String SERVER_TOKEN_URL = "http://localhost:8000/token";
    private String mIdentity = "USER_IDENTITY";
    private ChatClient mChatClient;
    final static String DEFAULT_CHANNEL_NAME = "general";
    private Channel mGeneralChannel;
    private Context context;
    private Map<ClientWrapper, String> clientDictionary;

    /**
     * This controller delegate has the purpose of dealing
     * with twilio controller notifications. This delegate notifys upper classes
     * when some client joined a channel, a message was sent to twilio with
     * success, a new message arrived or if a client fails connecting to twilio.
     */
    public interface TwilioControllerDelegate{

        /**
         * This notification indicates that the given identifier for vendor
         * joined a channel with the given identity.
         * @param channel Channel that has been joined.
         * @param identifierForVendor Identifier for Vendor of the device joined.
         * @param identity Identity of the device that joined the channel.
         */
        void didJoinChannelWithIdentifierForVendor(ChannelModel channel, String identifierForVendor, String identity);

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
        void didReceiveMessage(Message message);

        /**
         * This notification indicates that client could not join twilio channel.
         * @param response Indicates a message describing the why it failed.
         */
        void failConnecting(String response);

    }

    public TwilioController(Context context) {

        this.context = context;

    }

    /**
     * This method generates a twilio client with the given identifier.
     * @param identifierForVendor The identifier to assign.
     */
    public void generateTwilioClientWithIdentifierForVendor(final String identifierForVendor){

        String tokenURL = SERVER_TOKEN_URL + "?device=" + identifierForVendor + "&identity=" + mIdentity;

        Ion.with(getContext())
                .load(tokenURL)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            String accessToken = result.get("token").getAsString();
                            ChatClient.Properties.Builder builder = new ChatClient.Properties.Builder();
                            builder.setSynchronizationStrategy(ChatClient.SynchronizationStrategy.ALL);
                            ChatClient.Properties props = builder.createProperties();
                            ChatClient.create(getContext(),accessToken,props,new CallbackListener<ChatClient>() {

                                @Override
                                public void onSuccess(ChatClient chatClient) {
                                    mChatClient = chatClient;

                                    ClientWrapper wrapper = new ClientWrapper(chatClient);
                                    getClientDictionary().put(wrapper, identifierForVendor);
                                    loadChannels(chatClient);
                                    Log.d("twilio", "Success creating Twilio Chat Client");
                                }

                                @Override
                                public void onError(ErrorInfo errorInfo) {

                                    TwilioControllerDelegate delegate = getDelegate();

                                    if (delegate != null) {
                                        delegate.failConnecting("error");
                                    }

                                    Log.e("twilio","Error creating Twilio Chat Client: " + errorInfo.getMessage());
                                }
                            });

                        } else {
                            TwilioControllerDelegate delegate = getDelegate();

                            if (delegate != null) {
                                delegate.failConnecting("error");
                            }
                            Log.e("twilio",e.getMessage(),e);

                        }
                    }
                });
    }

    /**
     * This method sends a message to the given twilio channnel.
     * @param channel channel to send.
     * @param text message to send.
     */
    public void sendMessageToTwilioChannelWithText(ChannelModel channel, String text){

        Message message = mGeneralChannel.getMessages().createMessage(text);
        Log.d("twilio","Message created");

        channel.getChannel().getMessages().sendMessage(message, new StatusListener() {

            @Override
            public void onSuccess() {

                TwilioControllerDelegate delegate = getDelegate();

                if (delegate != null) {
                    delegate.didSendMessage("Success");
                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e("twilio","Error sending message: " + errorInfo.getMessage());

                TwilioControllerDelegate delegate = getDelegate();

                if (delegate != null) {
                    delegate.didSendMessage("Error");
                }
            }
        });

    }

    public TwilioControllerDelegate getDelegate() {

        return twilioControllerDelegate != null ? twilioControllerDelegate.get() : null;
    }

    public void setTwilioControllerDelegate(TwilioControllerDelegate twilioControllerDelegate) {

        this.twilioControllerDelegate = new WeakReference<>(twilioControllerDelegate);
    }

    private Map getClientDictionary(){


        if(this.clientDictionary == null){

            this.clientDictionary = new HashMap<ClientWrapper, String>();
        }

        return this.clientDictionary;

    }

    private Context getContext() {
        return context;
    }

    private WeakReference<TwilioControllerDelegate> twilioControllerDelegate;

    private void joinChannel(final Channel channel, final ChatClient chatClient) {
        Log.d("twilio", "Joining Channel: " + channel.getUniqueName());


        channel.join(new StatusListener() {

            public void onSuccess() {

                mGeneralChannel = channel;
                Log.d("twilio", "Joined default channel");
                mGeneralChannel.addListener(mDefaultChannelListener);

                TwilioControllerDelegate delegate = getDelegate();

                if (delegate != null) {

                    String identity = chatClient.getMyIdentity();

                    ClientWrapper clientWrapper = new ClientWrapper(chatClient);
                    String identifierForVendor = (String) getClientDictionary().get(clientWrapper);

                    ChannelModel channelModel = new ChannelModel(channel);

                    delegate.didJoinChannelWithIdentifierForVendor(channelModel, identifierForVendor, identity);
                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e("twilio","Error joining channel: " + errorInfo.getMessage());
            }
        });
    }

    private ChannelListener mDefaultChannelListener = new ChannelListener() {

        @Override
        public void onMessageAdded(Message message) {

            TwilioControllerDelegate delegate = getDelegate();

            if (delegate != null) {
                delegate.didReceiveMessage(message);
            }
        }

        @Override
        public void onMessageUpdated(Message message) {

        }

        @Override
        public void onMessageDeleted(Message message) {

        }

        @Override
        public void onMemberAdded(Member member) {

        }

        @Override
        public void onMemberUpdated(Member member) {

        }

        @Override
        public void onMemberDeleted(Member member) {

        }

        @Override
        public void onTypingStarted(Member member) {

        }

        @Override
        public void onTypingEnded(Member member) {

        }

        @Override
        public void onSynchronizationChanged(Channel channel) {

        }
    };

    private void loadChannels(final ChatClient chatClient) {

        mChatClient.getChannels().getChannel(DEFAULT_CHANNEL_NAME, new CallbackListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                if (channel != null) {
                    joinChannel(channel, chatClient);

                } else {
                    mChatClient.getChannels().createChannel(DEFAULT_CHANNEL_NAME,
                            Channel.ChannelType.PUBLIC, new CallbackListener<Channel>() {
                                @Override
                                public void onSuccess(Channel channel) {
                                    if (channel != null) {
                                        joinChannel(channel, chatClient);
                                    }
                                }

                                @Override
                                public void onError(ErrorInfo errorInfo) {
                                    Log.e("Twilio","Error creating channel: " + errorInfo.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e("Twilio","Error retrieving channel: " + errorInfo.getMessage());
            }

        });

    }
}
