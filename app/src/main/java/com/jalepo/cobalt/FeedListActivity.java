package com.jalepo.cobalt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FeedListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    FeedFetchHelper feedFetchHelper = new FeedFetchHelper(this);

    ArrayList<Feed.FeedItem> feedList = new ArrayList<>();
    ArrayList<Photos.Photo> photoList = new ArrayList<>();

    ProfileTracker mProfileTracker;
    String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.feed_list_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ListAdapter(feedList);
        mRecyclerView.setAdapter(mAdapter);

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                loadFeed();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the access token is null, then switch to the login activity and kill this one
        if (AccessToken.getCurrentAccessToken() == null) {
            Intent loginIntent = new Intent(getApplicationContext(), FBLoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(loginIntent);
            finish();
        } else {
            accessToken = AccessToken.getCurrentAccessToken().getToken();
            loadFeed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProfileTracker != null) {
            mProfileTracker.stopTracking();
        }
    }

    public void loadFeed() {
        if(Profile.getCurrentProfile() != null) {
            feedFetchHelper.getPageFeed(Profile.getCurrentProfile().getId(),
                    accessToken);
        }
    }

    public void updateFeedList(Feed.FeedItem newFeedItem) {
        feedList.add(newFeedItem);
        mAdapter.notifyDataSetChanged();
    }

    public void updatePhotoList(Photos.Photo newPhoto) {
        photoList.add(newPhoto);
        mAdapter.notifyDataSetChanged();
    }



    public void menuButtonClicked(View view) {
        Intent menuIntent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(menuIntent);
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
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
        public ListAdapter(ArrayList<Feed.FeedItem> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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
            if(item.type.equals("link")) {
                holder.mPostLink.setText(item.link);

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
