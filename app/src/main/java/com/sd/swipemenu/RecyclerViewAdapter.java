package com.sd.swipemenu;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sd.lib.adapter.FSimpleRecyclerAdapter;
import com.sd.lib.adapter.viewholder.FRecyclerViewHolder;
import com.sd.lib.swipemenu.SwipeMenu;
import com.sd.lib.swipemenu.utils.SingleModeSwipeMenuHolder;
import com.sd.lib.swipemenu.utils.SwipeMenuHolder;

public class RecyclerViewAdapter extends FSimpleRecyclerAdapter<DataModel>
{
    public final SwipeMenuHolder mAdapterSwipeMenuHolder = new SingleModeSwipeMenuHolder();

    @Override
    public int getLayoutId(ViewGroup parent, int viewType)
    {
        return R.layout.item_list;
    }

    @Override
    public void onBindData(FRecyclerViewHolder<DataModel> holder, int position, final DataModel model)
    {
        final TextView textView = holder.get(R.id.textview);
        textView.setText(model.name);

        final SwipeMenu swipeMenu = holder.get(R.id.swipemenu);
        mAdapterSwipeMenuHolder.bind(swipeMenu, model);

        swipeMenu.getContentView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (swipeMenu.getScrollPercent() != 0)
                    swipeMenu.setState(SwipeMenu.State.Close, true);
                else
                    Toast.makeText(getContext(), "click " + model, Toast.LENGTH_SHORT).show();
            }
        });

        final Button btn_delete = swipeMenu.getMenuView(SwipeMenu.Direction.Right).findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mAdapterSwipeMenuHolder.remove(model);
                getDataHolder().removeData(model);
            }
        });
    }
}
