package com.jalepo.cobalt;

import com.facebook.Profile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jalepo on 1/30/17.
 */

public class Feed {
    public Feed(){}
    public ArrayList<FeedItem> data = new ArrayList<>();
    public Paging paging;

    public class FeedItem {
        public FeedItem(){}
        public String id;
        public String object_id;
        public String link;
        public String type;
        public Profile from;
        public String message;
        public String story;
        public Date created_time;
        public Date updated_time;
    }

    public class Paging {
        public Paging(){}
        public String previous;
        public String next;
    }

}
