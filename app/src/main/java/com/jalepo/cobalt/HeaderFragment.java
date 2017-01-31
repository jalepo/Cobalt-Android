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


    public HeaderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
