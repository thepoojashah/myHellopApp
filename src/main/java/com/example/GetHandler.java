package com.example;

/*
 * #%L
 * myHellopApp
 * %%
 * Copyright (C) 2012 - 2018 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Date;

class GetHandler {

    String doGet(Queue queue) {

        Map.Entry<String, Message> entry = (Map.Entry<String, Message>) queue.getQueue().entrySet().iterator().next();
        String key = entry.getKey();
        String value = entry.getValue().getText();
        int reCount = entry.getValue().increaseCount();
        queue.getQueue().remove(key);
        Date date = new Date();
        long time = date.getTime();
        queue.getInFlightQueue().put(key, new Message(value, reCount, time));
        String message = "{\"ID\": " + key + "," + "\"Message Body\": " + value + "}";
        return message;
    }

    String doGet(Queue queue, String messageID) {

        for (Map.Entry<String, Message> iter : queue.getQueue().entrySet())
            System.out.println("Key = " + iter.getKey() + ", Value = " + iter.getValue().getText());

        queue.getQueue().remove(messageID);
        return "Extra Message Deleted";
    }

    String doGetCluster(Queue queue, String messageID) {
        String value = queue.getQueue().get(messageID).getText();
        String response = "{\"ID\": " + messageID + "," + "\"Message Body\": " + value + "}";
        return response;
    }
}
