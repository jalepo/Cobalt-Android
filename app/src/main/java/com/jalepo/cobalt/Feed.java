package com.jalepo.cobalt;

import com.facebook.Profile;

import java.util.ArrayList;

/**
 * Created by jalepo on 1/30/17.
 */

public class Feed {
    public Feed(){}
    public ArrayList<FeedItem> data = new ArrayList<>();


    public class FeedItem {
        public FeedItem(){}
        public Profile from;
        public String message;
        public String story;
    }

}
