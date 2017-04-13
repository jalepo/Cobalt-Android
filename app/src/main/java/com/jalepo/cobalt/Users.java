package com.jalepo.cobalt;

import java.util.ArrayList;

/**
 * Created by jalepo on 2/11/17.
 */

public class Users {
    public Users(){}
    ArrayList<User> data = new ArrayList<>();
    Paging paging;

    public class User {
        public User(){}
        String id;
        String about;
        String name;
        String link;
        CoverPhoto cover;

        class CoverPhoto {
            CoverPhoto(){}
            String id;
        }

    }

    public class Paging {
        public Paging(){}
        public String previous;
        public String next;
    }
}
