package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.agora.media.RtcTokenBuilder2;
import com.example.weather.details.AgoraInfo;
import com.example.weather.details.EventType;
import com.example.weather.details.Toogle;
import com.example.weather.interfaces.NotificationListener;
import com.example.weather.models.AppInfoListener;
import com.example.weather.models.UserModel;
import com.example.weather.services.AgoraService;
import com.example.weather.utils.FirebaseUtils;

public class CallActivity extends AppCompatActivity implements NotificationListener {
    private String token = null;
    private String appCertificate;
    private String appId;
    private String channel;
    private boolean isJoined;

    private UserModel agoraUser;

    private AppInfoListener appInfoListener;

    private TextView roomCodeTV, userNameTV;
    private ImageView callBtn,micBtn,deafenBtn;

    public static NotificationListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        listener = this;
        initViews();
        agoraUser = (UserModel) getIntent().getSerializableExtra(AgoraInfo.USER);
        userNameTV.setText(agoraUser.getName());
        channel = getIntent().getStringExtra(AgoraInfo.CHANNEL);
        roomCodeTV.setText(channel);
        setMicImage();
        setDeafenImage();

        appInfoListener = appInfo -> {
            if (appInfo != null) {
                appCertificate = appInfo.getAppCertificate();
                appId = appInfo.getAppId();

                token = generateToken();


                if (getIntent().getStringExtra(EventType.TYPE).equals(EventType.CREATED)){
                    startAgora();
                }


            } else {
                Toast.makeText(this, "No App Info exists", Toast.LENGTH_SHORT).show();
            }
        };


        if (getIntent().getStringExtra(EventType.TYPE).equals(EventType.PENDING_INTENT)){

        }
        else {
            FirebaseUtils.getAppInfo(appInfoListener);
        }




    }

    private void startAgora() {
        Intent serviceIntent = new Intent(this, AgoraService.class);
        serviceIntent.putExtra(AgoraInfo.USER, agoraUser);
        serviceIntent.putExtra(AgoraInfo.APP_ID, appId);
        serviceIntent.putExtra(AgoraInfo.CERTIFICATE, appCertificate);
        serviceIntent.putExtra(AgoraInfo.CHANNEL, channel);
        serviceIntent.putExtra(AgoraInfo.TOKEN, token);
        startService(serviceIntent);
    }

    private void initViews() {
        roomCodeTV = findViewById(R.id.roomCode);
        userNameTV = findViewById(R.id.userName);
        callBtn = findViewById(R.id.callBtn);
//        callBtn.setOnTouchListener(new OnTouch(this));
        micBtn = findViewById(R.id.micBtn);
//        micBtn.setOnTouchListener(new OnTouch(this));
        deafenBtn = findViewById(R.id.deafenBtn);
//        deafenBtn.setOnTouchListener(new OnTouch(this));

        if (getIntent().getStringExtra(EventType.TYPE).equals(EventType.CREATED) || getIntent().getStringExtra(EventType.TYPE).equals(EventType.PENDING_INTENT)){
            updateIcons();

//            timerListner.startTimer(0);
//            timer.setVisibility(View.VISIBLE);
            //createNotification(mic);
        }
    }

    private void updateIcons() {
        callBtn.setImageResource(R.drawable.call_end);
        Toogle.isCallActive = true;
        micBtn.setVisibility(View.VISIBLE);
        deafenBtn.setVisibility(View.VISIBLE);
        isJoined = true;
    }


    private String generateToken() {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        int time = (int) (System.currentTimeMillis()/1000 + 60*5);
        return tokenBuilder.buildTokenWithUid(appId,appCertificate,channel,0, RtcTokenBuilder2.Role.ROLE_PUBLISHER,time,time);
    }



    public void joinCall(View view) {
        isJoined = !isJoined;
        if (isJoined){
            if (token == null){
                Toast.makeText(this, "No token", Toast.LENGTH_SHORT).show();
                isJoined = false;
                return;
            }
            startAgora();
            updateIcons();

        }else {
            AgoraService.listener.onHangUp();
            finish();
        }
    }

    private void toogleMic(boolean isTap) {
        if (isTap){
            Toogle.isMute = !Toogle.isMute;
            AgoraService.listener.onToogleMic();
        }
        setMicImage();
    }
    private void toogleDeafen(boolean isTap) {
        if (isTap){
            Toogle.isDeafen = !Toogle.isDeafen;
            AgoraService.listener.onToogleDeafen();
        }
        setDeafenImage();
    }

    private void setDeafenImage() {
        if(Toogle.isDeafen){
            deafenBtn.setImageResource(R.drawable.deafen_on);
        }
        else{
            deafenBtn.setImageResource(R.drawable.deafen_off);
        }
    }

    private void setMicImage() {
        if(Toogle.isMute){
            micBtn.setImageResource(R.drawable.mic_off);
        }
        else{
            micBtn.setImageResource(R.drawable.mic_on);
        }
    }
    @Override
    public void onToogleMic() {
        toogleMic(false);
    }

    @Override
    protected void onDestroy() {
        disconnect();

        super.onDestroy();
    }
    private void disconnect() {
        callBtn.setImageResource(R.drawable.call_end);
        micBtn.setVisibility(View.GONE);
        deafenBtn.setVisibility(View.GONE);

//        timerListner.endTimer();
    }

    @Override
    public void onHangUp() {
        finish();
    }

    @Override
    public void onToogleDeafen() {
        toogleDeafen(false);
    }

    @Override
    public void onUpdateTime(int sec) {

    }

    public void mic(View view) {
        toogleMic(true);
    }

    public void deafen(View view) {
        toogleDeafen(true);
    }
}