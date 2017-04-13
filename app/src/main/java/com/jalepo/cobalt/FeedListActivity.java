package com.jalepo.cobalt;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class FeedListActivity extends CobaltActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedlist);

        mViewType = FEEDLIST;
        mRecyclerView = (RecyclerView) findViewById(R.id.feed_list_view);

        mRecyclerView.setHasFixedSize(true);

        mAdapter = new FeedListAdapter(dataList);
        mRecyclerView.setAdapter(mAdapter);

        feedFilter = new Predicate<Feed.FeedItem>() {
            @Override
            public boolean test(Feed.FeedItem feedItem) throws Exception {
                return feedItem.type.equals("status") ||
                        feedItem.type.equals("photo") ||
                        feedItem.type.equals("video");
            }
        };

    }




    public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.ViewHolder> {
        private ArrayList<Feed.FeedItem> mDataset;

         class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView mPostOwner;
            TextView mPostDate;
            TextView mPostMessage;
            TextView mPostLink;
            TextView mPostStory;
            ImageView mPostImage;
            CompositeDisposable disposable = new CompositeDisposable();
            String mExternalLink;

            ViewHolder(CardView v) {
                super(v);
                mPostOwner = (TextView) v.findViewById(R.id.post_owner_text);
                mPostDate = (TextView) v.findViewById(R.id.post_created_date);
                mPostMessage = (TextView) v.findViewById(R.id.post_message_text);
                mPostStory = (TextView) v.findViewById(R.id.post_story_text);
                mPostLink = (TextView) v.findViewById(R.id.post_link_text);
                mPostImage = (ImageView) v.findViewById(R.id.post_imageview);
                v.setOnClickListener(this);
            }


             @Override
             public void onClick(View view) {
                 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mExternalLink));
                 startActivity(browserIntent);
             }
         }

        public FeedListAdapter(ArrayList<Feed.FeedItem> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public FeedListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
            // create a new view
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_feedlist, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            Feed.FeedItem item = mDataset.get(position);

            holder.mPostOwner.setText(item.from.getName());
            holder.mPostDate.setText(getCreatedDate(item.created_time));
            holder.mPostMessage.setText(item.message);
            holder.mPostStory.setText(item.story);
            holder.mExternalLink = item.link;

            if(item.type.equals("photo")) {
                final String photoId = item.object_id;
                getRemoteImage(holder.mPostImage, photoId, holder.disposable);
            }
            if(item.type.equals("video")) {
                final String videoId = item.object_id;
                getRemoteVideo(holder.mPostImage, videoId, holder.disposable, true);

            }
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            holder.mPostImage.setImageDrawable(null);
            holder.mPostLink.setText("");
            holder.disposable.clear();
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }



    }



}
