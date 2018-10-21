package ins.com.ins_project.Share;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import ins.com.ins_project.Home.Camera;
import ins.com.ins_project.Home.PhotoEditor;
import ins.com.ins_project.R;
import ins.com.ins_project.Utils.BottomNavigationViewHelper;

public class SharePage extends AppCompatActivity {

    private static final String TAG = "share page";
    Button button1;
    Button button2;

    private static final int ACTIVITY_NUM = 2;
    private Context mContext = SharePage.this;

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
                String action = getAction();
                intent.setAction(action);
                startActivity(intent);
            }});

        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "goto gallery");
                Intent intent = new Intent(SharePage.this, PhotoEditor.class);
                String action = getAction();
                intent.setAction(action);
                //Open photo editor
                startActivity(intent);
            }});

        setupBottomNavigationView();
        //Action: to upload photo
    }

    private String getAction() {
        Intent intent = getIntent();
        return intent.getAction();
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
