package com.example.weather.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.weather.CallActivity;
import com.example.weather.R;
import com.example.weather.broadcasts.CallNotificationActionReceiver;
import com.example.weather.details.AgoraInfo;
import com.example.weather.details.Data;
import com.example.weather.details.EventType;
import com.example.weather.details.Toogle;
import com.example.weather.interfaces.NotificationListener;
import com.example.weather.models.UserModel;
import com.example.weather.utils.FirebaseUtils;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class AgoraService extends Service implements Data , NotificationListener {
    private RtcEngine agoraEngine;

    private UserModel agoraUser;
    private String appId;
    private String token;
    private String channel;

    private String appCertificate;
    private NotificationManager notificationManager;
    public static NotificationListener listener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listener = this;
        agoraUser = (UserModel) intent.getSerializableExtra(AgoraInfo.USER);
        appId = intent.getStringExtra(AgoraInfo.APP_ID);
        appCertificate = intent.getStringExtra(AgoraInfo.CERTIFICATE);
        token = intent.getStringExtra(AgoraInfo.TOKEN);
        channel = intent.getStringExtra(AgoraInfo.CHANNEL);

        createNotification(Toogle.isMute,Toogle.isDeafen);
        setupVoiceSDKEngine();
        joinChannel();
        return START_STICKY;
    }


    private boolean isJoined;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote user joining the channel.
        public void onUserJoined(int uid, int elapsed) {
            Log.d("agora","Remote user joined: " + uid);
//            Toast.makeText(AgoraService.this, "Remote user joined: " + uid, Toast.LENGTH_SHORT).show();
            //runOnUiThread(()->infoText.setText("Remote user joined: " + uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            // Successfully joined a channel
            isJoined = true;
            Log.d("agora","Joined Channel " + channel);
//            Toast.makeText(AgoraService.this, "Joined Channel " + channel, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Log.d("agora","Remote user offline " + uid + " " + reason);
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            Log.d("agora","User Offline");
            // Listen for the local user leaving the channel
//            Toast.makeText(AgoraService.this, "User Offline", Toast.LENGTH_SHORT).show();
            isJoined = false;
        }
    };

    private void setupVoiceSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
    }

    private void joinChannel() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeAudio = true;
        // Set both clients as the BROADCASTER.
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        // Set the channel profile as BROADCASTING.
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

        // Join the channel with a temp token.
        // You need to specify the user ID yourself, and ensure that it is unique in the channel.
        agoraEngine.joinChannel(token, channel, 0, options);
    }

    private void createNotification(boolean isMute, boolean isDeafen) {


        Intent agoraIntent = new Intent(this, CallActivity.class);

        agoraIntent.putExtra(AgoraInfo.USER, agoraUser);
        agoraIntent.putExtra(AgoraInfo.CHANNEL,channel);
        agoraIntent.putExtra(EventType.TYPE, EventType.PENDING_INTENT);
        agoraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, agoraIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Create an explicit intent for the activity that handles the button actions
        Intent intent = new Intent(this, CallNotificationActionReceiver.class);
        intent.setAction("com.testing.testingapp.ACTION_MUTE_HANGUP");
        intent.putExtra("requestCode", REQUEST_CODE_MUTE);

        PendingIntent mutePendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE_MUTE, intent, PendingIntent.FLAG_UPDATE_CURRENT |  PendingIntent.FLAG_IMMUTABLE);
        intent.putExtra("requestCode", REQUEST_CODE_HANGUP);
        PendingIntent hangupPendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE_HANGUP, intent, PendingIntent.FLAG_UPDATE_CURRENT |  PendingIntent.FLAG_IMMUTABLE);
        intent.putExtra("requestCode",REQUEST_CODE_DEAFEN);
        PendingIntent deafenPendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE_DEAFEN, intent, PendingIntent.FLAG_UPDATE_CURRENT |  PendingIntent.FLAG_IMMUTABLE);

        String muteLabel = isMute? "start" : "pause";
        String deafenLabel = isDeafen? "stopped" : "working";
        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.call)
                .setContentTitle("Network Connected")
                .setContentText("27 degree")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.call_end, "Stop", hangupPendingIntent)
                .addAction(R.drawable.mic_on, muteLabel, mutePendingIntent)
                .addAction(R.drawable.mic_on, deafenLabel, deafenPendingIntent)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true);

        //notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the notification channel for Android Oreo and above
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("channelDescription");


                notificationManager.createNotificationChannel(channel);
            }
            startForeground(NOTIFICATION_ID, builder.build());
            notificationManager.notify(NOTIFICATION_ID, builder.build());



        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        agoraEngine.leaveChannel();

        // Destroy the engine in a sub-thread to avoid congestion
        new Thread(() -> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();
    }

    @Override
    public void onToogleMic() {
        agoraEngine.muteLocalAudioStream(Toogle.isMute);
        createNotification(Toogle.isMute,Toogle.isDeafen);
    }




    @Override
    public void onHangUp() {
        FirebaseUtils.removeUserData(channel, agoraUser);
        Toast.makeText(this, "Disconnected!", Toast.LENGTH_SHORT).show();
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
        stopSelf();
    }

    @Override
    public void onToogleDeafen() {
        agoraEngine.muteAllRemoteAudioStreams(Toogle.isDeafen);
        createNotification(Toogle.isMute,Toogle.isDeafen);
    }

    @Override
    public void onUpdateTime(int sec) {

    }
}
