package com.fanwe.swipemenu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fanwe.lib.adapter.FSimpleAdapter;
import com.fanwe.lib.swipemenu.SwipeMenu;
import com.fanwe.lib.swipemenu.adapter.SwipeMenuAdapter;
import com.fanwe.lib.swipemenu.adapter.SwipeMenuAdapterView;

public class ListViewAdapter extends FSimpleAdapter<DataModel> implements SwipeMenuAdapter
{
    private final SwipeMenuAdapterView mSwipeMenuAdapterView;

    public ListViewAdapter(SwipeMenuAdapterView swipeMenuAdapterView)
    {
        mSwipeMenuAdapterView = swipeMenuAdapterView;
    }

    @Override
    public int getLayoutId(int position, View convertView, ViewGroup parent)
    {
        /**
         * 返回item布局
         */
        return R.layout.item_list;
    }

    @Override
    public void onBindData(int position, View convertView, ViewGroup parent, final DataModel model)
    {
        final TextView textView = get(R.id.textview, convertView);
        textView.setText(model.name);
    }

    @Override
    public int getItemViewType(int position)
    {
        final DataModel model = getDataHolder().get(position);
        return model.type;
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public View onCreateMenuView(int position, ViewGroup parent)
    {
        final int type = getItemViewType(position);
        switch (type)
        {
            case DataModel.TYPE_ZERO:
                return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_swipe_menu_zero, parent, false);
            case DataModel.TYPE_ONE:
                return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_swipe_menu_one, parent, false);
            default:
                return null;
        }
    }

    @Override
    public void onBindData(final int position, View contentView, View menuView, final SwipeMenu swipeMenu)
    {
        final DataModel model = getDataHolder().get(position);

        swipeMenu.setOnStateChangeCallback(new SwipeMenu.OnStateChangeCallback()
        {
            @Override
            public void onStateChanged(boolean isOpened, SwipeMenu swipeMenu)
            {
                model.isOpened = isOpened;
            }
        });

        if (model.isOpened)
            swipeMenu.open(false);
        else
            swipeMenu.close(false);

        final Button btn_delete = menuView.findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getDataHolder().removeData(model);
            }
        });
    }

    @Override
    public SwipeMenuAdapterView getSwipeMenuAdapterView()
    {
        return mSwipeMenuAdapterView;
    }
}
