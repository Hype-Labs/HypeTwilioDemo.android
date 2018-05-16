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

import com.hypelabs.hype.Instance;
import java.util.HashMap;
import java.util.Map;

/**
 * This class maps Hype instances with idenfiers for vendor
 * and ChannelModels with identifiers for vendor, so we can
 * associate hype instances with channels.
 */
public class InstanceChannelModel {

    /**
     * This Map maps Hype instances with identifier for vendors.
     */
    public Map<String, Instance> instanceIdentifierVendor;

    /**
     * @return
     */
    public Map<String, Instance> getInstanceIdentifierVendor() {

        if (instanceIdentifierVendor == null) {
            this.instanceIdentifierVendor = new HashMap<String, Instance>();
        }

        return this.instanceIdentifierVendor;
    }

    /**
     * Sets an Instance object with a given identifier vendor.
     * @param instance Instance object received.
     * @param identifierVendor Identifier for vendor received.
     */
    public void setInstanceIdentifierVendor(Instance instance, String identifierVendor){

        getInstanceIdentifierVendor().put(identifierVendor, instance);

    }

    /**
     * Sets an HYPTwilioChannel object with a given identifier vendor.
     * @param channel ChannelModel object received.
     * @param identifierVendor Identifier for vendor received.
     */
    public void setChannelIdentifierVendor(ChannelModel channel, String identifierVendor){

        getChannelIdentifierVendor().put(identifierVendor, channel);

    }

    /**
     * Gets an Instance object with a given identifier vendor.
     * @param identifierVendor Identifier for vendor received.
     * @return Instance object
     */
    public Instance getinstanceWithIdentifierVendor(String identifierVendor){


        Instance instance = getInstanceIdentifierVendor().get(identifierVendor);
        return instance;
    }

    /**
     * Gets an ChannelModel object with a given identifier vendor.
     * @param identifierVendor Identifier for vendor received.
     * @return ChannelModel object
     */
    public ChannelModel getchannelWithIdentifierVendor(String identifierVendor){

        ChannelModel channel = getChannelIdentifierVendor().get(identifierVendor);
        return channel;

    }

    private Map<String, ChannelModel> channelIdentifierVendor;

    private Map<String, ChannelModel> getChannelIdentifierVendor() {

        if (channelIdentifierVendor == null) {
            this.channelIdentifierVendor = new HashMap<String, ChannelModel>();
        }
        return this.channelIdentifierVendor;
    }

}
