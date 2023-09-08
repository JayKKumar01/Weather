package com.example.weather.broadcasts;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.weather.CallActivity;
import com.example.weather.details.Data;
import com.example.weather.details.Toogle;
import com.example.weather.services.AgoraService;

public class CallNotificationActionReceiver extends BroadcastReceiver implements Data {
    @Override
    public void onReceive(Context context, Intent intent1) {
        int requestCode = intent1.getIntExtra("requestCode", -1);

        if (requestCode == REQUEST_CODE_MUTE) {
            Toogle.isMute = !Toogle.isMute;
            AgoraService.listener.onToogleMic();
            CallActivity.listener.onToogleMic();
        } else if (requestCode == REQUEST_CODE_HANGUP) {
            AgoraService.listener.onHangUp();
            CallActivity.listener.onHangUp();
        } else if (requestCode == REQUEST_CODE_DEAFEN) {
            Toogle.isDeafen = !Toogle.isDeafen;
            AgoraService.listener.onToogleDeafen();
            CallActivity.listener.onToogleDeafen();
        }
    }
}

