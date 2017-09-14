package com.example.sifter;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by jeffreywongo on 8/10/2017.
 * Used to pass data from Service to Activity
 */

public class MyReceiver extends ResultReceiver {

    private Receiver receiver;

    public MyReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public interface Receiver {
        void parseData(int resultCode, Bundle resultData);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.parseData(resultCode, resultData);
        }
    }
}
