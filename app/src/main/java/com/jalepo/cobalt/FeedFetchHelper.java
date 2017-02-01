package com.jalepo.cobalt;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Retrofit;

import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;



public class FeedFetchHelper {

    ListActivity ownerActivity;
    String pageId;
    String accessToken;
    FeedFetchHelper(ListActivity activity) {
        ownerActivity = activity;
    }




    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://graph.facebook.com/v2.8/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

    PageFeedService pageFeedService = retrofit.create(PageFeedService.class);


//    Observable<Feed.FeedItem> fbFeedItemObservable = Observable.fromArray(new ArrayList<Feed.FeedItem>());

    public void getPageFeed(String page_id, String access_token) {
        pageId = page_id;
        accessToken = access_token;

        pageFeedService.getPageFeed(pageId, accessToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<Feed, Observable<Feed.FeedItem>>() {
                    @Override
                    public Observable<Feed.FeedItem> apply(Feed feed) throws Exception {
                        return Observable.fromIterable(feed.data);
                    }
                })
                .subscribe(new Observer<Feed.FeedItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v("COBALT", "Page Feed onSubscribe");
                    }

                    @Override
                    public void onNext(Feed.FeedItem value) {
                        Log.v("COBALT", "Page Feed onNext");
                        Log.v("COBALT", "Feed: " + value);

                        ownerActivity.updateFeedList(value);
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


    public interface PageFeedService {
        @GET("{page_id}/feed")
        Observable<Feed> getPageFeed(@Path("page_id") String pageId,
                               @Query("access_token") String accessToken);
    }


}
