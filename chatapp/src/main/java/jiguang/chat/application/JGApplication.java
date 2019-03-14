package jiguang.chat.application;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.SurfaceView;

import com.activeandroid.ActiveAndroid;
import com.baidu.mapapi.SDKInitializer;
import com.facebook.drawee.backends.pipeline.Fresco;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jiguang.jmrtc.api.JMRtcClient;
import cn.jiguang.jmrtc.api.JMRtcListener;
import cn.jiguang.jmrtc.api.JMRtcSession;
import cn.jiguang.jmrtc.api.JMSignalingMessage;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.database.UserEntry;
import jiguang.chat.entity.NotificationClickEventReceiver;
import jiguang.chat.event.JMRTCEvent;
import jiguang.chat.jmrtc.JMRTCActivity;
import jiguang.chat.location.service.LocationService;
import jiguang.chat.pickerimage.utils.StorageUtil;
import jiguang.chat.utils.SharePreferenceManager;
import jiguang.chat.utils.imagepicker.GlideImageLoader;
import jiguang.chat.utils.imagepicker.ImagePicker;
import jiguang.chat.utils.imagepicker.view.CropImageView;

/**
 * Created by ${chenyn} on 2017/2/16.
 */

//使用的数据库需要继承这个application
public class JGApplication extends com.activeandroid.app.Application {
    public static final String CONV_TITLE = "conv_title";
    public static final int IMAGE_MESSAGE = 1;
    public static final int TAKE_PHOTO_MESSAGE = 2;
    public static final int TAKE_LOCATION = 3;
    public static final int FILE_MESSAGE = 4;
    public static final int TACK_VIDEO = 5;
    public static final int TACK_VOICE = 6;
    public static final int BUSINESS_CARD = 7;
    public static final int REQUEST_CODE_SEND_FILE = 26;


    public static final int RESULT_CODE_ALL_MEMBER = 22;
    public static Map<Long, Boolean> isAtMe = new HashMap<>();
    public static Map<Long, Boolean> isAtall = new HashMap<>();
    public static List<Message> forwardMsg = new ArrayList<>();

    public static long registerOrLogin = 1;
    public static final int REQUEST_CODE_TAKE_PHOTO = 4;
    public static final int REQUEST_CODE_SELECT_PICTURE = 6;
    public static final int REQUEST_CODE_CROP_PICTURE = 18;
    public static final int REQUEST_CODE_CHAT_DETAIL = 14;
    public static final int RESULT_CODE_FRIEND_INFO = 17;
    public static final int REQUEST_CODE_ALL_MEMBER = 21;
    public static final int RESULT_CODE_EDIT_NOTENAME = 29;
    public static final String NOTENAME = "notename";
    public static final int REQUEST_CODE_AT_MEMBER = 30;
    public static final int RESULT_CODE_AT_MEMBER = 31;
    public static final int RESULT_CODE_AT_ALL = 32;
    public static final int SEARCH_AT_MEMBER_CODE = 33;

    public static final int RESULT_BUTTON = 2;
    public static final int START_YEAR = 1900;
    public static final int END_YEAR = 2050;
    public static final int RESULT_CODE_SELECT_FRIEND = 23;

    public static final int REQUEST_CODE_SELECT_ALBUM = 10;
    public static final int RESULT_CODE_SELECT_ALBUM = 11;
    public static final int RESULT_CODE_SELECT_PICTURE = 8;
    public static final int REQUEST_CODE_BROWSER_PICTURE = 12;
    public static final int RESULT_CODE_BROWSER_PICTURE = 13;
    public static final int RESULT_CODE_SEND_LOCATION = 25;
    public static final int RESULT_CODE_SEND_FILE = 27;
    public static final int REQUEST_CODE_SEND_LOCATION = 24;
    public static final int REQUEST_CODE_FRIEND_INFO = 16;
    public static final int RESULT_CODE_CHAT_DETAIL = 15;
    public static final int ON_GROUP_EVENT = 3004;
    public static final String DELETE_MODE = "deleteMode";
    public static final int RESULT_CODE_ME_INFO = 20;

    public static final String DRAFT = "draft";
    public static final String GROUP_ID = "groupId";
    public static final String POSITION = "position";
    public static final String MsgIDs = "msgIDs";
    public static final String NAME = "name";
    public static final String ATALL = "atall";
    public static final String SEARCH_AT_MEMBER_NAME = "search_at_member_name";
    public static final String SEARCH_AT_MEMBER_USERNAME = "search_at_member_username";
    public static final String SEARCH_AT_APPKEY = "search_at_appkey";

