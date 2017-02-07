package com.jalepo.cobalt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.Profile;
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

public class VideoListActivity extends CobaltActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.video_list_view);

        mRecyclerView.setHasFixedSize(true);
        ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(3);
        ((GridLayoutManager) mRecyclerView.getLayoutManager()).setOrientation(GridLayoutManager.VERTICAL);


        mAdapter = new VideoListAdapter(dataList);
        mRecyclerView.setAdapter(mAdapter);

        feedFilter = new Predicate<Feed.FeedItem>() {
            @Override
            public boolean test(Feed.FeedItem feedItem) throws Exception {
                return feedItem.type.equals("video") && feedItem.object_id != null;
            }
        };
    }


    public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {
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
        public VideoListAdapter(ArrayList<Feed.FeedItem> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public VideoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_photolist, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Feed.FeedItem item = mDataset.get(position);
            if(item.type.equals("video")) {
                final String videoId = item.object_id;
                getRemoteVideo(holder.mPostImage, videoId, holder.disposable);
            }
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
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
