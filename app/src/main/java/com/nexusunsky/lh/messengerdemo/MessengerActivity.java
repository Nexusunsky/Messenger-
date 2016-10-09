package com.nexusunsky.lh.messengerdemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * 客户端进程,
 * 1,绑定服务端的Service,绑定成功后用服务端返回的IBinder对象创建一个Messenger。
 * 2,通过该Messenger对象即可向服务端发送Message对象。
 * 3,如果需要服务端能够回应客户端,就和服务端一样,创建一个Handler并创建一个新的Messenger对象
 * 并把这个Messenger对象通过Message的replyTo参数传递给服务端
 * <p>
 * 总结:
 * Client-Server模式中,跨进程通信时。Server传递Client一个IBinder对象。
 * IBinder成为了桥梁,提供给单向Messenger来发送Message给对岸的Handler;对岸的Handler来handleMessage();
 * 该Messenger同时可以携带一个Message,使用Message.replyTo参数来携带Messenger给客户端回应客户端。至此便可以完成双边通信
 *
 * @author liuhao
 * @created at   16/10/9 下午3:20
 */

public class MessengerActivity extends AppCompatActivity {

    private static final String TAG = "MessengerActivity";

    public class ClientHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyConstants.SERVER_TO_CLIENT:
                    Log.d(TAG, "receive message from server :" + msg.getData().getString("msg"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Messenger serviceMessenger = new Messenger(service);
            Log.d(TAG, name.toString());
            Message c2s = Message.obtain(null, MyConstants.CLIENT_TO_SERVER);
            Bundle data = new Bundle();
            data.putString("msg", "hello server,this comes from client.");
            Messenger messenger = new Messenger(new ClientHandler());//用于客户端回应服务端的Messenger
            c2s.replyTo = messenger;
            c2s.setData(data);
            try {
                serviceMessenger.send(c2s);//通过该Messenger对象向服务端发送Message。
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, MessengerService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
