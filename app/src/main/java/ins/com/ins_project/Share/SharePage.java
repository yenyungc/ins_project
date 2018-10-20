package ins.com.ins_project.Share;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ins.com.ins_project.Home.Camera;
import ins.com.ins_project.Home.PhotoEditor;
import ins.com.ins_project.R;

public class SharePage extends AppCompatActivity {

    private static final String TAG = "share page";
    Button button1;
    Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_page);
        Log.d(TAG, "onCreate: starting.");
        button1 = (Button) findViewById(R.id.camera0);
        button2 = (Button) findViewById(R.id.gallery0);
        button1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "goto camera");
                Intent intent = new Intent(SharePage.this, Camera.class);
                startActivity(intent);
            }});

        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "goto gallery");
                PhotoEditor.setSource(1);
                Intent intent = new Intent(SharePage.this, PhotoEditor.class);
                startActivity(intent);
            }});

        //Action: to upload photo
        PhotoEditor.setCaller(1);
    }
}
