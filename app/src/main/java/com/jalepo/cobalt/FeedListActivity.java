package com.jalepo.cobalt;

import android.content.Intent;
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

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
         class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            TextView mPostOwner;
            TextView mPostMessage;
            TextView mPostLink;
            TextView mPostStory;
            ImageView mPostImage;
            CompositeDisposable disposable = new CompositeDisposable();
            ViewHolder(CardView v) {
                super(v);
                mPostOwner = (TextView) v.findViewById(R.id.post_owner_text);
                mPostMessage = (TextView) v.findViewById(R.id.post_message_text);
                mPostStory = (TextView) v.findViewById(R.id.post_story_text);
                mPostLink = (TextView) v.findViewById(R.id.post_link_text);
                mPostImage = (ImageView) v.findViewById(R.id.post_imageview);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
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
            Feed.FeedItem item = (Feed.FeedItem) mDataset.get(position);

            holder.mPostOwner.setText(item.from.getName());
            holder.mPostMessage.setText(item.message);
            holder.mPostStory.setText(item.story);

            if(item.type.equals("photo")) {
                final String photoId = item.object_id;
                feedFetchHelper.photoDataService.getPhotos(photoId,
                        feedFetchHelper.photoFields, accessToken)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Photos.Photo>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                holder.disposable.add(d);
                            }

                            @Override
                            public void onSuccess(Photos.Photo value) {
                                if(value.images != null) {
                                    String url = value.images.get(0).source;
                                    Picasso.with(getApplicationContext())
                                            .load(url)
                                            .into(holder.mPostImage);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });

            }
            if(item.type.equals("video")) {
                final String videoId = item.object_id;
                feedFetchHelper.videoDataService.getVideos(videoId,
                        feedFetchHelper.videoFields, accessToken)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Videos.Video>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                holder.disposable.add(d);
                            }

                            @Override
                            public void onSuccess(Videos.Video value) {
                                if(value.thumbnails != null) {
                                    String url = value.thumbnails.data.get(0).uri;
                                    Picasso.with(getApplicationContext())
                                            .load(url)
                                            .into(holder.mPostImage);
                                    cycleThumbnails(value, holder);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
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

        public void cycleThumbnails(final Videos.Video video, final ViewHolder holder) {
            // Fetch the thumbnails into Picasso's cache, for smoother cycling
            for(Videos.Video.Thumbnails.Thumbnail thumb: video.thumbnails.data) {
                Picasso.with(getApplicationContext()).load(thumb.uri).fetch();
            }

            Observable.interval(1, 1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        int displayIndex = 0;
                        @Override
                        public void onSubscribe(Disposable d) {
                            holder.disposable.add(d);
                        }

                        @Override
                        public void onNext(Long value) {
                            if(displayIndex < video.thumbnails.data.size() - 1) {
                                displayIndex++;
                            } else {
                                displayIndex = 0;
                            }
                            String url = video.thumbnails.data.get(displayIndex).uri;
                            Picasso.with(getApplicationContext())
                                    .load(url)
                                    .into(holder.mPostImage);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

    }



}
