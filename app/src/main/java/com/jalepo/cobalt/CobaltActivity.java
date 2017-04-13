package com.jalepo.cobalt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import rx.Observer;
import rx.Subscription;

public abstract class CobaltActivity extends AppCompatActivity {
    public static final String FRIENDLIST = "friendlist";
    public static final String FEEDLIST = "feedlist";
    protected RecyclerView mRecyclerView;
    protected RecyclerView.Adapter mAdapter;

    FeedFetchHelper feedFetchHelper = new FeedFetchHelper();
    String mViewType = FEEDLIST;
    CompositeDisposable disposable = new CompositeDisposable();
    ProfileTracker mProfileTracker;
    String accessToken;
    String nextPageLink;
    boolean loadingNextPage = false;

    Subscription scrollEventSubscription;
    protected ArrayList<Feed.FeedItem> dataList = new ArrayList<>();
    protected ArrayList<Users.User> userList = new ArrayList<>();

    protected Predicate<Feed.FeedItem> feedFilter = new Predicate<Feed.FeedItem>() {
        @Override
        public boolean test(Feed.FeedItem feedItem) throws Exception {
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                loadFirstPage();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();



        scrollEventSubscription = RxRecyclerView.scrollEvents(mRecyclerView)
                .subscribe(new Observer<RecyclerViewScrollEvent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(RecyclerViewScrollEvent recyclerViewScrollEvent) {

                        if(!loadingNextPage && !mRecyclerView.canScrollVertically(1)) {
                            loadNextPage();
                            loadingNextPage = true;
                        }
                    }
                });
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if(!loadingNextPage && !recyclerView.canScrollVertically(1)) {
//                    loadNextPage();
//                    loadingNextPage = true;
//                }
//            }
//        });
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mRecyclerView.removeOnScrollListener(null);
        scrollEventSubscription.unsubscribe();
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
            loadFirstPage();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProfileTracker != null) {
            mProfileTracker.stopTracking();
        }

        disposable.clear();
    }

    public void menuButtonClicked(View view) {
        Intent menuIntent = new Intent(getApplicationContext(), MenuActivity.class);
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(menuIntent);
    }

    public void loadFirstPage() {
        dataList.clear();
        userList.clear();
        mAdapter.notifyDataSetChanged();

        if(Profile.getCurrentProfile() != null) {
            String pageId = Profile.getCurrentProfile().getId();
            if(mViewType.equals(FEEDLIST)) {
                subscribeToFeed(feedFetchHelper.pageFeedService.getPageFeed(pageId,
                        feedFetchHelper.feedFields, accessToken));
            }
            if(mViewType.equals(FRIENDLIST)) {
                subscribeToFriends(feedFetchHelper.friendsDataService.getFriends(pageId,
                        feedFetchHelper.userFields, accessToken));
            }
        }
    }



    public void loadNextPage() {
        if(nextPageLink != null) {

            subscribeToFeed(feedFetchHelper.paginationService.getPage(nextPageLink.trim()));

        }
    }

    public void subscribeToFeed(Observable<Feed> feedObservable) {
        feedObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<Feed, Observable<Feed.FeedItem>>() {
                    @Override
                    public Observable<Feed.FeedItem> apply(Feed feed) throws Exception {
                        nextPageLink = feed.paging.next;
                        loadingNextPage = false;
                        return Observable.fromIterable(feed.data);
                    }
                })
                .filter(feedFilter)
                .subscribe(new io.reactivex.Observer<Feed.FeedItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v("COBALT", "Page Feed onSubscribe");
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(Feed.FeedItem value) {
                        Log.v("COBALT", "Page Feed onNext");
                        updateDataList(value);
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

    public void subscribeToFriends(Observable<Users> friendsObservable) {
        friendsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<Users, Observable<Users.User>>() {
                    @Override
                    public Observable<Users.User> apply(Users users) throws Exception {
                        nextPageLink = users.paging.next;
                        loadingNextPage = false;
                        return Observable.fromIterable(users.data);
                    }
                })
                //.filter(feedFilter)
                .subscribe(new io.reactivex.Observer<Users.User>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v("COBALT", "Friends onSubscribe");
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(Users.User value) {
                        Log.v("COBALT", "Friends onNext");
                        updateFriendsList(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("COBALT", "Friends onError: " + e.getLocalizedMessage());

                    }

                    @Override
                    public void onComplete() {
                        Log.v("COBALT", "Friends onComplete");

                    }
                });
    }

    public void updateDataList(Feed.FeedItem newFeedItem) {
        dataList.add(newFeedItem);
        mAdapter.notifyItemInserted(dataList.size() - 1);
    }

    public void updateFriendsList(Users.User newUser) {
        userList.add(newUser);
        mAdapter.notifyItemInserted(userList.size() - 1);
    }

    public void getRemoteImage(final ImageView imageView,
                               String objectId,
                               final CompositeDisposable disposable) {

        feedFetchHelper.photoDataService.getPhotos(objectId,
                feedFetchHelper.photoFields, accessToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Photos.Photo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(Photos.Photo value) {
                        if(value.images != null) {
                            String url = value.images.get(0).source;
                            Picasso.with(getApplicationContext())
                                    .load(url)
                                    .into(imageView);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });


    }

    public void getRemoteVideo(final ImageView imageView,
                               String objectId,
                               final CompositeDisposable disposable,
                               final boolean preview) {
        feedFetchHelper.videoDataService.getVideos(objectId,
                feedFetchHelper.videoFields, accessToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Videos.Video>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(Videos.Video value) {
                        if(value.thumbnails != null) {
                            String url = value.thumbnails.data.get(0).uri;
                            Picasso.with(getApplicationContext())
                                    .load(url)
                                    .into(imageView);
                            if(preview) {
                                cycleThumbnails(imageView, value, disposable);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public void cycleThumbnails(final ImageView imageView,
                                final Videos.Video video,
                                final CompositeDisposable disposable) {
        // Fetch the thumbnails into Picasso's cache, for smoother cycling
        for(Videos.Video.Thumbnails.Thumbnail thumb: video.thumbnails.data) {
            Picasso.with(getApplicationContext()).load(thumb.uri).fetch();
        }

        Observable.interval(1, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.Observer<Long>() {
                    int displayIndex = 0;
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
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
                                .into(imageView);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public String getCreatedDate(Date date) {

        return date.toString();
    }
}
