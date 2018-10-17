package ins.com.ins_project;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "main";
    Button button1;
    Button button2;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.Camera);
        button2 = (Button) findViewById(R.id.Gallery);
        button1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "goto camera");
                Intent intent = new Intent(MainActivity.this, Camera.class);
                startActivity(intent);
            }});

        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "goto gallery");
                PhotoEditor.setSource(1);
                Intent intent = new Intent(MainActivity.this, PhotoEditor.class);
                startActivity(intent);
            }});


        setupFirebaseAuth();

//        initImageLoader();
//        setupBottomNavigationView();
//        setupViewPager();



    }


         /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * checks to see if the @param 'user' is logged in
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if(user == null){
//            Intent intent = new Intent(mContext, LoginActivity.class);
//            startActivity(intent);
        }
    }
    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
