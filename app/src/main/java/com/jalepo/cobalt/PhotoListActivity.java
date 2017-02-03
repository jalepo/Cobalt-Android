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

public class PhotoListActivity extends CobaltActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.photo_list_view);

        mRecyclerView.setHasFixedSize(true);

        ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(3);
        ((GridLayoutManager) mRecyclerView.getLayoutManager()).setOrientation(GridLayoutManager.VERTICAL);

        mAdapter = new PhotoListActivity.PhotoListAdapter(dataList);
        mRecyclerView.setAdapter(mAdapter);

        feedFilter = new Predicate<Feed.FeedItem>() {
            @Override
            public boolean test(Feed.FeedItem feedItem) throws Exception {
                return feedItem.type.equals("photo")  && feedItem.object_id != null;
            }
        };

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
