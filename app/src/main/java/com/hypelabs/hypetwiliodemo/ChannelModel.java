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

import com.twilio.chat.Channel;

/**
 * This class wraps a Channel into to a
 * ChannelModel so we can create a custom object.
 */
public class ChannelModel {

    private Channel channel;

    /**
     * Initializes an instance object with a given channel.
     * @param channel Channel received.
     */
    public ChannelModel(Channel channel) {

        this.channel = channel;
    }

    /**
     *
     * @return current Channel
     */
    public Channel getChannel() {

        return channel;
    }

}
