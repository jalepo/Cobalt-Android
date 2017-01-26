package com.jalepo.cobalt;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;

import java.util.concurrent.Callable;
import java.util.zip.Inflater;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class HeaderFragment extends Fragment {

    TextView mUserName;
    ProfileTracker mProfileTracker;
//    private final CompositeDisposable disposables = new CompositeDisposable();
//
//
//    Single<String> userNameSingle = Single.fromCallable(new Callable<String>() {
//        @Override
//        public String call() throws Exception {
//
//            return Profile.getCurrentProfile().getName();
//        }
//    });



    public HeaderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView =  inflater.inflate(R.layout.fragment_header, container, false);

        mUserName = (TextView) fragmentView.findViewById(R.id.header_username);
        assert mUserName != null;

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile old, Profile current) {
                setHeaderUserName();
            }
        };
        setHeaderUserName();
//        userNameSingle
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SingleObserver<String>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//                disposables.add(d);
//            }
//
//            @Override
//            public void onSuccess(String value) {
//                assert mUserName != null;
//                mUserName.setText(Profile.getCurrentProfile().getName());
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//        });
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        disposables.clear();
        if (mProfileTracker != null) {
            mProfileTracker.stopTracking();
        }
    }

    public void setHeaderUserName() {
        if(Profile.getCurrentProfile() != null) {
            assert mUserName != null;
            mUserName.setText(Profile.getCurrentProfile().getName());

        }
    }
}
