package com.jalepo.cobalt;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendListActivity extends CobaltActivity {

//    private ArrayList<Users.User> userDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        mRecyclerView = (RecyclerView) findViewById(R.id.friend_list_view);

        mViewType = FRIENDLIST;
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new FriendListAdapter(userList);
        mRecyclerView.setAdapter(mAdapter);
    }

    public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {
        ArrayList<Users.User> mDataset;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView userName;
            public ViewHolder(View itemView) {
                super(itemView);
                userName = (TextView) itemView.findViewById(R.id.friend_name_text);
            }
        }

        public FriendListAdapter(ArrayList<Users.User> data) {
            mDataset = data;
        }

        @Override
        public FriendListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_friendlist, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;        }

        @Override
        public void onBindViewHolder(FriendListAdapter.ViewHolder holder, int position) {
            holder.userName.setText(mDataset.get(position).name);
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}
