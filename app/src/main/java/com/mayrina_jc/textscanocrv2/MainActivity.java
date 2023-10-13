package com.mayrina_jc.textscanocrv2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    EditText textfield;
    Button capture,copy,upl,share;
    LinearLayout l1,l2,l3,l4;
    ImageView imageviewTest;
    MenuItem menuItem;
    ActivityResultLauncher<Intent> activityResultLauncher;
    public static final int PICK_IMAGE = 1;
    private static final int REQUEST_CAMERA_CODE=100,REQUEST_EXTERNAL_CODE=100;
    Task<Text> textTask;
    private String currentPhotoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textfield = (EditText) findViewById(R.id.textfield);
        capture = (Button) findViewById(R.id.capture);
        copy = (Button) findViewById(R.id.copy);
        share = (Button) findViewById(R.id.share);
        upl = (Button) findViewById(R.id.upload);
        l1 = (LinearLayout) findViewById(R.id.layer1);
        l2 = (LinearLayout) findViewById(R.id.layer2);
        l3 = (LinearLayout) findViewById(R.id.layer3);
        l4 = (LinearLayout) findViewById(R.id.layer4);
        imageviewTest = (ImageView) findViewById(R.id.imageviewTest);

        capture.setOnClickListener(v -> {
            //check and ask for camera permission
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.CAMERA
                },REQUEST_CAMERA_CODE);
            }
            else {
                //open camera
                String fileName = "photo";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                try {
                    File imageFile = File.createTempFile(fileName,".png", storageDir);

                    currentPhotoPath = imageFile.getAbsolutePath();

                    Uri imageUri = FileProvider.getUriForFile(MainActivity.this,"com.mayrina_jc.textscanocrv2.fileprovider",imageFile);

                    Intent cameraInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraInt.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    activityResultLauncher.launch(cameraInt);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        upl.setOnClickListener(v -> {
            //check and ask for camera permission
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },REQUEST_EXTERNAL_CODE);
            }
            else{
                //open gallery
                try {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        copy.setOnClickListener(view -> {
            String scantext = textfield.getText().toString();
            copyToClip(scantext);
        });
        share.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,textfield.getText().toString());
            startActivity(Intent.createChooser(intent,"Share Via"));
        });
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(currentPhotoPath!=null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

                    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                    InputImage image = InputImage.fromBitmap(bitmap, 0);
                    textTask = recognizer.process(image)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text text) {
                                    textfield.setText(textTask.getResult().getText());
                                    if(textfield.getText().length()<=0){
                                        Toast.makeText(MainActivity.this, "Can't find any text!", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        l1.setVisibility(View.VISIBLE);
                                        l2.setVisibility(View.INVISIBLE);
                                        l3.setVisibility(View.VISIBLE);
                                        l4.setVisibility(View.VISIBLE);
                                        Toast.makeText(MainActivity.this, "Successfully converted to text", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Can't find any text!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            Uri filepath = data.getData();

            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            InputImage image = null;
            try {
                image = InputImage.fromFilePath(MainActivity.this, filepath);
                textTask = recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                textfield.setText(textTask.getResult().getText());
                                if(textfield.getText().length()<=0){
                                    Toast.makeText(MainActivity.this, "Can't find any text!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    l1.setVisibility(View.VISIBLE);
                                    l2.setVisibility(View.INVISIBLE);
                                    l3.setVisibility(View.VISIBLE);
                                    l4.setVisibility(View.VISIBLE);
                                    Toast.makeText(MainActivity.this, "Successfully converted to text", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Can't find any text!", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyToClip(String text){
        Toast.makeText(MainActivity.this,"Copied to clipboard!", Toast.LENGTH_SHORT).show();
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied data",text);
        clipboardManager.setPrimaryClip(clipData);
    }
}