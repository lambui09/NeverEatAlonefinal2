package com.quocthoaitran.demoversion3;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnSignIn;
    protected TextView txtSignUp;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail= (EditText) findViewById(R.id.txtEmail);
        txtPassword= (EditText) findViewById(R.id.txtPassword);
        txtSignUp= (TextView) findViewById(R.id.txtSingUp);
        btnSignIn= (Button) findViewById(R.id.btnSignIn);

        progressDialog= new ProgressDialog(this);
        firebaseAuth= FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null){
            //profile activity here
            finish();
            startActivity(new Intent(this, Home_page.class));
        }

        btnSignIn.setOnClickListener(this);
        txtSignUp.setOnClickListener(this);
    }

    private void userLognin(){
        String email= txtEmail.getText().toString().trim();
        String password= txtPassword.getText().toString().trim();

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
        //if validations are ok
        //we will first show a progressbar
        progressDialog.setMessage("");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if(task.isSuccessful()){
                            //start profile activity
                            finish();
                            startActivity(new Intent(getApplicationContext(), Home_page.class));
                        }
                    }
                });
    }
    @Override
    public void onClick(View v) {
        if(v==btnSignIn){
            userLognin();
        }
        if(v==txtSignUp){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

    }
}
