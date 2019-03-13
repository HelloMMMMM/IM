package jiguang.chat.jmrtc;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import cn.jiguang.jmrtc.api.JMRtcClient;
import cn.jiguang.jmrtc.api.JMRtcListener;
import cn.jiguang.jmrtc.api.JMRtcSession;
import cn.jiguang.jmrtc.api.JMSignalingMessage;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.application.JGApplication;
import jiguang.chat.utils.AndroidUtils;
import jiguang.chat.utils.ToastUtil;

public class JMRTCActivity extends Activity implements View.OnClickListener {

    private static final String TAG = JMRTCActivity.class.getSimpleName();
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

    FrameLayout contentLayout;
    FrameLayout surfaceViewContainer;
    LongSparseArray<SurfaceView> surfaceViewCache = new LongSparseArray<SurfaceView>();
    UserInfo myinfo = JMessageClient.getMyInfo();

    private JMRtcSession session;//通话数据元信息对象
    boolean requestPermissionSended = false;

    private String userName, nickName;
    private boolean isVideo;
    private ImageView accept, refuse, hangup;
    private TextView tip;
    private boolean isAccept = false;
    private boolean isLaunch; //是否是发起方
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jmrtc);

        handler = new Handler();
        nickName = getIntent().getStringExtra("nickName");
        userName = getIntent().getStringExtra("userId");
        isVideo = getIntent().getBooleanExtra("isVideo", false);
        isLaunch = getIntent().getBooleanExtra("isLaunch", false);

        contentLayout = (FrameLayout) findViewById(R.id.content_layout);
        surfaceViewContainer = (FrameLayout) contentLayout.findViewById(R.id.surface_container);

        tip = (TextView) findViewById(R.id.tip);
        accept = (ImageView) findViewById(R.id.btn_accept);
        refuse = (ImageView) findViewById(R.id.btn_refuse);
        hangup = (ImageView) findViewById(R.id.btn_hangup);
        accept.setOnClickListener(this);
        refuse.setOnClickListener(this);
        hangup.setOnClickListener(this);

        acceptBefore();
        JMRtcClient.getInstance().initEngine(jmRtcListener);
        initBaseSetting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestPermissionSended) {
            if (AndroidUtils.checkPermission(this, REQUIRED_PERMISSIONS)) {
                JMRtcClient.getInstance().reinitEngine();
            } else {
                Toast.makeText(this, "缺少必要权限，音视频引擎初始化失败，请在设置中打开对应权限", Toast.LENGTH_LONG).show();
            }
        } else {
            if (isLaunch) {
                tip.setText(String.format("正在等待%s接听...", nickName));
                startCall(userName, isVideo ? JMSignalingMessage.MediaType.VIDEO : JMSignalingMessage.MediaType.AUDIO);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.shortToast(JMRTCActivity.this, "长时间无人接听,请稍候再拨");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    }
                }, 300 * 1000);
            } else {
                tip.setText(String.format("%s正在等待接听...", nickName));
            }
        }
        requestPermissionSended = false;
    }

    @Override
    public void finish() {
        super.finish();
        if (isAccept) {
            hangUp();
        } else {
            refuseCall();
        }
        handler.removeCallbacksAndMessages(null);
        JMRtcClient.getInstance().releaseEngine();
        JGApplication jgApplication = (JGApplication) getApplication();
        jgApplication.initJMRtcListener();
    }

    private void acceptBefore() {
        if (isLaunch) {
            accept.setVisibility(View.GONE);
            refuse.setVisibility(View.GONE);
            hangup.setVisibility(View.VISIBLE);
        } else {
            accept.setVisibility(View.VISIBLE);
            refuse.setVisibility(View.VISIBLE);
            hangup.setVisibility(View.GONE);
        }
    }

    private void acceptAfter() {
        accept.setVisibility(View.GONE);
        refuse.setVisibility(View.GONE);
        hangup.setVisibility(View.VISIBLE);
    }

    private void initBaseSetting() {
        JMRtcClient.getInstance().setVideoProfile(JMRtcClient.VideoProfile.Profile_480P);
        JMRtcClient.getInstance().enableAudio(true);
        JMRtcClient.getInstance().enableSpeakerphone(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:
                isAccept = true;
                acceptCall();
                break;
            case R.id.btn_refuse:
                finish();
                break;
            case R.id.btn_hangup:
                finish();
                break;
        }
    }

    private void startCall(String username, final JMSignalingMessage.MediaType mediaType) {
        JMessageClient.getUserInfo(username, new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                Log.d(TAG, "get user info complete. code = " + responseCode + " msg = " + responseMessage);
                if (null != info) {
                    JMRtcClient.getInstance().call(Collections.singletonList(info), mediaType, new BasicCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage) {
                            Log.d(TAG, "call send complete . code = " + responseCode + " msg = " + responseMessage);
                        }
                    });
                } else {
                    Log.d(TAG, "发起失败");
                }
            }
        });
    }

    //邀请
    private void inviteUser(String username) {
        JMessageClient.getUserInfo(username, new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                Log.d(TAG, "get user info complete. code = " + responseCode + " msg = " + responseMessage);
                if (null != info) {
                    JMRtcClient.getInstance().invite(Collections.singletonList(info), new BasicCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage) {
                            Log.d(TAG, "invite send complete . code = " + responseCode + " msg = " + responseMessage);
                        }
                    });
                } else {
                    Log.d(TAG, "邀请用户失败");
                }
            }
        });
    }

    private void acceptCall() {
        JMRtcClient.getInstance().accept(new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                Log.d(TAG, "accept call!. code = " + responseCode + " msg = " + responseMessage);
            }
        });
    }

    private void refuseCall() {
        JMRtcClient.getInstance().refuse(new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                Log.d(TAG, "refuse call!. code = " + responseCode + " msg = " + responseMessage);
            }
        });
    }

    private void hangUp() {
        JMRtcClient.getInstance().hangup(new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                Log.d(TAG, "hangup call!. code = " + responseCode + " msg = " + responseMessage);
            }
        });
    }

    JMRtcListener jmRtcListener = new JMRtcListener() {
        @Override
        public void onEngineInitComplete(final int errCode, final String errDesc) {
            super.onEngineInitComplete(errCode, errDesc);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(), "音视频引擎初始化完成。 errCode = " + errCode + " err Desc = " + errDesc, Toast.LENGTH_LONG).show();
                }
            });

        }

        @Override
        public void onCallOutgoing(JMRtcSession callSession) {
            super.onCallOutgoing(callSession);
            Log.d(TAG, "onCallOutgoing invoked!. session = " + callSession);
            session = callSession;
        }

        @Override
        public void onCallInviteReceived(JMRtcSession callSession) {
            super.onCallInviteReceived(callSession);
            Log.d(TAG, "onCallInviteReceived invoked!. session = " + callSession);
            session = callSession;
        }

        @Override
        public void onCallOtherUserInvited(UserInfo fromUserInfo, List<UserInfo> invitedUserInfos, JMRtcSession callSession) {
            super.onCallOtherUserInvited(fromUserInfo, invitedUserInfos, callSession);
            Log.d(TAG, "onCallOtherUserInvited invoked!. session = " + callSession + " from user = " + fromUserInfo
                    + " invited user = " + invitedUserInfos);
            session = callSession;
        }

        //主线程回调
        @Override
        public void onCallConnected(JMRtcSession callSession, SurfaceView localSurfaceView) {
            super.onCallConnected(callSession, localSurfaceView);
            Log.d(TAG, "onCallConnected invoked!. session = " + callSession + " localSerfaceView = " + localSurfaceView);
            if (isVideo) {
                tip.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(300, 400, Gravity.RIGHT);
                localSurfaceView.setLayoutParams(layoutParams);
                surfaceViewCache.append(myinfo.getUserID(), localSurfaceView);
                surfaceViewContainer.addView(localSurfaceView);
            } else {
                tip.setText(String.format("正与%s通话中...", nickName));
            }
            acceptAfter();
            session = callSession;
        }

        //主线程回调
        @Override
        public void onCallMemberJoin(UserInfo joinedUserInfo, SurfaceView remoteSurfaceView) {
            super.onCallMemberJoin(joinedUserInfo, remoteSurfaceView);
            Log.d(TAG, "onCallMemberJoin invoked!. joined user  = " + joinedUserInfo + " remoteSerfaceView = " + remoteSurfaceView);
            if (isVideo) {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                remoteSurfaceView.setLayoutParams(layoutParams);
                surfaceViewCache.append(joinedUserInfo.getUserID(), remoteSurfaceView);
                surfaceViewContainer.addView(remoteSurfaceView);
            }
        }

        @Override
        public void onPermissionNotGranted(final String[] requiredPermissions) {
            Log.d(TAG, "[onPermissionNotGranted] permission = " + requiredPermissions.length);
            try {
                AndroidUtils.requestPermission(JMRTCActivity.this, requiredPermissions);
                requestPermissionSended = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCallMemberOffline(final UserInfo leavedUserInfo, JMRtcClient.DisconnectReason reason) {
            super.onCallMemberOffline(leavedUserInfo, reason);
            Log.d(TAG, "onCallMemberOffline invoked!. leave user = " + leavedUserInfo + " reason = " + reason);
            if (isVideo) {
                surfaceViewContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        SurfaceView cachedSurfaceView = surfaceViewCache.get(leavedUserInfo.getUserID());
                        if (null != cachedSurfaceView) {
                            surfaceViewCache.remove(leavedUserInfo.getUserID());
                            surfaceViewContainer.removeView(cachedSurfaceView);
                        }
                    }
                });
            }
            ToastUtil.shortToast(JMRTCActivity.this, "对方已挂断");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        }

        @Override
        public void onCallDisconnected(JMRtcClient.DisconnectReason reason) {
            super.onCallDisconnected(reason);
            Log.d(TAG, "onCallDisconnected invoked!. reason = " + reason);
            if (isVideo) {
                surfaceViewContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        surfaceViewCache.clear();
                        surfaceViewContainer.removeAllViews();
                    }
                });
            }
            finish();
            session = null;
        }

        @Override
        public void onCallError(int errorCode, String desc) {
            super.onCallError(errorCode, desc);
            Log.d(TAG, "onCallError invoked!. errCode = " + errorCode + " desc = " + desc);
            session = null;
            finish();
        }

        @Override
        public void onRemoteVideoMuted(UserInfo remoteUser, boolean isMuted) {
            super.onRemoteVideoMuted(remoteUser, isMuted);
            Log.d(TAG, "onRemoteVideoMuted invoked!. remote user = " + remoteUser + " isMuted = " + isMuted);
            if (isVideo) {
                SurfaceView remoteSurfaceView = surfaceViewCache.get(remoteUser.getUserID());
                if (null != remoteSurfaceView) {
                    remoteSurfaceView.setVisibility(isMuted ? View.GONE : View.VISIBLE);
                }
            }
            finish();
        }
    };
}