    public static final String MEMBERS_COUNT = "membersCount";

    public static String PICTURE_DIR = "sdcard/JChatDemo/pictures/";
    private static final String JCHAT_CONFIGS = "JChat_configs";
    public static String FILE_DIR = "sdcard/JChatDemo/recvFiles/";
    public static String VIDEO_DIR = "sdcarVIDEOd/JChatDemo/sendFiles/";
    public static final String TARGET_ID = "targetId";
    public static final String ATUSER = "atuser";
    public static final String TARGET_APP_KEY = "targetAppKey";
    public static int maxImgCount;               //允许选择图片最大数
    public static final String GROUP_NAME = "groupName";

    public static Context context;
    public static LocationService locationService;

    public static List<GroupInfo> mGroupInfoList = new ArrayList<>();
    public static List<UserInfo> mFriendInfoList = new ArrayList<>();
    public static List<UserInfo> mSearchGroup = new ArrayList<>();
    public static List<UserInfo> mSearchAtMember = new ArrayList<>();
    public static List<Message> ids = new ArrayList<>();
    public static List<UserInfo> alreadyRead = new ArrayList<>();
    public static List<UserInfo> unRead = new ArrayList<>();
    public static List<String> forAddFriend = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        StorageUtil.init(context, null);

        Fresco.initialize(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());
        locationService = new LocationService(getApplicationContext());

