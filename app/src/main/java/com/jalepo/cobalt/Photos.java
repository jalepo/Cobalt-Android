package com.jalepo.cobalt;

import java.util.ArrayList;

/**
 * Created by jalepo on 2/1/17.
 */

public class Photos {
    public Photos(){}
    ArrayList<Photo> data = new ArrayList<>();

    public class Photo {
        public Photo(){}
        public String id;
        public ArrayList<Image> images = new ArrayList<>();

        public class Image {
            public Image(){}

            public int height;
            public int width;
            public String source;
        }
    }
}
