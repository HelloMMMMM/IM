package jiguang.chat.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import cn.jiguang.api.JCoreInterface;
import jiguang.chat.R;
import jiguang.chat.controller.MainController;
import jiguang.chat.utils.ToastUtil;
import jiguang.chat.view.MainView;

public class MainActivity extends FragmentActivity {
    private MainController mMainController;
    private MainView mMainView;

    private int backCount = 1;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler=new Handler();

        mMainView = (MainView) findViewById(R.id.main_view);
        mMainView.initModule();
        mMainController = new MainController(mMainView, this);

        mMainView.setOnClickListener(mMainController);
        mMainView.setOnPageChangeListener(mMainController);
    }

    public FragmentManager getSupportFragmentManger() {
        return getSupportFragmentManager();
    }

    @Override
    protected void onPause() {
        JCoreInterface.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        JCoreInterface.onResume(this);
        mMainController.sortConvList();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (backCount == 2) {
                System.exit(0);
            } else {
                backCount++;
                ToastUtil.shortToast(this,"再按一次退出程序");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        backCount = 1;
                    }
                }, 1500);
            }
            return true;
        }
        return false;
    }
}
