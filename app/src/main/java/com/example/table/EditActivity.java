package com.example.table;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.table.utils.MyConstants;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class EditActivity extends AppCompatActivity {

    FloatingActionButton sendBtn;
    private StorageReference storageRef;
    private ImageView ivItem;
    private Uri uploadUri;
    private Spinner spinner;
    private DatabaseReference dRef;
    private FirebaseAuth mAuth;
    private EditText edTitle, edPrice, edTel, edDisc;
    private boolean editState = false;
    private String temp_cat = "";
    private String temp_uid = "";
    private String temp_time = "";
    private String temp_key = "";
    private String temp_image_url = "";
    private boolean is_image_update = false;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        init();
    }

    private void init() {
        edTitle = findViewById(R.id.edTitle);
        edTel = findViewById(R.id.edPhone);
        edPrice = findViewById(R.id.edPrice);
        edDisc = findViewById(R.id.edDisc);
        sendBtn = findViewById(R.id.floatingActionButton);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        ivItem = findViewById(R.id.ivItem);
        storageRef = FirebaseStorage.getInstance().getReference("Images");

        sendBtn.setOnClickListener(v -> {
            if(!editState) {
                uploadImage();
            } else {
                if(is_image_update) {
                    uploadUpdateImage();
                } else {
                    updatePost();
                }
            }
            finish();
        });
        getMyIntent();
    }

    private void getMyIntent() {
        if(getIntent() != null) {
            Intent i = getIntent();
            editState = i.getBooleanExtra(MyConstants.EDIT_STATE, false);
            if(editState)setDataAds(i);
        }
    }

    private void setDataAds(Intent i) {
        Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(ivItem);
        edTel.setText(i.getStringExtra(MyConstants.TEL));
        edTitle.setText(i.getStringExtra(MyConstants.TITLE));
        edPrice.setText(i.getStringExtra(MyConstants.PRICE));
        edDisc.setText(i.getStringExtra(MyConstants.DISC));

        temp_cat = i.getStringExtra(MyConstants.CAT);
        temp_uid = i.getStringExtra(MyConstants.UID);
        temp_time = i.getStringExtra(MyConstants.TIME);
        temp_key = i.getStringExtra(MyConstants.KEY);
        temp_image_url = i.getStringExtra(MyConstants.IMAGE_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==10 && data!=null && data.getData()!=null) {
            if (resultCode==RESULT_OK) {
                ivItem.setImageURI(data.getData());
                is_image_update = true;
            }
        }
    }

    public void onClickImage(View view) {
        getImage();
    }

    private void uploadImage() {
        Bitmap bitmap = ((BitmapDrawable)ivItem.getDrawable()).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mRef = storageRef.child(System.currentTimeMillis() + "_image");
        UploadTask up = mRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                savePost();
                Toast.makeText(EditActivity.this, "Upload done.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 10);
    }

    private void savePost() {

        dRef = FirebaseDatabase.getInstance().getReference(spinner.getSelectedItem().toString());
        String key = dRef.push().getKey();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getUid() != null) {
            NewPost post = new NewPost();
            post.setImageId(uploadUri.toString());
            post.setTitle(edTitle.getText().toString());
            post.setTel(edTel.getText().toString());
            post.setPrice(edPrice.getText().toString());
            post.setDisc(edDisc.getText().toString());
            post.setKey(key);
            post.setCat(spinner.getSelectedItem().toString());
            post.setTime(String.valueOf(System.nanoTime()));
            post.setUid(mAuth.getUid());

            Log.d("Проверка", ""+post);

            if(key != null)dRef.child(key).child("ad").setValue(post);
        }
    }

    private void updatePost() {

        dRef = FirebaseDatabase.getInstance().getReference(temp_cat);
        mAuth = FirebaseAuth.getInstance();

        NewPost post = new NewPost();
        post.setImageId(temp_image_url);
        post.setTitle(edTitle.getText().toString());
        post.setTel(edTel.getText().toString());
        post.setPrice(edPrice.getText().toString());
        post.setDisc(edDisc.getText().toString());
        post.setKey(temp_key);
        post.setCat(temp_cat);
        post.setTime(temp_time);
        post.setUid(temp_uid);

        Log.d("Проверка", ""+post);
        dRef.child(temp_key).child("ad").setValue(post);
    }

    private void uploadUpdateImage() {
        Bitmap bitmap = ((BitmapDrawable)ivItem.getDrawable()).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mRef = FirebaseStorage.getInstance().getReferenceFromUrl(temp_image_url);
        UploadTask up = mRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                assert uploadUri != null;
                temp_image_url = uploadUri.toString();
                updatePost();
                Toast.makeText(EditActivity.this, "Upload done.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}
