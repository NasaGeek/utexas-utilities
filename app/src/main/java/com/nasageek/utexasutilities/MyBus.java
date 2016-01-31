package com.nasageek.utexasutilities;

import com.squareup.otto.Bus;

/**
 * Lifted from http://simonvt.net/2014/04/17/asynctask-is-bad-and-you-should-feel-bad/
 */
public class MyBus {

    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }
}
