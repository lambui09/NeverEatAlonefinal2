package com.quocthoaitran.demoversion3;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth firebaseAuth;
    private Button btnHuy, btnLuu;
    private EditText txtName;
    private FirebaseUser user;
    private ImageView img_avatar;
    int REQUES_CODE_CAMERA =1;
    int PICK_IMAGE = 1;
    Uri imageUrl;
    private Uri img;
    int a;
    FirebaseStorage storage= FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://appnevereatalone.appspot.com");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth= FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }


        user= firebaseAuth.getCurrentUser();

        btnHuy= (Button) findViewById(R.id.btnHuy);
        btnLuu= (Button) findViewById(R.id.btnLuu);
        txtName= (EditText) findViewById(R.id.txtName);
        img_avatar= (ImageView) findViewById(R.id.img_avatar);
        img_avatar.setOnClickListener(this);
        txtName.setText(user.getDisplayName());
        Picasso.with(this).load(user.getPhotoUrl()).into(img_avatar);
        btnLuu.setOnClickListener(this);
        btnHuy.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v == img_avatar){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.avatar);
            alertDialogBuilder.setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a=1;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUES_CODE_CAMERA);
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.thu_vien, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a=2;
                    Intent getintent = new Intent(Intent.ACTION_GET_CONTENT);
                    getintent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getintent, "Seclect Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE);

                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        if(v== btnHuy) {
            finish();
            startActivity(new Intent(this, Home_page.class));
        }
        if(v == btnLuu){
            //Lưu dữ liệu
            Calendar calendar = Calendar.getInstance();
            StorageReference mountainsRef = storageRef.child("image" + calendar.getTimeInMillis() + ".png");
            img_avatar.setDrawingCacheEnabled(true);
            img_avatar.buildDrawingCache();
            Bitmap bitmap = img_avatar.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            final String name= txtName.getText().toString();

            UploadTask uploadTask = mountainsRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(ProfileActivity.this, "Lỗi!!!", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                   img = taskSnapshot.getDownloadUrl();
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(img)
                            .build();
                    user.updateProfile(profileChangeRequest)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                    }
                                }
                            });
                }
            });

            finish();
            startActivity(new Intent(this, Home_page.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(a==1){
            if(requestCode == REQUES_CODE_CAMERA && resultCode == RESULT_OK && data != null){
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                img_avatar.setImageBitmap(bitmap);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }else{
            ProfileActivity.super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_IMAGE) {
                imageUrl = data.getData();
                img_avatar.setImageURI(imageUrl);
                Toast.makeText(this, imageUrl + "", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
