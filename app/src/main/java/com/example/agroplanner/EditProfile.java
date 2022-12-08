package com.example.agroplanner;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class EditProfile extends AppCompatActivity {
    private ImageView editPImage, close;
    private TextView tx_change;
    private EditText fullname, username;
    private Button save;

    FirebaseUser firebaseUser;

    private static final String[] permissionsArray = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    Context mContext = this;
    File mPhotoFile;
    Uri photoTakenUri,mPhotoUril;

    private Uri mImageUrl;
    private StorageTask uploadTask;
    StorageReference storageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editPImage = findViewById(R.id.image_profileED);
        close = findViewById(R.id.close);
        tx_change = findViewById(R.id.tv_change);
        fullname = findViewById(R.id.fullnameE);
        username = findViewById(R.id.usernameE);
        save = findViewById(R.id.save);

        storageRef = FirebaseStorage.getInstance().getReference("uploads");


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userprofile = snapshot.getValue(User.class);
                fullname.setText(userprofile.getFullname());
                username.setText(userprofile.getUsername());
                Glide.with(getApplicationContext()).load(userprofile.getImageurl()).into(editPImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(fullname.getText().toString(),
                        username.getText().toString());
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    permissionsArray, 1);
        }

        editPImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callCropCamera();
            }
        });


    }

    private void updateProfile(String fullname, String username){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fullname", fullname);
        hashMap.put("username", username);

        reference.updateChildren(hashMap);
        refreshActivity();
        super.onBackPressed();
//        onBackPressed();
    }
    public void refreshActivity() {
        Intent i = new Intent(this, Profile.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }

    public void callCropCamera(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.lbl_set_profile_photo));
        // add a list
        String[] animals = {mContext.getString(R.string.lbl_take_camera_picture), mContext.getString(R.string.lbl_choose_from_gallery)};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        takePhoto();
                        break;
                    case 1:
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startForResultFromGallery.launch(intent);
                        break;
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    private void takePhoto() {
        String uuid = UUID.randomUUID().toString();
        File outputDir = getCacheDir();
        try {
            mPhotoFile = File.createTempFile( uuid, ".jpg", outputDir );
        }
        catch( IOException e ) {
            e.printStackTrace();
            return;
        }

        try {
            photoTakenUri = FileProvider.getUriForFile( Objects.requireNonNull(
                    getApplicationContext()),
                    "com.example.agroplanner.provider", mPhotoFile );

        }
        catch( IllegalArgumentException e ) {
            e.printStackTrace();
            return;
        }

        takeAPhoto.launch( photoTakenUri );
    }


    ActivityResultLauncher<Uri> takeAPhoto = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), result ->
            {
                if( !result  )
                    return;
                editPImage.setImageURI(photoTakenUri);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoTakenUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saveImageToGallery(bitmap);
            } );

    private ActivityResultLauncher startForResultFromGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                try {
                    if (result.getData() != null){
                        Uri selectedImageUri = result.getData().getData();
                        Bitmap bitmap = BitmapFactory.decodeStream(getBaseContext().getContentResolver().openInputStream(selectedImageUri));
                        saveImageToGallery(bitmap);
                        editPImage.setImageURI(selectedImageUri);


                    }
                }catch (Exception exception){
                    Log.d("TAG",""+exception.getLocalizedMessage());
                }
            }
        }
    });

    public void saveImageToGallery(Bitmap bitmap){
        OutputStream fos = null;
        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues =  new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_" + Calendar.getInstance().getTime()+ ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "TestFolder");

                mPhotoUril = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(mPhotoUril));

                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                Objects.requireNonNull(fos);
                uploadImage();

                Toast.makeText(mContext, "Image Saved", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){

            Toast.makeText(mContext, "Image not saved \n" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("uploading");
        pd.show();

        if (mPhotoUril !=null){
            final StorageReference filerefencre = storageRef.child(System.currentTimeMillis()
                    +"."+ getFileExtension(mPhotoUril));

            uploadTask =  filerefencre.putFile(mPhotoUril);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filerefencre.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();

                        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("imageurl", ""+myUrl);

                        reference2.updateChildren(hashMap);
                        pd.dismiss();
                        refreshActivity();
                        onBackPressed();
                    }else {
                        Toast.makeText(EditProfile.this, "Failed upload", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

}