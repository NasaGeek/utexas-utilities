
package com.nasageek.utexasutilities.model.canvas;

import java.io.Serializable;
import java.util.ArrayList;

public class Module implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    public String name;
    private int position;
    public int items_count;
    private String items_url;
    public java.util.List<ModuleItem> items;

    public static class List extends ArrayList<Module> {

        private static final long serialVersionUID = 1L;
    }

}
