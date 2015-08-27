package com.endurata.spotracer.DataStructs;

/**
 * Created by dfreadhoff on 8/25/2015.
 */
public class FollowAthleteStruct {
    public String getAthleteId() {
        return mAthleteId;
    }

    // 0e66bb55-cd24-4835-a0e9-9e1d28bb7256,David,Erickson,Naperville,Il,USA,0;
    private String mAthleteId;

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    private String mFirstName;
    private String mLastName;
    private String mCity;
    private String mState;
    private String mCoutry;
    private String mIsFollowing;

    public FollowAthleteStruct(String WsString) {
        String temp[] = WsString.split(",");
        mAthleteId = temp[0];
        mFirstName = temp[1];
        mLastName = temp[2];
        mCity = temp[3];
        mState = temp[4];
        mCoutry = temp[5];
        mIsFollowing = temp[6];
    }

    public String getIsFollowing() {
        return mIsFollowing;
    }

    public void setIsFollowing(String mIsFollowing) {
        this.mIsFollowing = mIsFollowing;
    }
}