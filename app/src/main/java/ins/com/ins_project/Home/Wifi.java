package ins.com.ins_project.Home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ins.com.ins_project.R;

public class Wifi extends Activity {
    private final String TAG = "wifi0";
    Button btn_server,btn_clien;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "initial!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        btn_server= (Button) findViewById(R.id.goto_server);
        btn_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Wifi.this, ins.com.ins_project.Home.ServerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_clien= (Button) findViewById(R.id.goto_client);
        btn_clien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Wifi.this, ins.com.ins_project.Home.ClientActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
