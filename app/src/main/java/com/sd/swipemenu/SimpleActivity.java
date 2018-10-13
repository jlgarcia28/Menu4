package com.sd.swipemenu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sd.lib.swipemenu.FSwipeMenu;
import com.sd.lib.swipemenu.SwipeMenu;

public class SimpleActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final String TAG = SimpleActivity.class.getSimpleName();

    private FSwipeMenu mSwipeMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        mSwipeMenu = findViewById(R.id.swipemenu);

        mSwipeMenu.setDebug(true);

        // 设置状态变化回调
        mSwipeMenu.setOnStateChangeCallback(new SwipeMenu.OnStateChangeCallback()
        {
            @Override
            public void onStateChanged(SwipeMenu.State state, SwipeMenu swipeMenu)
            {
                Log.e(TAG, "onStateChanged:" + state);
            }
        });
        // 设置view位置变化回调
        mSwipeMenu.setOnViewPositionChangeCallback(new SwipeMenu.OnViewPositionChangeCallback()
        {
            @Override
            public void onViewPositionChanged(boolean isDrag, SwipeMenu swipeMenu)
            {
                // 获得滚动百分比
                final float scrollPercent = swipeMenu.getScrollPercent();
                Log.i(TAG, "onViewPositionChanged:" + isDrag + " " + scrollPercent);
            }
        });
        // 设置滚动状态变化回调
        mSwipeMenu.setOnScrollStateChangeCallback(new SwipeMenu.OnScrollStateChangeCallback()
        {
            @Override
            public void onScrollStateChanged(SwipeMenu.ScrollState state, SwipeMenu swipeMenu)
            {
                Log.i(TAG, "onScrollStateChanged:" + state);
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_cancel:
                // 关闭菜单，true-执行滑动动画
                mSwipeMenu.setState(SwipeMenu.State.Close, true);
                Toast.makeText(this, "click cancel", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_content:
                Toast.makeText(this, "click content", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, String.valueOf(v), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
