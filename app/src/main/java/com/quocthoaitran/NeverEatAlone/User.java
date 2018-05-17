package com.quocthoaitran.NeverEatAlone;


import android.net.Uri;

/**
 * @author Marcelino Yax-marce7j@gmail.com-Android Developer
 *         Created on 12/23/2016.
 */

public class User {

    private String UserUid;
    private String displayName;
    private String email;
    private String connection;
    private String hobby;
    private int avatarId;
    private long createdAt;
    private Uri img_avatar;

    private String mRecipientId;

    public User(Uri img_avatar) {
        this.img_avatar = img_avatar;
    }

    public Uri getImg_avatar() {

        return img_avatar;
    }

    public void setImg_avatar(Uri img_avatar) {
        this.img_avatar = img_avatar;
    }

    public User() {
    }

    public String getUserUid() {
        return UserUid;
    }

    public void setUserUid(String userUid) {
        UserUid = userUid;
    }

    public User(String userUid, String displayName, String email, String connection, String hobby, int avatarId, long createdAt, Uri img) {
        this.UserUid = userUid;
        this.displayName = displayName;
        this.email = email;
        this.connection = connection;
        this.hobby = hobby;
        this.avatarId = avatarId;
        this.createdAt = createdAt;
        this.img_avatar = img;
    }


    public String createUniqueChatRef(long createdAtCurrentUser, String currentUserEmail){
        String uniqueChatRef="";
        if(createdAtCurrentUser > getCreatedAt()){
            uniqueChatRef = cleanEmailAddress(currentUserEmail)+"-"+cleanEmailAddress(getUserEmail());
        }else {

            uniqueChatRef=cleanEmailAddress(getUserEmail())+"-"+cleanEmailAddress(currentUserEmail);
        }
        return uniqueChatRef;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    private String cleanEmailAddress(String email){
        //replace dot with comma since firebase does not allow dot
        return email.replace(".","-");
    }

    private String getUserEmail() {
        //Log.e("user email  ", userEmail);
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getConnection() {
        return connection;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public String getRecipientId() {
        return mRecipientId;
    }

    public String getHobby() {
        return hobby;
    }

    public void setRecipientId(String recipientId) {
        this.mRecipientId = recipientId;

    }
}
