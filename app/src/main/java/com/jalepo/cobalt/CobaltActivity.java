package com.jalepo.cobalt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import rx.Observer;
import rx.Subscription;

public abstract class CobaltActivity extends AppCompatActivity {
    protected RecyclerView mRecyclerView;
    protected RecyclerView.Adapter mAdapter;

    FeedFetchHelper feedFetchHelper = new FeedFetchHelper();

    CompositeDisposable disposable = new CompositeDisposable();
    ProfileTracker mProfileTracker;
    String accessToken;
    String nextPageLink;
    boolean loadingNextPage = false;

    Subscription scrollEventSubscription;
    protected ArrayList<Feed.FeedItem> dataList = new ArrayList<>();

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
        startActivity(menuIntent);
    }

    public void loadFirstPage() {
        if(Profile.getCurrentProfile() != null) {
            String pageId = Profile.getCurrentProfile().getId();
            subscribeToFeed(feedFetchHelper.pageFeedService.getPageFeed(pageId,
                    feedFetchHelper.feedFields, accessToken));

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

    public void updateDataList(Feed.FeedItem newFeedItem) {
        dataList.add(newFeedItem);
        mAdapter.notifyItemInserted(dataList.size() - 1);
    }

}
