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
import java.util.TimerTask;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;

class ScheduledTask extends TimerTask {

    String queueName;

    Queue queue;

    ScheduledTask(String queueName, Queue queue) {
        this.queueName = queueName;
        this.queue = queue;
    }

    @Override
    public void run() {
        Date date = new Date();
        long time = date.getTime();
        LinkedHashSet<String> temporaryQueue = new LinkedHashSet<>();
        LinkedHashSet<String> temporaryDeadQueue = new LinkedHashSet<>();
        int flag1 = 0, flag2 = 0;
        int position = 0;
        for (Map.Entry<String, Message> entry : queue.getInFlightQueue().entrySet()) {
            long entryTime = entry.getValue().getTime();
            int count = entry.getValue().getCount();
            if(time-entryTime > queue.getTimeout() && count < 5) {
                temporaryQueue.add(entry.getKey());
                queue.putMessage(position, entry.getKey(),entry.getValue());
                position++;
                flag1=1;
            }
            else if(count >= queue.getMaxRDCount()) {
                flag2=1;
                temporaryDeadQueue.add(entry.getKey());
                queue.getDeadLetterQueue().put(entry.getKey(),entry.getValue());
            }
        }
        if(flag1 == 1)
            queue.getInFlightQueue().keySet().removeAll(temporaryQueue);
        if(flag2 == 1)
            queue.getInFlightQueue().keySet().removeAll(temporaryDeadQueue);
    }
}
