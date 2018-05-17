package com.quocthoaitran.NeverEatAlone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends Activity {


    private static final String TAG = ChatActivity.class.getSimpleName();

    @BindView(R.id.recycler_view_chat)
    RecyclerView mChatRecyclerView;
    @BindView(R.id.edit_text_message)
    EditText mUserMessageChatText;
    private TextView txt_name;
    private DatabaseReference mUserRefDatabase;


    private String mRecipientId;
    private String mCurrentUserId;
    private MessageChatAdapter messageChatAdapter;
    private DatabaseReference messageChatDatabase;
    private ChildEventListener messageChatListener;
    private FirebaseAuth firebaseAuth;
    private Context context;
    private CircleImageView imgAvatar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        context = this;

        bindButterKnife();
        setDatabaseInstance();
        setUsersId();
        setChatRecyclerView();
        setAvatar();
        getname();
        //startService(new Intent(this, FirebaseNotificationService.class));
    }

    private void setAvatar() {
        imgAvatar = (CircleImageView) findViewById(R.id.name_avatar);
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mRecipientId).child("uri_avatar");
        mUserRefDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String s = String.valueOf(dataSnapshot.getValue(URI.class));
                if(s == "null"){
                    imgAvatar.setImageResource(R.drawable.profile);
                }else{
                    URI img = dataSnapshot.getValue(URI.class);
                    Picasso.with(context).load(String.valueOf(img)).into(imgAvatar);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void getname() {
        txt_name = (TextView) findViewById(R.id.txt_name);
        String name = getIntent().getStringExtra("name");
        txt_name.setText(name);
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }
    private void setDatabaseInstance() {
        String chatRef = getIntent().getStringExtra(ExtraIntent.EXTRA_CHAT_REF);
        messageChatDatabase = FirebaseDatabase.getInstance().getReference().child(chatRef);
    }

    private void setUsersId() {
        mRecipientId = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_ID);
        mCurrentUserId = getIntent().getStringExtra(ExtraIntent.EXTRA_CURRENT_USER_ID);
    }

    private void setChatRecyclerView() {
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatRecyclerView.setHasFixedSize(true);
        messageChatAdapter = new MessageChatAdapter(new ArrayList<ChatMessage>());
        mChatRecyclerView.setAdapter(messageChatAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        messageChatListener = messageChatDatabase.limitToFirst(20).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {

                if(dataSnapshot.exists()){
                    ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                    if(newMessage.getSender().equals(mCurrentUserId)){
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.SENDER);
                    }else{
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.RECIPIENT);
                    }
                    messageChatAdapter.refillAdapter(newMessage);
                    mChatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount()-1);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();

        if(messageChatListener != null) {
            messageChatDatabase.removeEventListener(messageChatListener);
        }
        messageChatAdapter.cleanUp();

    }

    @OnClick(R.id.btn_send_message)
    public void btnSendMsgListener(View sendButton){

        String senderMessage = mUserMessageChatText.getText().toString().trim();

        if(!senderMessage.isEmpty()){

            ChatMessage newMessage = new ChatMessage(senderMessage,mCurrentUserId,mRecipientId);
            messageChatDatabase.push().setValue(newMessage);

            mUserMessageChatText.setText("");
            sendNotificationToUser(mRecipientId);
        }
    }

    private void sendNotificationToUser(String mRecipientId) {
        Utilities.sendNotification(this,
                mRecipientId,
                "A new message from " + firebaseAuth.getCurrentUser().getDisplayName(),
                "New message",
                "new_message"
        );
    }

}
