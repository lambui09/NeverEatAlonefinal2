package com.quocthoaitran.NeverEatAlone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marcel on 11/11/2015.
 */
public class UsersChatAdapter extends RecyclerView.Adapter<UsersChatAdapter.ViewHolderUsers> {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    private List<User> mUsers;
    private Context mContext;
    private String mCurrentUserEmail;
    private Long mCurrentUserCreatedAt;
    private String mCurrentUserId;
    private Object img;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mUserRefDatabase;
    private FirebaseUser user;

    public UsersChatAdapter(Context context, List<User> fireChatUsers) {
        mUsers = fireChatUsers;
        mContext = context;
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(mContext, LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolderUsers holder, int position) {

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        User fireChatUser = mUsers.get(position);
        String uid = fireChatUser.getUserUid();
        int userAvatarId= ChatHelper.getDrawableAvatarId(fireChatUser.getAvatarId());
        final Drawable avatarDrawable = ContextCompat.getDrawable(mContext,userAvatarId);

        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("uri_avatar");
        mUserRefDatabase.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               String s = String.valueOf(dataSnapshot.getValue(URI.class));
               if(s == "null"){
                   holder.getUserAvatar().setImageResource(R.drawable.profile);
               }else{
                   URI img = dataSnapshot.getValue(URI.class);
                   Picasso.with(mContext).load(String.valueOf(img)).into(holder.getUserAvatar());
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Set avatar

       // holder.getUserAvatar().setImageDrawable(avatarDrawable);

        // Set display name
        holder.getUserDisplayName().setText(fireChatUser.getDisplayName());

        // Set presence status
        holder.getStatusConnection().setText(fireChatUser.getConnection());

        holder.getHobby().setText(fireChatUser.getHobby());

        // Set presence text color
        if(fireChatUser.getConnection().equals(ONLINE)) {
            // Green color
            holder.getStatusConnection().setTextColor(Color.parseColor("#00FF00"));
        }else {
            // Red color
            holder.getStatusConnection().setTextColor(Color.parseColor("#FF0000"));
        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void refill(User users) {
        mUsers.add(users);
        notifyDataSetChanged();
    }

    public void changeUser(int index, User user) {
        mUsers.set(index,user);
        notifyDataSetChanged();
    }
    public void setUser(User user){
        mUsers.remove(user);
        mUsers.add(0,user);
        notifyDataSetChanged();
    }

    public void setCurrentUserInfo(String userUid, String email, long createdAt) {
        mCurrentUserId = userUid;
        mCurrentUserEmail = email;
        mCurrentUserCreatedAt = createdAt;
    }

    public void clear() {
        mUsers.clear();
    }


    /* ViewHolder for RecyclerView */
    public class ViewHolderUsers extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView mUserAvatar;
        private TextView mUserDisplayName;
        private TextView mStatusConnection;
        private Context mContextViewHolder;
        private TextView mHobby;

        public ViewHolderUsers(Context context, View itemView) {
            super(itemView);
            mUserAvatar = (ImageView)itemView.findViewById(R.id.img_avatar);
            mUserDisplayName = (TextView)itemView.findViewById(R.id.text_view_display_name);
            mStatusConnection = (TextView)itemView.findViewById(R.id.text_view_connection_status);
            mContextViewHolder = context;
            mHobby = (TextView) itemView.findViewById(R.id.text_view_hobby);

            itemView.setOnClickListener(this);
        }

        public ImageView getUserAvatar() {
            return mUserAvatar;
        }

        public TextView getUserDisplayName() {
            return mUserDisplayName;
        }
        public TextView getStatusConnection() {
            return mStatusConnection;
        }
        public TextView getHobby(){
            return mHobby;
        }

        @Override
        public void onClick(View view) {

            User user = mUsers.get(getLayoutPosition());

            String chatRef = user.createUniqueChatRef(mCurrentUserCreatedAt,mCurrentUserEmail);

            Intent chatIntent = new Intent(mContextViewHolder, ChatActivity.class);
            chatIntent.putExtra(ExtraIntent.EXTRA_CURRENT_USER_ID, mCurrentUserId);
            chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_ID, user.getRecipientId());
            chatIntent.putExtra("name", user.getDisplayName());
            chatIntent.putExtra(ExtraIntent.EXTRA_CHAT_REF, chatRef);
            mContextViewHolder.startActivity(chatIntent);

        }
    }

}
