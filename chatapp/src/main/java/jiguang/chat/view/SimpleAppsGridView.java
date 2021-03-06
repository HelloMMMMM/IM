package jiguang.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import jiguang.chat.R;
import jiguang.chat.adapter.AppsAdapter;
import jiguang.chat.model.AppBean;

public class SimpleAppsGridView extends RelativeLayout {

    protected View view;
    private boolean isSingle;

    public SimpleAppsGridView(Context context) {
        this(context, null, false);
    }

    public SimpleAppsGridView(Context context, boolean isSingle) {
        this(context, null, isSingle);
    }

    public SimpleAppsGridView(Context context, AttributeSet attrs, boolean isSingle) {
        super(context, attrs);
        this.isSingle = isSingle;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_apps, this);
        init();
    }

    protected void init() {
        GridView gv_apps = (GridView) view.findViewById(R.id.gv_apps);
        ArrayList<AppBean> mAppBeanList = new ArrayList<>();
        mAppBeanList.add(new AppBean(R.mipmap.icon_photo, "图片"));
        mAppBeanList.add(new AppBean(R.mipmap.icon_camera, "拍摄"));
        mAppBeanList.add(new AppBean(R.mipmap.icon_file, "文件"));
        // TODO: 2019/3/12/012 暂时隐藏位置
        //mAppBeanList.add(new AppBean(R.mipmap.icon_loaction, "位置"));
        mAppBeanList.add(new AppBean(R.mipmap.businesscard, "名片"));
        if (isSingle) {
            mAppBeanList.add(new AppBean(R.mipmap.icon_audio, "视频"));
            mAppBeanList.add(new AppBean(R.mipmap.icon_voice, "语音"));
        }
        AppsAdapter adapter = new AppsAdapter(getContext(), mAppBeanList);
        gv_apps.setAdapter(adapter);
    }
}
