package ins.com.ins_project.opengl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import ins.com.ins_project.R;

public class OpenGLES10Activity extends AppCompatActivity {

    private static final String TAG = "OpenGLES10Activity";
    private static final int RELEASE_PLAYER = 0;
    private static final int INITIALIZE_PLAYER = 1;
    private static final int LOADING_ERROR = 2;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private ArrayList<String[]> mMedia = new ArrayList<>();
    private JSONArray mMasterStoriesArray = new JSONArray();
    private MyGLSurfaceView mGLView;
    private float mScreenWidth;
    private float mScreenHeight;

    private int resourceIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl);
        getIncomingIntent();
    }


    private void getIncomingIntent() {
        Log.d(TAG, "getIncomingIntent: checking for incoming intent.");

        if (getIntent().hasExtra(getString(R.string.user_stories)) && getIntent().hasExtra(getString(R.string.resource_index))) {
            Log.d(TAG, "getIncomingIntent: found extras.");

            String jsonArray = getIntent().getStringExtra(getString(R.string.user_stories));
            try {
                mMasterStoriesArray = new JSONArray(jsonArray);
                resourceIndex = getIntent().getIntExtra(getString(R.string.resource_index), 0);
                initSurfaceView();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void initSurfaceView() {
        mGLView = findViewById(R.id.my_gl_surfaceview);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;
        mScreenHeight = displaymetrics.heightPixels;

        Log.d(TAG, "initSurfaceView: screen width: " + mScreenWidth);
        Log.d(TAG, "initSurfaceView: screen height: " + mScreenHeight);

        mGLView.setConfig(this, mScreenHeight, mScreenWidth, mMasterStoriesArray, resourceIndex);

    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: called.");
        super.onResume();
        if (mGLView == null) {
//            initSurfaceView();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: called.");
        super.onPause();
        if (mGLView != null) {
            mGLView.onPause();
            mGLView.reset();
            releasePlayers();
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: called.");
        super.onStop();
        if (mGLView != null) {
            mGLView.reset();
            releasePlayers();
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: called.");
        super.onStart();
//        initSurfaceView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        if (mGLView != null) {
            mGLView.reset();
            releasePlayers();
        }
    }

    private void releasePlayers() {
        mGLView.mRenderer.releasePlayers();
    }
}