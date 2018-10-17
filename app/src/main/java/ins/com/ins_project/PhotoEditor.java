package ins.com.ins_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.util.Log;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.widget.TextView;


import java.util.ArrayList;

public class PhotoEditor extends AppCompatActivity {

    private final String TAG = "PhotoEditor";

    private Button saveButton;
    private Button brightnessButton;
    private Spinner filterSpinner;
    private Button cropButton;
    private Button confirmButton;
    private SeekBar brightness;
    private SeekBar contrast;
    private TextView text1;
    private TextView text2;
    private ArrayAdapter<String> arr_adapter;
    private ImageView imageToShow;


    private ArrayList<String> filter_list;
    private int FILTER_USED = 0;
    private static final int IMAGE = 1;
    private static final int PERMISSION_REQUEST_STORAGE = 1;

    // The bitmap of the image to process
    private Bitmap bm;
    // The bitmap of the image after processing
    private Bitmap new_bm;
    //Height and width of the image
    private int imgHeight, imgWidth;
    /**
     * The source of photo
     * 1 means from gallery
     * 2 means from camera
     */
    private static int PHOTOSOURCE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);
        if (ContextCompat.checkSelfPermission(PhotoEditor.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "asking for permissions");
            requestDataPermission();
            return;
        }
        Log.d(TAG, "initial");
        saveButton = (Button) findViewById(R.id.save);
        brightnessButton = (Button) findViewById(R.id.brightnessButton);
        cropButton = (Button) findViewById(R.id.crop);
        filterSpinner = (Spinner) findViewById(R.id.filer);
        confirmButton = (Button) findViewById(R.id.confirm);
        brightness = (SeekBar) findViewById(R.id.brightness);
        contrast = (SeekBar) findViewById(R.id.contrast);
        text1 = (TextView) findViewById(R.id.textView);
        text2 = (TextView) findViewById(R.id.textView2);
        imageToShow = (ImageView) findViewById(R.id.image);

        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // save

                Log.d(TAG, "save photo");

                save();

            }});

        brightnessButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // change brightness and contrast
                Log.d(TAG, "change brightness and contrast");

                brightness();

            }});

        cropButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // change brightness and contrast

                Log.d(TAG, "crop photo");
                crop();
            }});

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // confirm changes

                Log.d(TAG, "confirm changes");
                confirm();
            }});

        //Open image
        if (PHOTOSOURCE == 1) {
            // Open a photo from gallery
            openGallery();
        } else {
            // Get photo from camera
            //TODO
        }

        //The filters provided
        initFilers();

        // Initialize the filters
        arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filter_list);
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(arr_adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectFilers(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        contrast.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
                changeContrast(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        brightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
                changeBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {

            }
        });
    }


    // Ask for permission to read and write to storage
    private void requestDataPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            ActivityCompat.requestPermissions(PhotoEditor.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);


        } else {
            ActivityCompat.requestPermissions(PhotoEditor.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length != 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "permission denied");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //open the phone gallery
    protected void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PHOTOSOURCE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "received result");
        super.onActivityResult(requestCode, resultCode, data);
        //The direction of image
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            Log.d(TAG, "image path:"+imagePath);
            showImage(imagePath);
            c.close();
        }
    }

    //load image
    private void showImage(String path){
        bm = BitmapFactory.decodeFile(path);
        getImageInfo(bm);
        ((ImageView)findViewById(R.id.image)).setImageBitmap(bm);
    }

    protected static void setSource(int source) {
        PHOTOSOURCE = source;
    }

    private void getImageInfo(Bitmap b) {
        imgHeight = b.getHeight();
        imgWidth = b.getWidth();
    }



    private void save() {

    }

    // press the button
    private void brightness() {

        cropButton.setVisibility(View.INVISIBLE);
        brightnessButton.setVisibility(View.INVISIBLE);
        saveButton.setVisibility(View.INVISIBLE);
        confirmButton.setVisibility(View.VISIBLE);
        filterSpinner.setVisibility(View.INVISIBLE);
        brightness.setVisibility(View.VISIBLE);
        contrast.setVisibility(View.VISIBLE);
        text1.setVisibility(View.VISIBLE);
        text2.setVisibility(View.VISIBLE);
    }

    private void changeBrightness(int progress) {
        Bitmap bmp = Bitmap.createBitmap(imgWidth, imgHeight, Config.ARGB_8888);
        int brightness = progress - 127;
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[] { 1, 0, 0, 0, brightness, 0, 1, 0, 0, brightness, 0, 0, 1,
                0, brightness, 0, 0, 0, 1, 0 });

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bm, 0, 0, paint);
        imageToShow.setImageBitmap(bmp);
    }

    private void changeContrast(int progress) {
        Bitmap bmp = Bitmap.createBitmap(imgWidth, imgHeight, Config.ARGB_8888);
        float contrast = (float) ((progress + 64) / 128.0);
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[] { contrast, 0, 0, 0, 0, 0, contrast, 0, 0, 0, 0, 0,
                contrast, 0, 0, 0, 0, 0, 1, 0 });
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

        Canvas canvas = new Canvas(bmp);
        // draw the new pic on canvas
        canvas.drawBitmap(bm, 0, 0, paint);

        imageToShow.setImageBitmap(bmp);
    }

    private void crop() {

    }


    //Initialize the filers
    private void initFilers() {
        filter_list = new ArrayList<String>();
        filter_list.add("No filter");
        filter_list.add("Negative");
        filter_list.add("Blackboard");
        filter_list.add("Mono");
    }

    //The filters to elect
    private void selectFilers(int i) {
        switch (i) {
            case(0): {
                FILTER_USED = 0;
                noFilterMode();

                Log.d(TAG,"no filter used");
                break;
            }
            case(1): {
                FILTER_USED = 1;
                negativeMode();

                Log.d(TAG,"filter 1 used");
                break;
            }
            case(2): {
                FILTER_USED = 2;
                blacknWhiteMode();

                Log.d(TAG,"filter 2 used");
                break;
            }
            case(3): {
                FILTER_USED = 3;
                monoMode();

                Log.d(TAG,"filter 3 used");
                break;
            }
        }
    }

    //The 3 types of filters provided
    private void noFilterMode() {

    }

    private void negativeMode() {

    }

    private void blacknWhiteMode() {

    }

    private void monoMode() {

    }

    private void confirm() {
        cropButton.setVisibility(View.VISIBLE);
        brightnessButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);
        confirmButton.setVisibility(View.INVISIBLE);
        filterSpinner.setVisibility(View.VISIBLE);
        brightness.setVisibility(View.INVISIBLE);
        contrast.setVisibility(View.INVISIBLE);
        text1.setVisibility(View.INVISIBLE);
        text2.setVisibility(View.INVISIBLE);
    }
}

//调用系统相册
//blog.csdn.net/w18756901575/article/details/52085157
//https://blog.csdn.net/u011150924/article/details/71748464

//亮度对比度
//http://blog.csdn.net/sxwyf248/article/details/7019731