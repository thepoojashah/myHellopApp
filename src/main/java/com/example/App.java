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
import java.util.Map;
import java.util.HashMap;
import fi.iki.elonen.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.UUID;
import org.jgroups.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

public class App extends NanoHTTPD {

    public static HashMap<String, Queue> mainHashMap = new HashMap<String, Queue>();

    PutHandler implementPut = new PutHandler();

    GetHandler implementGet = new GetHandler();

    DeleteHandler implementDelete = new DeleteHandler();

    static nodeCommunication comm;

    public App() throws Exception {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    public static void main(String[] args) {
        Queue queue1 = new Queue(5, 60000);
        Queue queue2 = new Queue(6, 30000);
        Queue queue3 = new Queue(4, 60000);
        mainHashMap.put("queue1", queue1);
        mainHashMap.put("queue2", queue2);
        mainHashMap.put("queue3", queue3);
        Timer time1 = new Timer();
        Timer time2 = new Timer();
        Timer time3 = new Timer();
        ScheduledTask task1 = new ScheduledTask("queue1", queue1);
        ScheduledTask task2 = new ScheduledTask("queue2", queue2);
        ScheduledTask task3 = new ScheduledTask("queue3", queue3);
        time1.schedule(task1, 0, 5000);
        time2.schedule(task2, 0, 5000);
        time3.schedule(task3, 0, 5000);
        try {
            new App();
            comm = new nodeCommunication();
            comm.start();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        } catch (Exception e) {
            System.err.println("Error");
        }
    }

    public Object handle(Message msg) throws Exception {
        System.out.println("handle(): " + msg);
        return "Success!";
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod(); 
        String uri = session.getUri();
        Map<String, String> files = new HashMap<>(); 
        String[] uriComponents = uri.split("/");
        String response;
        if (null != method) switch (method) {
            case PUT:
                try {
                    session.parseBody(files);
                    if (uriComponents[1].equals("queue") && mainHashMap.containsKey(uriComponents[2])) {
                        String content = "";
                        String tmpFilePath = files.get("content"); 
                        try {
                            content = new String(Files.readAllBytes(Paths.get(tmpFilePath))); 
                        } catch (IOException ioe) {
                            System.out.println("Error : Copying from temp file/accessing temp file");
                        }
                        String messageID = UUID.randomUUID().toString().replace("-", "");
                        response = implementPut.doPut(mainHashMap.get(uriComponents[2]),content, messageID);
                        comm.putHandling(uriComponents[2],content, messageID);
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.CREATED, "text/plain", response);

                    }
                    else {
                        response = "Error : Given queue name doesn't exits. Usage:- PUT/queue/<queue_name>";
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", response);
                    }
                } catch (IOException ioe) {
                    return newFixedLengthResponse("Internal Error IO Exception: " + ioe.getMessage());
                } catch (ResponseException re) {
                    return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
                }
            case GET:
                if (uriComponents[1].equals("queue") && mainHashMap.containsKey(uriComponents[2])) {
                    if (mainHashMap.get(uriComponents[2]).getQueue().isEmpty()) {
                        response = "Error : trying to access an empty queue";
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", response);
                    } else {
                        response=implementGet.doGet(mainHashMap.get(uriComponents[2]));
                        try {
                            JSONParser jsonParser = new JSONParser();
                            JSONObject jsonObj = (JSONObject) jsonParser.parse(response);
                            String messageID = (String) jsonObj.get("ID");
                            System.out.println(messageID);
                            comm.getHandling(uriComponents[2], messageID);
                        }
                        catch (Exception e) {
                            System.err.println(e);
                        }
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", response);
                    }
                } else {
                    response = "Error : Given queue name doesn't exits. Usage:- GET/queue/<queue_name>";
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", response);
                }
            case DELETE:
                if (uriComponents[1].equals("queue") && mainHashMap.containsKey(uriComponents[2])) {
                    if (mainHashMap.get(uriComponents[2]).getInFlightQueue().isEmpty()) {
                        comm.deleteHandling(uriComponents[2], uriComponents[4]);
                        response = "Success : Entry deleted from queue";
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", response);
                    } else {
                        response=implementDelete.doDelete(mainHashMap.get(uriComponents[2]), uriComponents[4]);
                        comm.deleteHandling(uriComponents[2], uriComponents[4]);
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", response);
                    }
                } else {
                    response = "Error : Given queue name doesn't exits. Usage:- DELETE/queue/<queue_name>";
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", response);
                }
            default:
                break;
        }
        response = "Error : Use only GET/queue/<queue_name> | PUT/queue/<queue_name> | DELETE/queue/<queue_name>";
        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", response);
    }
}
