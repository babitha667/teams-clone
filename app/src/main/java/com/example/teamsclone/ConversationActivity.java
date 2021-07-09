package com.example.teamsclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class ConversationActivity extends AppCompatActivity {

    // Declare the request ID
    private static final int PERMISSION_REQ_ID = 22;

    // Basic Permissions: Audio, Camera and Storage
    // Array of permissions that we require from the OS
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Agora engine reference
    private RtcEngine mRtcEngine;

    // Views for displaying the video of local and remote user.
    private FrameLayout mLocalContainer;
    private RelativeLayout mRemoteContainer;
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;

    // Buttons for ending call, muting, camera on and off, switch camera
    private ImageView mCallBtn;
    private ImageView mMuteBtn;
    private ImageView mCameraBtn;
    private ImageView mSwitchCameraBtn;

    // Boolean variables for keeping track if the user muted, switched on camera and is on call
    private boolean mCallEnd;
    private boolean mMuted;
    private boolean mCameraOn;

    // Agora Event Handler
    private final IRtcEngineEventHandler mRtcHandler = new IRtcEngineEventHandler() {

        // This will handle when the Local user joins the channel
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("join call: ", "Join channel success, uid: "+ (uid & 0xFFFFFFFFL));
                }
            });
        }

        // This will handle when the remote user leave the channel
        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("join call: ", "User offline, uid: "+ (uid & 0xFFFFFFFFL));
                    removeRemoteVideo();
                }
            });
        }

        // This will handle when the remote video state is change
        // i.e remote user turned on the camera or turned off the camera
        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            if(state == Constants.REMOTE_VIDEO_STATE_STARTING) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("join call: ", "Remote video starting, uid: "+ (uid & 0xFFFFFFFFL));
                        setupRemoteVideo(uid);
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // Initialising the UI variables
        initUi();

        // checking if the permissions access is given
        if(checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)){
            // Required permissions are granted and now initialising the Agora engine
            initEngineAndJoinChannel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();;
        if(!mCallEnd){
            leaveChannel();
        }
        RtcEngine.destroy();
    }

    private void initUi() {
        // Containers for the local and remote video display
        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);

        mCameraOn=true;

        // Initialising the buttons
        mCallBtn = findViewById(R.id.btn_call);
        mMuteBtn = findViewById(R.id.btn_mute);
        mCameraBtn = findViewById(R.id.btn_camera);
        mSwitchCameraBtn = findViewById(R.id.btn_switch_camera);
    }

    private void initEngineAndJoinChannel(){
        // Initialising the engine
        initializeEngine();
        // Setting up the video configuration
        setupVideoConfig();
        // Setting up the local video
        setupLocalVideo();
        // Joining the channel
        joinChannel();
    }

    // Function to initialise the rtcEngine using the Agora AppID
    private void initializeEngine(){
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcHandler);
        } catch (Exception e) {
            // Failed to initialize rtcEngine due to rtc sdk issues
            Log.d("initial call: ", Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n"+Log.getStackTraceString(e));
        }
    }

    // Initialising the rtcEngine is successful
    // Enabling the local video and setting up the configuration
    private void setupVideoConfig(){
        // Enabling the local video
        mRtcEngine.enableVideo();

        // enabling the video configuration setting like video scale, bit rate, frame rate and orientation
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        ));
    }

    // Enabling the video configuration is successful
    // Add the local video to the container
    private void setupLocalVideo() {
        // Enabling the local video
        mRtcEngine.enableVideo();

        // Setting up the local video to the container and rtcEngine
        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);

        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(localVideoCanvas);
    }

    // Setting up the all the remote video streams and displaying on the screen
    private void setupRemoteVideo(int uid) {
        int count = mRemoteContainer.getChildCount();
        View view = null;
        for(int i=0;i<count;i++){
            View v = mRemoteContainer.getChildAt(i);
            if(v.getTag() instanceof  Integer && ((int) v.getTag()) == uid){
                view = v;
            }
        }

        if(view != null) {
            return;
        }

        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteContainer.addView(mRemoteView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        mRemoteView.setTag(uid);
    }

    // when a remote user leaves the video call we remove the video display of the remote user
    private void removeRemoteVideo() {
        if(mRemoteView != null){
            mRemoteContainer.removeView(mRemoteView);
        }

        mRemoteView  = null;
    }

    // After setting up everything required to start the video call
    // Joining the channel
    private void joinChannel() {
        // No token required as we didn't enable the primary/secondary certificate
        // for this project on agora.io website
        String token  = getString(R.string.agora_access_token);
        if (TextUtils.isEmpty(token)){
            token = null;
        }

        // Joining the channel with channel name as room ID to have unique channels for each room
        String newChannel = getIntent().getStringExtra("ROOM_ID");
        mRtcEngine.joinChannel(token, newChannel, "",0);
    }

    // When end call button clicked this method id initialised to leave the current channel
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Method for changing the local audio setting i.e for switching on/off the microphone
    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        mRtcEngine.enableLocalAudio(!mMuted);
        int res = mMuted ? R.drawable.microphone_off : R.drawable.microphone_on;
        mMuteBtn.setImageResource(res);
    }

    // Method for changing the local video setting i.e for switching on/off the camera
    public void onLocalVideoClicked(View view) {
        mCameraOn = !mCameraOn;
        mRtcEngine.enableLocalVideo(mCameraOn);
        int res = mCameraOn ? R.drawable.camera_on : R.drawable.camera_off;
        mCameraBtn.setImageResource(res);
    }

    // Method for changing the local video setting i.e for switching from front to back camera or vice versa
    public void onSwitchCameraClicked(View view) {
        if(mCameraOn){
            mRtcEngine.switchCamera();
        }
    }

    // Method is for ending the call
    public void onCallClicked(View view){
        if(mCallEnd) {
            // Currently no option for starting the call
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.end_call);
        }
        else{
            // when end call button clicked we finish the current activity and go back to room activity
            endCall();
            mCallEnd = true;
            finish();
        }

        showButtons(!mCallEnd);
    }

    // Currently no option to start the call with a start button
    // we start the call through room activity only
    private void startCall() {
        setupLocalVideo();
        joinChannel();
    }

    // when call is ended we remove the local, remote video and leave the channel
    private void endCall(){
        removeLocalVideo(); // removing local video
        removeRemoteVideo(); // removing remote video
        leaveChannel(); // leaving channel
    }

    // when a local/remote user end meeting and leaves the video call
    // we remove the video display of the local user
    private void removeLocalVideo() {
        if(mLocalView != null) {
            mLocalContainer.removeView(mLocalView);
        }
        mLocalView = null;
    }

    // when end call button clicked we remove the visibility of the buttons as well
    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);
    }

    // Checking if all the required permissions are granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQ_ID: {
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                        grantResults[1] != PackageManager.PERMISSION_GRANTED)  {
                    break;
                }
                initEngineAndJoinChannel();
                break;
            }
        }
    }

    // This function is to access the camera and microphone permissions while launching the meeting
    // If the user give the permission then we return true
    private boolean checkSelfPermission(String permission, int requestCode) {
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        // permission access given!
        return true;
    }

    // When chat button in the meeting clicked
    // we take the user to the chat activity with current room id
    public void chatRoom(View view){
        String roomId = getIntent().getStringExtra("ROOM_ID");
        Intent intent = new Intent(ConversationActivity.this, ChatActivity.class);
        intent.putExtra("ROOM_ID", roomId);
        startActivity(intent);
    }
}