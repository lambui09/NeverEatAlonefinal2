package com.quocthoaitran.NeverEatAlone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btnRegister;
    private EditText txtEmail;
    private EditText txtPassword;
    private TextView txtSingIn;
    private EditText txt_full_name;
    private EditText txt_hobby;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private Uri img;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth= FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
       // user = firebaseAuth.getCurrentUser();
        if(firebaseAuth.getCurrentUser()!=null){
            //profile activity here
            finish();
            startActivity(new Intent(getApplicationContext(), Home_page.class));
        }

        progressDialog= new ProgressDialog(this);

        btnRegister= (Button) findViewById(R.id.btnRegister);
        txtEmail= (EditText) findViewById(R.id.txtEmail);
        txtPassword= (EditText) findViewById(R.id.txtPassword);
        txtSingIn= (TextView) findViewById(R.id.txtSingIn);
        txt_full_name = (EditText) findViewById(R.id.txt_full_name);
        txt_hobby = (EditText) findViewById(R.id.txtHobby);

        btnRegister.setOnClickListener(this);
        txtSingIn.setOnClickListener(this);
    }

    private  void register(){
        final String email= txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String full_name = txt_full_name.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            //email is empty
            Toast.makeText(this, R.string.enter_email,Toast.LENGTH_SHORT).show();
            //stopping the function excution further
            return;
        }
        if(TextUtils.isEmpty(password)){
            //password is empty
            Toast.makeText(this, R.string.enter_password,Toast.LENGTH_SHORT).show();
            //stopping the function excution further
            return;
        }
        if(TextUtils.isEmpty(full_name)){
            //password is empty
            Toast.makeText(this, R.string.enter_fullname,Toast.LENGTH_SHORT).show();
            //stopping the function excution further
            return;
        }
        if(TextUtils.isEmpty(getHobby())){
            //password is empty
            Toast.makeText(this, R.string.enter_hobby,Toast.LENGTH_SHORT).show();
            //stopping the function excution further
            return;
        }
        //if validations are ok
        //we will first show a progressbar
        //progressDialog.setMessage("");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            //user is successfully registered and logged in
                            //we will start the profile activity here
                            //right now lets display a toast only
                            finish();
                            onAuthSuccess(task.getResult().getUser());
                            //startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this, R.string.not_register, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String getUserFullName(){
        return txt_full_name.getText().toString().trim();
    }
    private String getUserEmail(){
        return txtEmail.getText().toString().trim();
    }
    private String getUserPassword(){
        return txtPassword.getText().toString().trim();
    }
    private String getHobby(){
        return txt_hobby.getText().toString().trim();
    }
    private void onAuthSuccess(FirebaseUser user) {
        createNewUser(user.getUid());
        Intent intent = new Intent(MainActivity.this, Home_page.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void createNewUser(String userUid) {
        User user = new User(userUid,
                getUserFullName(),
                getUserEmail(),
                UsersChatAdapter.ONLINE,
                getHobby(),
                ChatHelper.generateRandomAvatarForUser(),
                new Date().getTime(),
                img);
        mDatabase.child("users").child(userUid).setValue(user);
    }


    @Override
    public void onClick(View v) {
        if(v== btnRegister){
            register();
        }
        if(v==txtSingIn){
            //will open login activity
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
