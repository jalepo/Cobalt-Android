package com.jalepo.cobalt;

import com.facebook.Profile;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jalepo on 2/2/17.
 */

public class Videos {
    public Videos(){}
    ArrayList<Video> data = new ArrayList<>();


    public class Video {
        public Video(){}
        public String id;
        public Profile from;
        public String permalink_url;
        public String description;
        public Date created_time;
        public Date modified_time;
        public Thumbnails thumbnails;

        public class Thumbnails {
            Thumbnails(){}
            ArrayList<Thumbnail> data = new ArrayList<>();


            public class Thumbnail {
                Thumbnail(){}
                public int height;
                public int width;
                public String id;
                public int scale;
                public String uri;
            }
        }
    }
}
