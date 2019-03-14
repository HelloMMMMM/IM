package jiguang.chat.event;

import android.view.SurfaceView;

import java.util.List;

import cn.jiguang.jmrtc.api.JMRtcClient;
import cn.jiguang.jmrtc.api.JMRtcSession;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * author:helloM
 * email:1694327880@qq.com
 */
public class JMRTCEvent implements Event {
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public JMRTCEvent(int type) {
        this.type = type;
    }

    private JMRtcSession callSession;
    private UserInfo fromUserInfo;
    private List<UserInfo> invitedUserInfos;
    private SurfaceView localSurfaceView;
    private UserInfo joinedUserInfo;
    private SurfaceView remoteSurfaceView;
    private String[] requiredPermissions;
    private UserInfo leavedUserInfo;
    private JMRtcClient.DisconnectReason reason;
    private int errorCode;
    private String desc;
    private UserInfo remoteUser;
    private boolean isMuted;

    public JMRtcSession getCallSession() {
        return callSession;
    }

    public void setCallSession(JMRtcSession callSession) {
        this.callSession = callSession;
    }

    public UserInfo getFromUserInfo() {
        return fromUserInfo;
    }

    public void setFromUserInfo(UserInfo fromUserInfo) {
        this.fromUserInfo = fromUserInfo;
    }

    public List<UserInfo> getInvitedUserInfos() {
        return invitedUserInfos;
    }

    public void setInvitedUserInfos(List<UserInfo> invitedUserInfos) {
        this.invitedUserInfos = invitedUserInfos;
    }

    public SurfaceView getLocalSurfaceView() {
        return localSurfaceView;
    }

    public void setLocalSurfaceView(SurfaceView localSurfaceView) {
        this.localSurfaceView = localSurfaceView;
    }

    public UserInfo getJoinedUserInfo() {
        return joinedUserInfo;
    }

    public void setJoinedUserInfo(UserInfo joinedUserInfo) {
        this.joinedUserInfo = joinedUserInfo;
    }

    public SurfaceView getRemoteSurfaceView() {
        return remoteSurfaceView;
    }

    public void setRemoteSurfaceView(SurfaceView remoteSurfaceView) {
        this.remoteSurfaceView = remoteSurfaceView;
    }

    public String[] getRequiredPermissions() {
        return requiredPermissions;
    }

    public void setRequiredPermissions(String[] requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    public UserInfo getLeavedUserInfo() {
        return leavedUserInfo;
    }

    public void setLeavedUserInfo(UserInfo leavedUserInfo) {
        this.leavedUserInfo = leavedUserInfo;
    }

    public JMRtcClient.DisconnectReason getReason() {
        return reason;
    }

    public void setReason(JMRtcClient.DisconnectReason reason) {
        this.reason = reason;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public UserInfo getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(UserInfo remoteUser) {
        this.remoteUser = remoteUser;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }
}
