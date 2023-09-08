package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.weather.details.AgoraInfo;
import com.example.weather.details.EventType;
import com.example.weather.models.UserModel;
import com.example.weather.utils.Base;
import com.example.weather.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_CODE = 112;
    private static final String NOTIFICATION_PERMISSION_CODE_STR = "112";
    String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private TextInputEditText etJoinName,etCode;
    private Button btnJoin;
    private boolean noPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
       // FirebaseUtils.writeAppInfo(new AppInfo("68adafb754f243049a8b1c8537f8d637","3091716378c342a09746957f939ffe66"));
    }

    private void initViews() {
        etJoinName = findViewById(R.id.etJoinName);
        etCode = findViewById(R.id.etCode);
        btnJoin = findViewById(R.id.btnJoin);
    }

    @SuppressLint("DefaultLocale")
    public void join(View view) {
        if(!isGranted()){
            askPermission();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !shouldShowRequestPermissionRationale(NOTIFICATION_PERMISSION_CODE_STR)){
            if (!noPermission){
                Toast.makeText(this, "No Notification permission", Toast.LENGTH_SHORT).show();
                getNotificationPermission();
                noPermission = true;
                return;
            }
        }else {
            noPermission = false;
        }

        if (!(etJoinName.getText() != null && !etJoinName.getText().toString().isEmpty())){
            Toast.makeText(this, "Please Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = etJoinName.getText().toString();
        String channel = null;

        if (etCode.getText() != null && !etCode.getText().toString().isEmpty()){
            channel = etCode.getText().toString().trim();
        }



        String userId = Base.generateRandomString();
        UserModel userModel = new UserModel(name, userId,false,false,System.currentTimeMillis());

        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(AgoraInfo.USER, userModel);

        if (channel == null){
            int randomNumber = new Random().nextInt(1000000);
            channel = String.format("%06d", randomNumber);
            intent.putExtra(AgoraInfo.CHANNEL, channel);
            FirebaseUtils.writeUserData(channel, userModel);
            intent.putExtra(EventType.TYPE, EventType.CREATED);
            startActivity(intent);
            return;
        }
        intent.putExtra(AgoraInfo.CHANNEL, channel);
        if (noPermission){
            intent.putExtra(EventType.NO_NOTIFICATION, EventType.NO_NOTIFICATION);
        }

        FirebaseUtils.checkCodeExists(channel, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
//                    FirebaseUtils.updateUserData(code, userModel);
                    Toast.makeText(MainActivity.this, "List Updated", Toast.LENGTH_SHORT).show();
                    intent.putExtra(EventType.TYPE, EventType.JOINED);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Code Does not exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }

        });



    }


    private void getNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
        }
    }


    void askPermission(){
        int reqCode = 1;
        ActivityCompat.requestPermissions(this,permissions, reqCode);
    }

    private boolean isGranted(){
        for(String permission: permissions){
            if(ActivityCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
}