package com.jalepo.cobalt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class PhotoListActivity extends AppCompatActivity {
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    FeedFetchHelper feedFetchHelper = new FeedFetchHelper();

    ArrayList<Feed.FeedItem> photoList = new ArrayList<>();
    CompositeDisposable disposable = new CompositeDisposable();
    ProfileTracker mProfileTracker;
    String accessToken;
    String nextPageLink;
    boolean loadingNextPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.photo_list_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3,
                GridLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PhotoListActivity.PhotoListAdapter(photoList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!loadingNextPage && !recyclerView.canScrollVertically(1)) {
                    loadNextPage();
                    loadingNextPage = true;
                }
            }
        });
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                loadPhotos();

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
            loadPhotos();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProfileTracker != null) {
            mProfileTracker.stopTracking();
        }

        mRecyclerView.removeOnScrollListener(null);
        disposable.clear();
    }

    public void menuButtonClicked(View view) {
        Intent menuIntent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(menuIntent);
    }


    public void loadPhotos() {
        if(Profile.getCurrentProfile() != null) {
            String pageId = Profile.getCurrentProfile().getId();
            String feedFields = "id,from,link,object_id,message,type,name,story,created_time,updated_time";
            subscribeToFeed(feedFetchHelper.pageFeedService.getPageFeed(pageId, feedFields, accessToken));

        }
    }

    public void loadNextPage( ) {
        if(nextPageLink != null) {

            subscribeToFeed(feedFetchHelper.paginationService.getPage(nextPageLink.trim()));

        }

    }

    public void subscribeToFeed(Observable<Feed> observable) {
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<Feed, Observable<Feed.FeedItem>>() {
                    @Override
                    public Observable<Feed.FeedItem> apply(Feed feed) throws Exception {
                        nextPageLink = feed.paging.next;
                        loadingNextPage = false;
                        return Observable.fromIterable(feed.data);
                    }
                })
                .filter(new Predicate<Feed.FeedItem>() {
                    @Override
                    public boolean test(Feed.FeedItem feedItem) throws Exception {
                        return feedItem.type.equals("photo") ;
                    }
                })
                .subscribe(new Observer<Feed.FeedItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v("COBALT", "Page Feed onSubscribe");
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(Feed.FeedItem value) {
                        Log.v("COBALT", "Page Feed onNext");
                        Log.v("COBALT", "Feed: " + value);

                        updatePhotoList(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("COBALT", "Page Feed onError: " + e.getLocalizedMessage());

                    }

                    @Override
                    public void onComplete() {
                        Log.v("COBALT", "Page Feed onComplete");

                    }
                });

    }

    public void updatePhotoList(Feed.FeedItem newFeedItem) {
        photoList.add(newFeedItem);
        mAdapter.notifyDataSetChanged();
    }

    public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {
        private ArrayList<Feed.FeedItem> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            ImageView mPostImage;
            CompositeDisposable disposable = new CompositeDisposable();
            ViewHolder(CardView v) {
                super(v);
                mPostImage = (ImageView) v.findViewById(R.id.photolist_image);

            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public PhotoListAdapter(ArrayList<Feed.FeedItem> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public PhotoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                              int viewType) {
            // create a new view
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_photolist, parent, false);
            // set the view's size, margins, paddings and layout parameters
            PhotoListAdapter.ViewHolder vh = new PhotoListAdapter.ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final PhotoListAdapter.ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            Feed.FeedItem item = mDataset.get(position);

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

        }

        @Override
        public void onViewRecycled(PhotoListAdapter.ViewHolder holder) {
            super.onViewRecycled(holder);
            holder.mPostImage.setImageDrawable(null);
            holder.disposable.clear();
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

    }
}
