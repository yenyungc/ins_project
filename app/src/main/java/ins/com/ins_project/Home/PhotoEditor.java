package ins.com.ins_project.Home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ins.com.ins_project.BuildConfig;
import ins.com.ins_project.R;

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
    private static final int CROP_PHOTO = 2;
    private static final int PERMISSION_REQUEST_STORAGE = 1;

    // The bitmap of the image to process
    private Bitmap bm;
    // The bitmap of the image after processing
    private Bitmap bmCopy;
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
                // save a temp file and apply crop on it

                Log.d(TAG, "crop photo");
                saveTemp(bm);
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
        if (requestCode == CROP_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
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
        // A copy of the image in bitMap
        copyImg();
        getImageInfo(bm);
        ((ImageView)findViewById(R.id.image)).setImageBitmap(bm);
    }

    public static void setSource(int source) {

        PHOTOSOURCE = source;
    }

    private void getImageInfo(Bitmap b) {
        imgHeight = b.getHeight();
        imgWidth = b.getWidth();
    }


    //saving photo to storage
    private void save() {
        Log.d(TAG, "saving image");
        File appDir = new File(Environment.getExternalStorageDirectory(),"ins_project_photos");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        String path = "ins_project_photos" + fileName;
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Log.d(TAG, "image saved");
            Toast.makeText(this,"photo saved"+fileName,Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Insert the photo to system gallery
        try {
            MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Broadcast the update
        getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + path)));
    }

    //save a temporary file and then apply crop method on
    private void saveTemp(Bitmap b) {
        Log.d(TAG, "saving a temp image for cropping");
        File appDir = new File(Environment.getExternalStorageDirectory(),"ins_project_photos");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Log.d(TAG, "temp image saved");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        crop(file);
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

    private void crop(File file) {
        Intent intent = new Intent("com.android.camera.action.CROP");

        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri contentUri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".fileProvider", file);
        intent.setDataAndType(contentUri, "image/*");

        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0.1);
        intent.putExtra("aspectY", 0.1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        startActivityForResult(intent, CROP_PHOTO);
    }


    //Initialize the filers
    private void initFilers() {
        filter_list = new ArrayList<String>();
        filter_list.add("No filter");
        filter_list.add("Negative");
        filter_list.add("Blackboard");
        filter_list.add("Nostalgic");
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
                nostalgicMode();

                Log.d(TAG,"filter 3 used");
                break;
            }
        }
    }

    //The 3 types of filters provided
    private void noFilterMode() {
        bm = bmCopy;
        imageToShow.setImageBitmap(bm);
    }

    private void negativeMode() {
        /*
         * Algorithm: B.r = 255 - B.r; B.g = 255 - B.g; B.b = 255 - B.b;
         */
        int width = imgWidth;
        int height = imgHeight;
        Bitmap bmp = bm;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int[] oldPixels = new int[width * height];
        int[] newPixels = new int[width * height];
        int color;
        int pixelsR, pixelsG, pixelsB, pixelsA;

        bmp.getPixels(oldPixels, 0, width, 0, 0, width, height);
        for (int i = 1; i < height * width; i++) {
            color = oldPixels[i];
            // get the RGB value
            pixelsA = Color.alpha(color);
            pixelsR = Color.red(color);
            pixelsG = Color.green(color);
            pixelsB = Color.blue(color);
            // Transform to negative
            pixelsR = (255 - pixelsR);
            pixelsG = (255 - pixelsG);
            pixelsB = (255 - pixelsB);
            // should be in range of 0 and 255
            if (pixelsR > 255) {
                pixelsR = 255;
            } else if (pixelsR < 0) {
                pixelsR = 0;
            }
            if (pixelsG > 255) {
                pixelsG = 255;
            } else if (pixelsG < 0) {
                pixelsG = 0;
            }
            if (pixelsB > 255) {
                pixelsB = 255;
            } else if (pixelsB < 0) {
                pixelsB = 0;
            }
            // generate new pixels
            newPixels[i] = Color.argb(pixelsA, pixelsR, pixelsG, pixelsB);
        }
        bitmap.setPixels(newPixels, 0, width, 0, 0, width, height);
        bm = bitmap;
        imageToShow.setImageBitmap(bitmap);
    }


    public void blacknWhiteMode() {
        int width = imgWidth;
        int height = imgHeight;
        Bitmap bmp = bm;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        bm = bitmap;
        imageToShow.setImageBitmap(bitmap);
    }

    public void nostalgicMode() {
        /*
         * Algorithm: RGB R=0.393r+0.769g+0.189b G=0.349r+0.686g+0.168b B=0.272r+0.534g+0.131b
         */
        int width = imgWidth;
        int height = imgHeight;
        Bitmap bmp = bm;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int k = 0; k < width; k++) {
                pixColor = pixels[width * i + k];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
                newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
                newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
                int newColor = Color.argb(255, newR > 255 ? 255 : newR,
                        newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
                pixels[width * i + k] = newColor;
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        bm = bitmap;
        imageToShow.setImageBitmap(bitmap);
    }

    private void copyImg() {
        bmCopy = bm;
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
        copyImg();
        showToast("Confirm!");
    }

    private void showToast(final String text) {


        PhotoEditor.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PhotoEditor.this, text, Toast.LENGTH_SHORT).show();
            }
        });

    }
}

//调用系统相册
//blog.csdn.net/w18756901575/article/details/52085157
//https://blog.csdn.net/u011150924/article/details/71748464

//亮度对比度
//http://blog.csdn.net/sxwyf248/article/details/7019731

//滤镜
//https://blog.csdn.net/aqi00/article/details/51331531

//剪裁
//https://blog.csdn.net/wuliang756071448/article/details/71080968
