//------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------

package com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PushMessageManager {
    static {
        instance = new PushMessageManager();
    }

    public final static PushMessageManager instance;
    private List<JsonElement> pushMessages;

    private PushMessageManager() {
        pushMessages = new ArrayList<>();
    }

    public synchronized boolean AddMessage(JsonElement message) {
        return (pushMessages.add(message));
    }

    public synchronized boolean checkMessage(JsonElement message) {
        boolean result = false;
        for (JsonElement msg: pushMessages) {
            result = result || Util.compareJson(msg, message);
        }
        return result;
    }

    public synchronized void  clearMessages() {
        pushMessages.clear();
    }

    public ListenableFuture<Boolean> isPushMessageReceived(final long timeout, final JsonElement message) {

        final SettableFuture<Boolean> resultFuture = SettableFuture.create();

        TimerTask task = new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                if (!pushMessages.isEmpty() && checkMessage(message)) {
                    this.cancel();
                    resultFuture.set(true);
                } else {
                    count = count + 500;
                }

                if (count > timeout) {
                    resultFuture.set(false);
                    this.cancel();
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 0, 500);

        return resultFuture;
    }
}
