package com.duynguyen.socket;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.duynguyen.server.Config;
import com.duynguyen.utils.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIO {

    public static Socket socket;
    public static boolean isInitialized;
    public static boolean connected;

    public static void init() {
        if (isInitialized) {
            return;
        }
        isInitialized = true;
        reconnect(1);
    }

    public static void listen() {
    }

    public static void connect() {
        if (connected) {
            return;
        }
        try {
            Config config = Config.getInstance();
            socket = IO.socket(config.getWebsocketHost() + ":" + config.getWebsocketPort());
            socket.connect();
            listen();
            connected = true;
            Log.info("Connect to socket server successfully!");
        } catch (Exception e) {
            Log.error("Can not connect to socket server", e);
            reconnect(10000);
        }
    }

    public static void reconnect(long time) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(time);
                } catch (Exception ex) {
                    Log.error("Reconnect error", ex);
                }
                connect();
            }

        })).start();
    }

    public static void on(String event, IAction action) {
        socket.on(event, new Emitter.Listener() {
            public void call(Object... args) {
            }
        });
    }

    public static void on(byte event, IAction action) {
        on(String.valueOf(event), action);
    }

    public static void emit(String event, String data) {
        Object obj = null;
        try {
            obj = new JSONObject(data);
        } catch (JSONException e) {
            obj = data;
        }

        while (true) {
            try {
                JSONObject send = new JSONObject();
                send.put("data", obj);
                socket.emit(event, send);
                break;
            } catch (JSONException e) {
                Log.error("Emit error", e);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SocketIO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static void emit(byte event, String data) {
        emit(String.valueOf(event), data);
    }

    public static void disconnect() {
        socket.disconnect();
    }

}

