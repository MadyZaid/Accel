package com.nike.accel.data.web;

/**
 * Used to communicate back to the client (the main activity) when the connection to the
 * server (PubNub) changes, which could be a result of a wifi connection change or
 * possibly the server not being available.
 */
public interface IWebAccess {
    void onConnectionChange(boolean connected);
}
