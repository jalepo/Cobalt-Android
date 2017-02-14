package com.jalepo.cobalt;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Retrofit;

import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;


public class FeedFetchHelper {


    String baseUrl = "https://graph.facebook.com/v2.8/";
    String feedFields = "id,from,link,object_id,message,type,name,story,created_time,updated_time";
    String photoFields = "id,from,images,link,name,created_time,updated_time";
    String videoFields = "id,from,thumbnails,permalink_url,title,description,created_time,updated_time";
    String userFields = "id,name,cover,about,link";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

    PageFeedService pageFeedService = retrofit.create(PageFeedService.class);
    PhotoDataService photoDataService = retrofit.create(PhotoDataService.class);
    VideoDataService videoDataService = retrofit.create(VideoDataService.class);
    FriendsDataService friendsDataService = retrofit.create(FriendsDataService.class);

    FeedPaginationService paginationService = retrofit.create(FeedPaginationService.class);




    public interface PageFeedService {
        @GET("{page_id}/feed")
        Observable<Feed> getPageFeed(@Path("page_id") String pageId,
                                     @Query("fields") String fields,
                                     @Query("access_token") String accessToken);
    }

    public interface PhotoDataService {
        @GET("{photo_id}")
        Single<Photos.Photo> getPhotos(@Path("photo_id") String photoId,
                                       @Query("fields") String fields,
                                       @Query("access_token") String accessToken);
    }

    public interface VideoDataService {
        @GET("{video_id}")
        Single<Videos.Video> getVideos(@Path("video_id") String videoId,
                                       @Query("fields") String fields,
                                       @Query("access_token") String accessToken);
    }


    public interface FriendsDataService {
        @GET("{user_id}/friends")
        Observable<Users> getFriends(@Path("user_id") String userId,
                                     @Query("fields") String fields,
                                     @Query("access_token") String accessToken);
    }

    public interface FeedPaginationService {
        @GET
        Observable<Feed> getPage(@Url String url);
    }
}
