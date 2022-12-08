package com.example.agroplanner;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PostImage extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ImageView imgSelected;
    private static final String[] permissionsArray = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    Context mContext = this;
    File mPhotoFile;
    Uri photoTakenUri,mPhotoUril;
    TextView post, plont_name_load, plot_name_load;
    int selectedDay, selectedMonth, selectedYear;
    EditText description, fertilizer;
    StorageReference storageRef;
    String miUrlOk = "";
    String plantName, plotName, plotid;
    private StorageTask uploadTask;
    Uri mImageUri;
    Bitmap bitmap;
    String date_checker, description_checker, fertilizer_checker, image_upload, upload_pass;

    private TextView dateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            plantName = extras.getString("plantName");
            plotName = extras.getString("plotName");
            plotid = extras.getString("plotid");
        }

        plont_name_load = findViewById(R.id.plont_name_load);
        plont_name_load.setText(plantName);
        plot_name_load = findViewById(R.id.plot_name_load);
        plot_name_load.setText(plotName);

        imgSelected = findViewById(R.id.image_added);
        dateText = findViewById(R.id.date_text);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        fertilizer = findViewById(R.id.fertilizer);

        findViewById(R.id.date_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }});

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    permissionsArray, 1);
        }

        //Request automatically
        callCropCamera();

        //Request by click
        imgSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callCropCamera();
            }
        });

        //Post button
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage_10();
            }
        });

    }

    public void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//        String date = "month/day/year: " + (month+1) + "/" + dayOfMonth + "/" + year;
        String date = dayOfMonth + "/" + (month+1) + "/" + year;
        selectedDay = dayOfMonth;
        selectedMonth = (month+1);
        selectedYear = year;

        dateText.setText(date);
        date_checker = "true";
    }

    public void callCropCamera(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.lbl_set_post_photo));
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
                //Image for Camera
                imgSelected.setImageURI(photoTakenUri);
                mImageUri = photoTakenUri;
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoTakenUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saveImageToGallery(bitmap);
                image_upload = "yes";
            } );

    private ActivityResultLauncher startForResultFromGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                try {
                    if (result.getData() != null){
                        //Image for Gallery
                        Uri selectedImageUri = result.getData().getData();
                        Bitmap bitmap = BitmapFactory.decodeStream(getBaseContext().getContentResolver().openInputStream(selectedImageUri));
                        //Disable to avoid duplicate image in the gallery
                        mImageUri = selectedImageUri;
                        imgSelected.setImageURI(selectedImageUri);
                        image_upload = "yes";
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

                Toast.makeText(mContext, "Image Saved", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){

            Toast.makeText(mContext, "Image not saved \n" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //Upload
    private void uploadImage_10() {

        //Check whether value is empty or not
        if(image_upload == "yes"){
            description_checker = description.getText().toString();
            if(description_checker.isEmpty()){
                upload_pass = "no";
                Toast.makeText(getApplicationContext(), "please insert daily note", Toast.LENGTH_SHORT).show();
            }
            else{
                if(date_checker == "true"){
                    fertilizer_checker = fertilizer.getText().toString();
                    if(fertilizer_checker.isEmpty()){
                        fertilizer_checker = "None";
                        upload_pass = "yes";
                    }
                    else{
                        upload_pass = "yes";
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "please select date", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            callCropCamera();
        }

        if(upload_pass == "yes" ){
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Posting");
            pd.show();

            //id == user id
            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance()
            .getReference("Record/" +plotid+"/Daily/" +selectedDay +"/" +selectedMonth +"/" +selectedYear);
            String postid = reference.push().getKey();

            // Create a storage reference from our app
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            // Create a reference to "image.jpg"
            StorageReference postReference = FirebaseStorage.getInstance().getReference("Plants/"+id+"/"+plotid+"/Daily").child(System.currentTimeMillis() + ".null");

            // Create a reference to 'image.jpg'
            StorageReference pictureImagesReference = storageRef.child("post/images.jpg");

            // While the file names are the same, the references point to different files
            postReference.getName().equals(pictureImagesReference.getName());    // true
            postReference.getPath().equals(pictureImagesReference.getPath());    // false

            imgSelected.setDrawingCacheEnabled(true);
            imgSelected.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imgSelected.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = postReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(PostImage.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("postid", postid);
                                    hashMap.put("type", "image");
                                    hashMap.put("day", selectedDay);
                                    hashMap.put("month", selectedMonth);
                                    hashMap.put("year", selectedYear);
                                    hashMap.put("imageUrl", imageUrl);
                                    hashMap.put("description", description.getText().toString());
                                    hashMap.put("fertilizer", fertilizer_checker);
                                    hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                    reference.child(postid).setValue(hashMap);

                                    pd.dismiss();

                                    startActivity(new Intent(PostImage.this, Plants.class));
                                    finish();
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    //Close Button
    public void CloseButton (View view){
        Intent intent = new  Intent(getApplicationContext(), Plants.class);
        startActivity(intent);
    }
}