        JMessageClient.init(getApplicationContext(), true);
        JMessageClient.setDebugMode(true);
        SharePreferenceManager.init(getApplicationContext(), JCHAT_CONFIGS);
        //设置Notification的模式
        JMessageClient.setNotificationFlag(JMessageClient.FLAG_NOTIFY_WITH_SOUND | JMessageClient.FLAG_NOTIFY_WITH_LED | JMessageClient.FLAG_NOTIFY_WITH_VIBRATE);
        //注册Notification点击的接收器
        new NotificationClickEventReceiver(getApplicationContext());
        initJMRtcListener();
        initImagePicker();

    }

    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);                      //显示拍照按钮
        imagePicker.setCrop(true);                           //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true);                   //是否按矩形区域保存
        imagePicker.setSelectLimit(maxImgCount);              //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);                       //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);                      //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);                         //保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);                         //保存文件的高度。单位像素
    }

    public static void setPicturePath(String appKey) {
        if (!SharePreferenceManager.getCachedAppKey().equals(appKey)) {
            SharePreferenceManager.setCachedAppKey(appKey);
            PICTURE_DIR = "sdcard/JChatDemo/pictures/" + appKey + "/";
        }
    }

    public static UserEntry getUserEntry() {
        return UserEntry.getUser(JMessageClient.getMyInfo().getUserName(), JMessageClient.getMyInfo().getAppKey());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * 监听通话邀请
     */
    public void initJMRtcListener() {
        JMRtcClient.getInstance().initEngine(jmRtcListener);
        initBaseSetting();
    }

    private void initBaseSetting() {
        JMRtcClient.getInstance().setVideoProfile(JMRtcClient.VideoProfile.Profile_720P);
        JMRtcClient.getInstance().enableAudio(true);
        JMRtcClient.getInstance().enableSpeakerphone(true);
    }

    /**
     * 释放音视频
     */
    public void releaseJMRtcListener() {
        JMRtcClient.getInstance().releaseEngine();
    }

    private JMRtcListener jmRtcListener = new JMRtcListener() {
        @Override
        public void onEngineInitComplete(final int errCode, final String errDesc) {
            super.onEngineInitComplete(errCode, errDesc);
        }

        @Override
        public void onCallOutgoing(JMRtcSession callSession) {
            super.onCallOutgoing(callSession);
            JMRTCEvent jmrtcEvent = new JMRTCEvent(1);
            jmrtcEvent.setCallSession(callSession);
            EventBus.getDefault().post(jmrtcEvent);
        }

        @Override
        public void onCallInviteReceived(final JMRtcSession callSession) {
            super.onCallInviteReceived(callSession);
            //收到通话邀请
            Log.d("JMRTCActivity", "onCallInviteReceived invoked!. session = " + callSession);
            final boolean isVideo = callSession.getMediaType() == JMSignalingMessage.MediaType.VIDEO;
            callSession.getInviterUserInfo(new RequestCallback<UserInfo>() {
                @Override
                public void gotResult(int i, String s, UserInfo userInfo) {
                    Intent intent = new Intent(getApplicationContext(), JMRTCActivity.class);
                    if (isVideo) {
                        intent.putExtra("isVideo", true);
                    } else {
                        intent.putExtra("isVideo", false);
                    }
                    intent.putExtra("nickName", userInfo.getNickname());
                    intent.putExtra("isLaunch", false);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    JMRTCEvent jmrtcEvent = new JMRTCEvent(2);
                    jmrtcEvent.setCallSession(callSession);
                    EventBus.getDefault().post(jmrtcEvent);
                }
            });
        }

        @Override
        public void onCallOtherUserInvited(UserInfo fromUserInfo, List<UserInfo> invitedUserInfos, JMRtcSession callSession) {
            super.onCallOtherUserInvited(fromUserInfo, invitedUserInfos, callSession);
            JMRTCEvent jmrtcEvent = new JMRTCEvent(3);
            jmrtcEvent.setFromUserInfo(fromUserInfo);
            jmrtcEvent.setInvitedUserInfos(invitedUserInfos);
            jmrtcEvent.setCallSession(callSession);
            EventBus.getDefault().post(jmrtcEvent);
        }

        //主线程回调
        @Override
        public void onCallConnected(JMRtcSession callSession, SurfaceView localSurfaceView) {
            super.onCallConnected(callSession, localSurfaceView);
            JMRTCEvent jmrtcEvent = new JMRTCEvent(4);
            jmrtcEvent.setCallSession(callSession);
            jmrtcEvent.setLocalSurfaceView(localSurfaceView);
            EventBus.getDefault().post(jmrtcEvent);
        }

        //主线程回调
        @Override
        public void onCallMemberJoin(UserInfo joinedUserInfo, SurfaceView remoteSurfaceView) {
            super.onCallMemberJoin(joinedUserInfo, remoteSurfaceView);
            JMRTCEvent jmrtcEvent = new JMRTCEvent(5);
            jmrtcEvent.setJoinedUserInfo(joinedUserInfo);
            jmrtcEvent.setRemoteSurfaceView(remoteSurfaceView);
            EventBus.getDefault().post(jmrtcEvent);
        }

        @Override
        public void onPermissionNotGranted(final String[] requiredPermissions) {
            JMRTCEvent jmrtcEvent = new JMRTCEvent(6);
            jmrtcEvent.setRequiredPermissions(requiredPermissions);
            EventBus.getDefault().post(jmrtcEvent);
        }

        @Override
        public void onCallMemberOffline(final UserInfo leavedUserInfo, JMRtcClient.DisconnectReason reason) {
            super.onCallMemberOffline(leavedUserInfo, reason);
            JMRTCEvent jmrtcEvent = new JMRTCEvent(7);
            jmrtcEvent.setLeavedUserInfo(leavedUserInfo);
            jmrtcEvent.setReason(reason);
            EventBus.getDefault().post(jmrtcEvent);
        }

        @Override
        public void onCallDisconnected(JMRtcClient.DisconnectReason reason) {
            super.onCallDisconnected(reason);
            JMRTCEvent jmrtcEvent = new JMRTCEvent(8);
            jmrtcEvent.setReason(reason);
            EventBus.getDefault().post(jmrtcEvent);
        }

        @Override
        public void onCallError(int errorCode, String desc) {
            super.onCallError(errorCode, desc);
            JMRTCEvent jmrtcEvent = new JMRTCEvent(9);
            jmrtcEvent.setErrorCode(errorCode);
            jmrtcEvent.setDesc(desc);
            EventBus.getDefault().post(jmrtcEvent);
        }

        @Override
        public void onRemoteVideoMuted(UserInfo remoteUser, boolean isMuted) {
            super.onRemoteVideoMuted(remoteUser, isMuted);
            JMRTCEvent jmrtcEvent = new JMRTCEvent(10);
            jmrtcEvent.setRemoteUser(remoteUser);
            jmrtcEvent.setMuted(isMuted);
            EventBus.getDefault().post(jmrtcEvent);
        }
    };
}
