package com.nexusunsky.lh.messengerdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * MessengerService服务端:用于处理客户端的连接请求。
 * 1,同时创建一个Handler,并通过该Handler创建Messenger对象,
 * 2,在service的onBind中返回这个Messenger对象底层的Binder即可。
 *
 * @time 16/10/9 上午9:51
 */
public class MessengerService extends Service {

    private static String TAG = "MessengerService";

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyConstants.CLIENT_TO_SERVER:
                    Log.d(TAG, "receive message from client :" + msg.getData().getString("msg"));
                    Messenger clientMessenger = msg.replyTo;
                    Message s2c = Message.obtain(null, MyConstants.SERVER_TO_CLIENT);
                    Bundle data = new Bundle();
                    data.putString("msg", "hello client,I have got your Message.");
                    s2c.setData(data);
                    try {
                        clientMessenger.send(s2c);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new MessengerHandler());//用于服务端回应客户端的Messenger

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
