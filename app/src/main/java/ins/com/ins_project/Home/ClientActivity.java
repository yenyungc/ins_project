package ins.com.ins_project.Home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import ins.com.ins_project.R;

public class ClientActivity extends Activity {
    private static String file_path=null;
    Button link_wifi, link_server, send_file, send;
    EditText ET_print, ET_input;
    ScrollView scrollView;
    WifiManager wifiManager;
    int wifiState=0;
    private Socket socket;
    private ReadThread readThread;
    String reviceMgs=null;
    long size = 0;
    long sendtime =0;
    private final BroadcastReceiver wifiBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)){
                //signal strength changed
            }
            else if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){//wifi打开与否
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);

                if(wifistate == WifiManager.WIFI_STATE_DISABLED){
                    System.out.println("wifi OFF");
                    link_wifi.setText("WIFI disabled");
                    wifiState=0;

                }
                else if(wifistate == WifiManager.WIFI_STATE_ENABLED){
                    System.out.println("wifi ON");
                   if(isWifiConnected()){
                       if(getConnectWifiSsid().equals("\"wifisocket\"")){
                           link_wifi.setText("connected to server's WIFI");
                           wifiState=3;
                       }else{
                           link_wifi.setText("WIFI connected(Non-server)");
                           wifiState=4;
                       }
                   }else{
                       link_wifi.setText("WIFI already ON");
                       wifiState=1;
                   }


                }
            }
            else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){//wifi连接上与否
                System.out.println("网络状态改变");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
                    System.out.println("wifi Disconnected");
                    link_wifi.setText("WIFI Disconnected");
                    wifiState=2;
                }
                else if(info.getState().equals(NetworkInfo.State.CONNECTED)){

                    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    //获取当前wifi名称
                    System.out.println("连接到网络 " + wifiInfo.getSSID());
                    if(wifiInfo.getSSID().equals("\"wifisocket\"")){
                        link_wifi.setText("connected to server's WIFI");
                        wifiState=3;
                    }else{
                        link_wifi.setText("WIFI connected(Non-Server)");
                        wifiState=4;
                    }

                }

            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        //WIFI状态接收器


        registerReceiver(wifiBroadcast, makeGattUpdateIntentFilter());
        link_wifi = (Button) findViewById(R.id.client_link_wifi);
        link_server = (Button) findViewById(R.id.client_link_server);
        send_file = (Button) findViewById(R.id.client_file);
        send = (Button) findViewById(R.id.client_send);
        scrollView= (ScrollView) findViewById(R.id.client_scrollView);

        link_wifi.setOnClickListener(cccc);
        link_server.setOnClickListener(cccc);
        send_file.setOnClickListener(cccc);
        send.setOnClickListener(cccc);

        ET_print = (EditText) findViewById(R.id.client_print);
        ET_input = (EditText) findViewById(R.id.client_input);



    }

    View.OnClickListener cccc = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.client_link_wifi:
                    switch (wifiState){
                        case 0:
                            final AlertDialog.Builder normalDialog =
                                    new AlertDialog.Builder(ClientActivity.this);
                            normalDialog.setTitle("Attention");
                            normalDialog.setMessage("turn on wifi?");
                            normalDialog.setPositiveButton("Confirm",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!wifiManager.isWifiEnabled()) {
                                                wifiManager.setWifiEnabled(true);
                                            }
                                        }
                                    });
                            normalDialog.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            // 显示
                            normalDialog.show();
                            break;
                        case 1:
                        case 2:
                        case 4:
                            if (!wifiManager.isWifiEnabled()) {
                                wifiManager.setWifiEnabled(true);
                            }
                            final AlertDialog.Builder normalDialog2 =
                                    new AlertDialog.Builder(ClientActivity.this);
                            normalDialog2.setTitle("Attention");
                            normalDialog2.setMessage("是否连接服务器WiFi？(请确认服务器热点已开启," +
                                    "如果无法连接，请去WIFI列表连接服务器WIFI“wifisocket”,密码“00000000”)");
                            normalDialog2.setPositiveButton("Confirm",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            WifiConfiguration config = new WifiConfiguration();
                                            config.SSID = "\"wifisocket\"";
                                            config.preSharedKey = "\"00000000\"";//加密wifi
                                            config.hiddenSSID = true;
                                            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                                            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                                            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                                            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                                            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                                            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                                            config.status = WifiConfiguration.Status.ENABLED;
                                            int netId = wifiManager.addNetwork(config);
                                            boolean b = wifiManager.enableNetwork(netId, true);
//                                            if(b){
//                                                Toast.makeText(getApplication(),"连接成功！",Toast.LENGTH_SHORT).show();
//                                            }else{
//                                                Toast.makeText(getApplication(),"连接失败！请确定服务器热点是否开启！",Toast.LENGTH_SHORT).show();
//
//                                            }
                                        }
                                    });
                            normalDialog2.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            // 显示
                            normalDialog2.show();
                            break;
                        case 3:
                            final AlertDialog.Builder normalDialog3 =
                                    new AlertDialog.Builder(ClientActivity.this);
                            normalDialog3.setTitle("Attention");
                            normalDialog3.setMessage("Connected to server WIFI!");
                            normalDialog3.setPositiveButton("Confirm",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            // 显示
                            normalDialog3.show();
                            break;

                    }

                    break;
                case R.id.client_link_server:
                    new Thread(runnable).start();//开启线程
                    break;
                case R.id.client_file:

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    try {
                        startActivityForResult( Intent.createChooser(intent, "Select a File to Upload"), 100);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.client_send:
                    if(socket==null){
                        Toast.makeText(getApplicationContext(),"please connect first！", Toast.LENGTH_SHORT).show();

                    }else{
                        String input= ET_input.getText().toString().trim();
                        if(!input.isEmpty()&&input!=null){
                            byte si[] = new byte[0];
                            try {
                                si = input.getBytes("UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            if(send(socket,si)){
                                ET_print.getText().append("\n"+"client:"+input);
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                            }else {
                                ET_print.getText().append("\n"+"*****发送数据失败*****");
                                Toast.makeText(getApplicationContext(),"sending file！", Toast.LENGTH_SHORT).show();
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                            }

                        }else{
                            Toast.makeText(getApplicationContext(),"empty！", Toast.LENGTH_SHORT).show();
                        }
                    }
                    ET_input.setText("");
                    break;
            }

        }
    };


    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            initClientSocket();
            readThread = new ReadThread();
            readThread.start();
        }
    };
    private class ReadThread extends Thread {

        @Override
        public void run() {
            byte[]mode=new byte[13];//对话0  文件1
            InputStream inputStream  = null;
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (inputStream!= null) {
                for(int i=0;i<13;i++){
                    try {
                        mode[i]=(byte)inputStream.read();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
//                        try {
//                            Log.e("关闭socket"," socket.close();");
//                            inputStream.close();
//                            socket.close();
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                        }
                    }
                }
                String modestr =new String(mode);
                //传送的数据模式 0 对话  1 文件
                try{
                int modeCode= Integer.parseInt(modestr.substring(0,1), 10);
                //对话或文件大小
                int inLength= Integer.parseInt(modestr.substring(1,11), 10);
                //如果是文件的话文件名长度
                int nameLength= Integer.parseInt(modestr.substring(11,13), 10);
                byte[]fileName=new byte[nameLength];
                byte[]mgs=new byte[inLength];

                for(int i=0;i<inLength;i++){
                    try {
                        mgs[i]=(byte)inputStream.read();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if(modeCode==0){
                    reviceMgs=new String(mgs,"UTF-8");
                    Log.e("收到数据",reviceMgs.toString());
                    handler.sendEmptyMessage(3);
                    //  ET_print.getText().append("\n"+new String(mgs));
                }else if (modeCode==1){
                }
                }catch (Exception e){

                }
            }


        }

    }

    public void initClientSocket() {
        try {
            socket = new Socket("192.168.43.1", 9999);
            // output = new PrintStream(socket.getOutputStream(), true, "gbk");
            handler.sendEmptyMessage(1);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            System.out.println("请检查端口号是否为服务器IP");
            handler.sendEmptyMessage(5);
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("服务器未开启");
            handler.sendEmptyMessage(5);
            e.printStackTrace();
        }
        // output.println("this is the message from client");
    }

    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;

    }

    public static Boolean send(Socket socket, byte[] msg) {
        DataOutputStream out=null;
        try {
            out = new DataOutputStream((socket.getOutputStream()));
            String mode ="0";
            String name ="00";
            int mgsLength =msg.length;
            String ll = String.format("%010d", mgsLength);
            String sss=mode+ll+name;
            byte sendByte[] =addBytes(sss.getBytes("UTF-8"),msg);
            Log.e("aa","类容字节"+ Arrays.toString(sendByte));
            out.write(sendByte);
            System.out.println("写入数据");
            return true;
            //  out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("写入数据异常");
            return false;
        }
    }

    public Boolean sendfile(Socket socket) {
        DataOutputStream out=null;
        try {
            out = new DataOutputStream((socket.getOutputStream()));
            String mode ="1";

            String[] getname = file_path.split("/");

            String filename=getname[getname.length-1];

            byte filenamebyte[] =filename.getBytes("UTF-8");

            String name = String.format("%02d", filenamebyte.length);

            File file =new File(file_path);



            if (file.exists()) {
                sendtime=0;
                FileInputStream fis = null;
                fis = new FileInputStream(file);
                size = fis.available();
                String filesizeString = String.format("%010d", size);
                String sss=mode+filesizeString+name;
                byte sendByteHead[] =addBytes(sss.getBytes("UTF-8"),filenamebyte);
                out.write(sendByteHead);
                handler.sendEmptyMessage(91);
                int n=512;
                byte buffer[]=new byte[n];
                while((fis.read(buffer,0,n)!=-1)&&(n>0)){
                    System.out.println(Arrays.toString(buffer));
                    System.out.println(new String(buffer));
                    out.write(buffer);
                    handler.sendEmptyMessage(92);
                }
                System.out.println("写入完成");
                handler.sendEmptyMessage(93);
                fis.close();
                return true;
            } else {
                file.createNewFile();
                Log.e("获取文件大小", "文件不存在!");
                return false;
            }

            //  out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("写入数据异常");
            return false;
        }
    }

    private String getConnectWifiSsid() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID", wifiInfo.getSSID());
        return wifiInfo.getSSID();
    }
    private boolean isWifiOpened() {
        return wifiManager.isWifiEnabled();
    }
    private boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo.isConnected();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        return filter;
    }

    public Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case 91:
                    ET_print.getText().append("\n"+"*****file size:"+size+"Byte*****");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    break;
                case 92:
                    sendtime++;
                    if(sendtime>0){
                        ET_print.getText().append("\n"+"*****Sent"+sendtime*512+"Byte*****");
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    }
                    break;
                case 93:
                    ET_print.getText().append("\n"+"*****finish sending*****");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    break;
                case 1:
                    ET_print.getText().append("\n"+"*****connected to server success*****");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    break;
                case 2:

                    ET_print.getText().append("\n"+"*****************");
                    ET_print.getText().append("\n"+"fail to connect to server");
                    ET_print.getText().append("\n"+"*****************");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    break;
                case 3:
                    ET_print.getText().append("\n"+"server:"+reviceMgs.toString());
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    break;
                case 5:
                    ET_print.getText().append("\n"+"*****************");
                    ET_print.getText().append("\n"+"fail to connect to server");
                    ET_print.getText().append("\n"+"*****************");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    break;
                case 99:

                    ET_print.getText().append("\n"+"*****************");
                    ET_print.getText().append("\n"+"choose the file:"+file_path);
                    ET_print.getText().append("\n"+"*****************");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部

                    final AlertDialog.Builder normalDialog2 =
                            new AlertDialog.Builder(ClientActivity.this);
                    normalDialog2.setTitle("hint");
                    normalDialog2.setMessage("yes/no to send the file:\n"+file_path);
                    normalDialog2.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(socket==null){
                                        Toast.makeText(getApplicationContext(),"setup the connection first！", Toast.LENGTH_SHORT).show();

                                    }else{

                                            if(sendfile(socket)){
                                                ET_print.getText().append("\n"+"*****Sending file success*****");
                                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                                            }else {
                                                ET_print.getText().append("\n"+"*****Sending file fail*****");
                                                Toast.makeText(getApplicationContext(),"Sending file fail！", Toast.LENGTH_SHORT).show();
                                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                                            }

                                    }
                                }
                            });
                    normalDialog2.setNegativeButton("cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    // 显示
                    normalDialog2.show();

                    break;
            }

        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        switch (requestCode) {
            case 100:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    //String path = FileUtils.getPath(this, uri);
                    file_path=getFileAbsolutePath(ClientActivity.this,data.getData());

                    handler.sendEmptyMessage(99);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 根据Uri获取文件的绝对路径，解决Android4.4以上版本Uri转换
     *
     * @param fileUri
     */

    public static String getFileAbsolutePath(Activity context, Uri fileUri) {
        if (context == null || fileUri == null)
            return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, fileUri)) {
            if (isExternalStorageDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(fileUri)) {
                String id = DocumentsContract.getDocumentId(fileUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(fileUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(fileUri))
                return fileUri.getLastPathSegment();
            return getDataColumn(context, fileUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(fileUri.getScheme())) {
            return fileUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}

//
//class FileUtils {
//    public static String getPath(Context context, Uri uri) {
//
//        if ("content".equalsIgnoreCase(uri.getScheme())) {
//            String[] projection = { "_data" };
//            Cursor cursor = null;
//
//            try {
//                cursor = context.getContentResolver().query(uri, projection,null, null, null);
//                int column_index = cursor.getColumnIndexOrThrow("_data");
//                if (cursor.moveToFirst()) {
//                    return cursor.getString(column_index);
//                }
//            } catch (Exception e) {
//                // Eat it
//            }
//        }
//
//        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            return uri.getPath();
//        }
//
//        return null;
//    }
//}