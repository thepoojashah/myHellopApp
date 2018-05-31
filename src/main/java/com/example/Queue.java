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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.map.ListOrderedMap;

public class Queue {

    ListOrderedMap queue = (ListOrderedMap) ListOrderedMap.decorate(new HashMap<String, Message>());

    ListOrderedMap inflightQueue = (ListOrderedMap) ListOrderedMap.decorate(new HashMap<String, Message>());

    ListOrderedMap deadletterQueue = (ListOrderedMap) ListOrderedMap.decorate(new HashMap<String, Message>());

    int maxRDCount;

    int visibilityTimeout;

    Queue(int maxRDCount, int visibilityTimeout) {

        this.maxRDCount = maxRDCount;
        this.visibilityTimeout = visibilityTimeout;
    }

    Map<String, Message> getQueue() {
        return (ListOrderedMap) this.queue;
    }

    Map<String, Message> getInFlightQueue() {
        return this.inflightQueue;
    }

    Map<String, Message> getDeadLetterQueue() {
        return this.deadletterQueue;
    }

    int getMaxRDCount() {
        return this.maxRDCount;
    }

    int getTimeout() {
        return visibilityTimeout;
    }

    void putMessage(int position, String Key, Message message) {
        this.queue.put(position, Key, message);
    }
}
