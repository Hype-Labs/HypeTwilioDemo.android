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

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.twilio.chat.Channel;
import com.twilio.chat.ChatClient;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BridgeController.BridgeControllerDelegate {

    final static String TAG = "TwilioChat";
    private String mIdentity = "USER_IDENTITY";
    private RecyclerView mMessagesRecyclerView;
    private MessagesAdapter mMessagesAdapter;
    private ArrayList<Map<String, String>> mMessages = new ArrayList<Map<String, String>>();
    private EditText mWriteMessageEditText;
    private Button mSendChatMessageButton;
    private ChatClient mChatClient;
    private Channel mGeneralChannel;
    private BridgeController bridgeController;

    private boolean netAccess = false;
    private ChannelModel channel;
    private static final int REQUEST_ACCESS_COARSE_LOCATION_ID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessagesRecyclerView = (RecyclerView) findViewById(R.id.messagesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mMessagesRecyclerView.setLayoutManager(layoutManager);
        mMessagesAdapter = new MessagesAdapter();
        mMessagesRecyclerView.setAdapter(mMessagesAdapter);
        mWriteMessageEditText = (EditText) findViewById(R.id.writeMessageEditText);
        mSendChatMessageButton = (Button) findViewById(R.id.sendChatMessageButton);

        mSendChatMessageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String messageBody = mWriteMessageEditText.getText().toString();
                getBridgeController().sendMessageToTwilioWithTextWithText(messageBody);
                mWriteMessageEditText.setText("");

            }
        });
        setTitle("Logging in ...");
        requestOwnTwilioClient();
        requestHypeToStart();

    }

    private BridgeController getBridgeController(){

        if(this.bridgeController == null){

            this.bridgeController = new BridgeController(getApplicationContext());
            this.bridgeController.setBridgeControllerDelegate(this);
        }
        return this.bridgeController;

    }

    private void requestOwnTwilioClient(){

        this.getBridgeController().generateTwilioClient();
    }

    private void requestHypeToStart(){



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION_ID);
        }
        else
        {
            this.getBridgeController().requestHypeToStart();
        }
    }

    @Override
    public void didJoinChannelWithIdentity(final ChannelModel channel, final String identity) {
        this.channel = new ChannelModel(channel.getChannel());
        this.netAccess = true;

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String messageText = String.format("Logged in as %s", identity);
                setTitle(messageText);

            }
        });
    }

    @Override
    public void didSendMessage(String response) {

        // Message sent.
    }

    @Override
    public void didReceiveMessage(final Map<String, String> message) {

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mMessages.add(message);
                mMessagesAdapter.notifyDataSetChanged();

            }
        });
    }

    @Override
    public void didJoinTwilio(final Map<String, String> response) {

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String messageText = String.format("Logged in as %s", response.get("identity"));
                setTitle(messageText);

            }
        });


    }

    @Override
    public void connectionFail(String response) {

        this.netAccess = false;

    }

    @Override
    public void didLoseInstance(String response) {

        if(response.equals("off")){


            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle("Logging in ...");

                }
            });
        }
    }

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            public TextView mMessageTextView;

            public ViewHolder(TextView textView) {
                super(textView);
                mMessageTextView = textView;
            }
        }

        public MessagesAdapter() {

        }

        @Override
        public MessagesAdapter
                .ViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
            TextView messageTextView = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_text_view, parent, false);
            return new ViewHolder(messageTextView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Map<String, String> message = mMessages.get(position);

            String messageText = String.format("%s: %s", message.get("author"), message.get("body"));

            holder.mMessageTextView.setText(messageText);

        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION_ID:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.getBridgeController().requestHypeToStart();
                }
                break;
        }
    }
}
