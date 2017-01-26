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

import java.util.zip.Inflater;


/**
 * A simple {@link Fragment} subclass.
 */
public class HeaderFragment extends Fragment {

    TextView mUserName;

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


        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        assert mUserName != null;
        mUserName.setText(Profile.getCurrentProfile().getName());
    }
}
