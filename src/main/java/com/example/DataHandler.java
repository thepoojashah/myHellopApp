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

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.Map;
import java.util.HashMap;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataHandler {

    PutHandler putHandler = new PutHandler();

    GetHandler getHandler = new GetHandler();

    String path = "/Users/poojashah/Desktop/nanohttpd-master/Files/";

    public String update() {
        String response = "Not updated";
        try {
            String que = "queue";
            for (int i = 1; i <= 3; i++) {
                String queue_name = que + String.valueOf(i);
                String fpath = path + queue_name + ".json";
                Map<String, Message> map1 = App.mainHashMap.get(queue_name).getQueue();
                Map<String, Message> map2 = App.mainHashMap.get(queue_name).getInFlightQueue();
                Map<String, Message> map3 = App.mainHashMap.get(queue_name).getDeadLetterQueue();
                JSONArray arr = new JSONArray();
                JSONObject mainObj = new JSONObject();
                for (Map.Entry<String, Message> entry : map1.entrySet()) {
                    String id = entry.getKey();
                    String message = entry.getValue().getText();
                    int reDeliveryCount = entry.getValue().getCount();
                    JSONObject currObj = new JSONObject();
                    currObj.put("ID", id);
                    currObj.put("Message Body", message);
                    currObj.put("reDeliveryCount", reDeliveryCount);
                    arr.add(currObj);
                    mainObj.put(queue_name, arr);
                    FileWriter file = new FileWriter(fpath);
                    file.write(mainObj.toJSONString());
                    file.close();
                }
            }
            response = "Updated Successfully";
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public void initializeData() {
        try {
            String que = "queue";
            for (int i = 1; i <= 3; i++) {
                String queue_name = que + String.valueOf(i);
                String queue_inflightname = "inflightQueue" + String.valueOf(i);
                String queue_dlqname = "deadletterQueue" + String.valueOf(i);
                String fpath1 = path + queue_name + ".json";
                String fpath2 = path + queue_inflightname + ".json";
                String fpath3 = path + queue_dlqname + ".json";
                // INITIALISE ARRAY
                Object obj1 = new JSONParser().parse(new FileReader(fpath1));
                JSONObject jobj1 = (JSONObject) obj1;
                JSONArray jarr = (JSONArray) jobj1.get(queue_name);
                int size = jarr.size();
                for (int j = 0; j < size; j++) {
                    JSONObject innerObject = (JSONObject) jarr.get(j);
                    String id = (String) innerObject.get("ID");
                    String messageText = (String) innerObject.get("Message Body");
                    long reDeliveryCount = (long) innerObject.get("reDeliveryCount");
                    int rdCount = (int) (long) reDeliveryCount;
                    Message msg = new Message(messageText, rdCount);
                    App.mainHashMap.get(queue_name).getQueue().put(id, msg);

                }
                // INTIALIZE INFLIGHT ARRRAY

                obj1 = new JSONParser().parse(new FileReader(fpath2));
                jobj1 = (JSONObject) obj1;
                jarr = (JSONArray) jobj1.get(queue_name);
                size = jarr.size();
                for (int j = 0; j < size; j++) {
                    JSONObject innerObject = (JSONObject) jarr.get(j);
                    String id = (String) innerObject.get("ID");
                    String messageText = (String) innerObject.get("Message Body");
                    long reDeliveryCount = (long) innerObject.get("reDeliveryCount");
                    int rdCount = (int) (long) reDeliveryCount;
                    Message msg = new Message(messageText, rdCount);
                    App.mainHashMap.get(queue_name).getInFlightQueue().put(id, msg);
                }

                obj1 = new JSONParser().parse(new FileReader(fpath3));
                jobj1 = (JSONObject) obj1;
                jarr = (JSONArray) jobj1.get(queue_name);
                size = jarr.size();
                for (int j = 0; j < size; j++) {
                    JSONObject innerObject = (JSONObject) jarr.get(j);
                    String id = (String) innerObject.get("ID");
                    String messageText = (String) innerObject.get("Message Body");
                    long reDeliveryCount = (long) innerObject.get("reDeliveryCount");
                    int rdCount = (int) (long) reDeliveryCount;
                    Message msg = new Message(messageText, rdCount);
                    App.mainHashMap.get(queue_name).getDeadLetterQueue().put(id, msg);
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
