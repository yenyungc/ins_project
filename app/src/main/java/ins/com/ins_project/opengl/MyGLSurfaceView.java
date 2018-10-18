package ins.com.ins_project.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;

class MyGLSurfaceView extends GLSurfaceView {

    private static final String TAG = "MyGLSurfaceView";

    private int CLICK_ACTION_THRESHOLD = 15;
    private float startX;
    private float startY;

    public MyGLRenderer mRenderer;
    private static final float pi = 3.14159f;
    private float width;
    private float height;
    private boolean down = false;
    private Context mContext;
    private ProgressBar mProgressBar;

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setConfig(Context context, float height, float width, JSONArray userStories, int resourceIndex) {
        mContext = context;
        this.width = width;
        this.height = height;
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        try {
            mRenderer = new MyGLRenderer(mContext, height, width, userStories, MyGLSurfaceView.this, resourceIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        Log.d(TAG, "onTouchEvent: POSITION: " + x);

        switch (e.getAction()) {

            case MotionEvent.ACTION_UP:

                if (down && !mRenderer.mRotateCounterClockwise && !mRenderer.mRotateClockwise) {
                    float endX = e.getX();
                    float endY = e.getY();
                    if (isAClick(startX, endX, startY, endY)) {
                        Log.d(TAG, "onTouchEvent: detected a click");
                        Log.d(TAG, "onTouchEvent: ACTION_UP");
                        try {
                            mRenderer.incrementMediaIndex();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        mRenderer.setStopped(true);
                    }
                    down = false;
                }
                break;


            case MotionEvent.ACTION_DOWN:
                if (!mRenderer.mRotateCounterClockwise && !mRenderer.mRotateClockwise) {
                    Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                    mRenderer.setStartPositionX(e.getX());
                    mRenderer.setStopped(false);
                    startX = e.getX();
                    startY = e.getY();
                    Log.d(TAG, "onTouchEvent: starting position: " + e.getX());

                    mRenderer.pausePlayer();
                    //set this so the ACTION_UP flag doesn't trigger when you first touch the screen
                    down = true;
                }

            case MotionEvent.ACTION_MOVE:
                if (!mRenderer.mRotateCounterClockwise && !mRenderer.mRotateClockwise) {
                    Log.d(TAG, "onTouchEvent: ACTION_MOVE");

                    float endX = e.getX();
                    float endY = e.getY();
                    if (!isAClick(startX, endX, startY, endY)) {
                        if (Math.abs(startX - x) < 800) {
                            mRenderer.setPosition(x);
                        }
                    }
                    break;
                }
        }
        return true;
    }


    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        Log.d(TAG, "isAClick: differenceX: " + differenceX);
        Log.d(TAG, "isAClick: differenceY: " + differenceY);
        if (!mRenderer.mRotateCounterClockwise && !mRenderer.mRotateClockwise) {
            return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY > CLICK_ACTION_THRESHOLD);
        }
        return false;
    }

    public void reset() {
        mRenderer.setStopped(false);
        mRenderer.setDx(0);
        mRenderer.setStartPositionX(0);
    }
}











