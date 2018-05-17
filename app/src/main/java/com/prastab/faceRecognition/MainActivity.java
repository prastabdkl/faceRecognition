package com.prastab.faceRecognition;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prastab.faceRecognition.R;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    TextView text1;
    Button enrollBtn, listBtn, picBtn, verifyBtn;
    ImageView imageView;
    Bitmap thumbnail;
    String app_id = "9c30a6d8";
    String app_key = "ec55c93ba20e7b8de929e0cb40931d10";
    private int REQUEST_CAMERA = 100;
    private int SELECT_FILE = 200;
    private Bitmap bm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text1 = findViewById(R.id.text1);
        enrollBtn = findViewById(R.id.enroll_btn);
        listBtn = findViewById(R.id.list_btn);
        picBtn = findViewById(R.id.pic_btn);
        imageView = findViewById(R.id.image_view);
        verifyBtn = findViewById(R.id.verify_btn);


        enrollBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JsonObject json = new JsonObject();
                json.addProperty("image", "https://scontent.fktm6-1.fna.fbcdn.net/v/t1.0-9/11928742_541846712646856_7004449724994258643_n.jpg?oh=a32fc864d4247f74dcb7ffd6b0a22f20&oe=5B1F6CFA");
                json.addProperty("subject_id", "Anup");
                json.addProperty("gallery_name", "Android");
                Ion.with(getApplicationContext())
                        .load("POST", "http://api.kairos.com/enroll")
                        .addHeader("app_id", app_id)
                        .addHeader("app_key", app_key)
                        .setJsonObjectBody(json)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (e != null) {
                                    text1.setText(e.toString());
                                } else {
                                    text1.setText(result.toString());
                                    Toast.makeText(getApplicationContext(),"Image has been enrolled successfully",Toast.LENGTH_SHORT);
                                }
                            }
                        });

            }
        });

        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JsonObject json = new JsonObject();
                json.addProperty("gallery_name", "Android");
                Ion.with(getApplicationContext())
                        .load("POST", "http://api.kairos.com/gallery/view")
                        .addHeader("app_id", app_id)
                        .addHeader("app_key", app_key)
                        .setJsonObjectBody(json)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (e != null) {
                                    text1.setText(e.toString());
                                } else {
                                    text1.setText(result.toString());
                                }
                            }
                        });

            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bm = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                if (bm != null){
                    verify(ConvertBitmapToString(bm));
                }
                else{
                    Toast.makeText(getApplicationContext(),"No images selected",Toast.LENGTH_SHORT);

                }

            }
        });

        picBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPhoto();
            }
        });

    }

    //  Set the captured image into the imageView using
    public void selectPhoto() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Take Photo")) {
                    // Compose camera intent
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[which].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        // show dialog
        builder.show();
    }


        /**
         * This function handles result receiver by calling
         * startActivityForResult.
         *
         *  @param requestCode
         * @param resultCode
         * @param data
         */
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK && data != null) {
                if (requestCode == REQUEST_CAMERA) {

                    thumbnail = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    File destination = new File(Environment.getExternalStorageDirectory(),
                            System.currentTimeMillis() + ".jpg");

                    FileOutputStream fo;
                    try {
                        destination.createNewFile();
                        fo = new FileOutputStream(destination);
                        fo.write(stream.toByteArray());
                        fo.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    imageView.setImageBitmap(thumbnail);
                    bm = thumbnail;
                } else if (requestCode == SELECT_FILE) {
                    Uri selectedImageUri = data.getData();

                    String[] projection = { MediaStore.MediaColumns.DATA };
                    CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);

                    Cursor cursor = cursorLoader.loadInBackground();
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();

                    String selectedImagePath = cursor.getString(column_index);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(selectedImagePath, options);

                    final int REQUIRED_SIZE = 200;
                    int scale = 1;

                    while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                            && options.outHeight / scale / 2 >= REQUIRED_SIZE) {
                        scale *= 2;
                    }
                    options.inSampleSize = scale;
                    options.inJustDecodeBounds = false;
                    bm = BitmapFactory.decodeFile(selectedImagePath, options);

                    imageView.setImageBitmap(bm);
                }
                verify(ConvertBitmapToString(bm));
            }
        }

    public static String ConvertBitmapToString(Bitmap bitmap){
        String encodedImage = "";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        encodedImage= Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        Log.d("Base64",encodedImage);

        return encodedImage;
    }

    public void verify(String imageBase) {
        JsonObject json = new JsonObject();
        json.addProperty("image", imageBase);
        json.addProperty("gallery_name", "Android");
        json.addProperty("subject_id", "Anup");
        Ion.with(getApplicationContext())
                .load("POST", "http://api.kairos.com/verify")
                .addHeader("app_id", app_id)
                .addHeader("app_key", app_key)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            text1.setText(e.toString());
                        } else {
                            text1.setText(result.toString());
                        }
                    }
                });

    }
}
