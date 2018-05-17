package com.quocthoaitran.demoversion3;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btnRegister;
    private EditText txtEmail;
    private EditText txtPassword;
    private TextView txtSingIn;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth= FirebaseAuth.getInstance();
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

        btnRegister.setOnClickListener(this);
        txtSingIn.setOnClickListener(this);
    }

    private  void register(){
        String email= txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

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
        progressDialog.setMessage(""+R.string.registering);
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
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this, R.string.not_register, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
