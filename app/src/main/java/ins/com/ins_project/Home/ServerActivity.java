package ins.com.ins_project.Home;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import ins.com.ins_project.R;

public class ServerActivity extends Activity {
    Button open_wifi,open_server,send;
    TextView text_path;
    EditText ET_print,ET_input;
    ScrollView scrollView;
    private WifiManager wifiManager;
    public static ServerActivity mainactivity;
    Socket socket = null;
    private static final int PORT = 9999;
    private List<Socket> mList = new ArrayList<Socket>();
    private ServerSocket server = null;
    private ExecutorService mExecutorService = null; //thread pool
    InputStream in;
    String reviceMgs=null;
    String reviceName=null;
    File dir=null;
    long reciveTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        mainactivity=this;
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        open_wifi= (Button) findViewById(R.id.server_open_wifi);
        open_server= (Button) findViewById(R.id.server_open_server);
        send= (Button) findViewById(R.id.send);
        scrollView= (ScrollView) findViewById(R.id.server_scrollView);

        open_wifi.setOnClickListener(cccc);
        open_server.setOnClickListener(cccc);
        send.setOnClickListener(cccc);

        text_path= (TextView) findViewById(R.id.path);
        ET_print= (EditText) findViewById(R.id.print);
        ET_input= (EditText) findViewById(R.id.input);

        if(isApOn()){
            open_wifi.setText("Turn on wifi hotspot(ON)");
        }else{
            open_wifi.setText("Turn on wifi hotspot(OFF)");
        }

    }

    View.OnClickListener cccc = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.server_open_wifi:
                    if(isApOn()){
                        final AlertDialog.Builder normalDialog =
                                new AlertDialog.Builder(mainactivity);
                        normalDialog.setTitle("Attention");
                        normalDialog.setMessage("Wifi hotspot is ON，turn off?");
                        normalDialog.setPositiveButton("Confirm",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(stratWifiAp("wifisocket","00000000",false)){
                                            Toast.makeText(getApplicationContext(),"Hotspot is OFF!", Toast.LENGTH_SHORT).show();
                                            open_wifi.setText("Turn on wifi hotspot(OFF)");
                                        }else{
                                            Toast.makeText(getApplicationContext(),"unable to turn on Hotspot!", Toast.LENGTH_SHORT).show();

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
                    }else{
                        if(true){
                            Toast.makeText(getApplicationContext(),"wifi Hotspot On!", Toast.LENGTH_SHORT).show();
                            open_wifi.setText("Turn on wifi Hotspot(ON)");
                        }else{
                            Toast.makeText(getApplicationContext(),"unable to turn on wifi!", Toast.LENGTH_SHORT).show();

                        }

                    }
                    break;
                case R.id.server_open_server:
                    if(true){

                        startServer();


                    }else{
                        final AlertDialog.Builder normalDialog =
                                new AlertDialog.Builder(mainactivity);
                        normalDialog.setTitle("Attention");
                        normalDialog.setMessage("Wifi hotspot is OFF，turn it ON！");
                        normalDialog.setPositiveButton("Confirm",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(stratWifiAp("wifisocket","00000000",true)){
                                            Toast.makeText(getApplicationContext(),"turn on wifi hotspot success!", Toast.LENGTH_SHORT).show();
                                            open_wifi.setText("Turn wifi hotspot on(ON)");
                                        }else{
                                            Toast.makeText(getApplicationContext(),"unable to turn on wifi hotspot!", Toast.LENGTH_SHORT).show();

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
                    }
                    break;
                case R.id.send:
                    if(socket==null){
                        Toast.makeText(getApplicationContext(),"Please set up connection first！", Toast.LENGTH_SHORT).show();

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
                            ET_print.getText().append("\n"+"Server:"+input);
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                        }else {
                            ET_print.getText().append("\n"+"*****enable to send data*****");
                            Toast.makeText(getApplicationContext(),"Sending fail！", Toast.LENGTH_SHORT).show();
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                        }

                    }else{
                        Toast.makeText(getApplicationContext(),"Empty！", Toast.LENGTH_SHORT).show();
                    }
                    }
                    ET_input.setText("");
                    break;
            }
        }
    };


    /**
     * 判断WIFI热点是否开启
     */
    public boolean isApOn() {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        }
        catch (Throwable ignored) {}
        return false;
    }
    /**
     * 设置热点名称及密码，并创建热点
     * @param mSSID
     * @param mPasswd
     */
    private Boolean stratWifiAp(String mSSID, String mPasswd, Boolean onoff) {
        Method method1 = null;
        if(onoff){
            wifiManager.setWifiEnabled(false);
        }
       try {
            //通过反射机制打开热点
            method1 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            WifiConfiguration netConfig = new WifiConfiguration();
            netConfig.SSID = mSSID;
            netConfig.preSharedKey = mPasswd;
            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            return (Boolean)method1.invoke(wifiManager, netConfig, onoff);

        } catch (Exception e) {
           return false;
       }
    }
    /**
     * 选择路径回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor.getString(actual_image_column_index);
            File file = new File(img_path);
            Toast.makeText(ServerActivity.this, file.toString(), Toast.LENGTH_SHORT).show();
        }
    }

public void startServer(){
    Thread thread = new Thread(){
        @Override
        public void run() {
            super.run();
                /*指明服务器端的端口号*/
            try {
                server = new ServerSocket(9999);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true){

                try {
                    socket = server.accept();
                    in  = socket.getInputStream();
                    handler.sendEmptyMessage(2);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new ServerThread(socket,in).start();

            }
        }
    };
    thread.start();
}

    class ServerThread extends Thread {

        private Socket socket;
        private InputStream inputStream;


        public ServerThread(Socket socket, InputStream inputStream){
            this.socket = socket;
            this.inputStream = inputStream;
        }
        @Override
        public void run() {
            byte[]mode=new byte[13];//对话0  文件1

            while (inputStream!= null) {
                for(int i=0;i<13;i++){
                    try {
                        mode[i]=(byte)inputStream.read();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                    }
                }
                String modestr =new String(mode);
                //传送的数据模式 0 对话  1 文件
                try {
                    int modeCode = Integer.parseInt(modestr.substring(0, 1), 10);
                    //对话或文件大小
                    long inLength = Long.parseLong(modestr.substring(1, 11), 10);
                    //如果是文件的话文件名长度
                    int nameLength = Integer.parseInt(modestr.substring(11, 13), 10);
                    byte[] fileName = new byte[nameLength];
                    byte[] mgs = new byte[new Long(inLength).intValue()];

                    int inTime=(int)(inLength/512);
                    int inTimeResidue=(int)(inLength%512);

                    if (modeCode == 0) {
                        for (int i = 0; i < inLength; i++) {
                            try {
                                mgs[i] = (byte) inputStream.read();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        reviceMgs = new String(mgs,"UTF-8");
                        Log.e("Data received", reviceMgs.toString());
                        handler.sendEmptyMessage(1);
                        //  ET_print.getText().append("\n"+new String(mgs));


                    } else if (modeCode == 1) {
                        for (int i = 0; i < nameLength; i++) {
                            try {
                                fileName[i] = (byte) inputStream.read();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        reviceName = new String(fileName,"UTF-8");
                        Log.e("Data", reviceName.toString());
                     //   handler.sendEmptyMessage(1);
                         dir = new File("/mnt/sdcard/WifiSocketDownload/"+reviceName);
                        File file = new File("/mnt/sdcard/WifiSocketDownload");
                        if (!file.exists()) {
                            try {
                                //按照指定的路径创建文件夹
                                file.mkdirs();
                                Log.e("创建文件夹成功",file.toString());
                            } catch (Exception e) {
                                // TODO: handle exception
                                Log.e("创建文件夹失败","创建文件夹失败");
                            }
                        }
                        if (!dir.exists()) {
                            try {
                                //在指定的文件夹中创建文件
                                dir.createNewFile();
                                Log.e("创建文件成功",dir.toString());
                            } catch (Exception e) {
                                Log.e("创建文件失败","创建文件失败");
                            }
                        }
                    if(inTime>=0&&inTimeResidue>0) {
                        byte[] buffer = new byte[512];
                        reciveTime= 0;
                        for (int i = 0; i < inTime + 1; i++) {
                            for (int k = 0; k < 512; k++) {
                                try {
                                    buffer[k] = (byte) inputStream.read();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            writeFileSdcard(dir, buffer);
                            handler.sendEmptyMessage(99);

                        }
//                        if(inTimeResidue>0){
//                            byte []buffer2=new byte[inTimeResidue];
//                            for (int k = 0; k < inTimeResidue; k++) {
//                                try {
//                                    buffer2[k] = (byte) inputStream.read();
//                                } catch (IOException e) {
//                                    // TODO Auto-generated catch block
//                                    e.printStackTrace();
//                                }
//                            }
//                            writeFileSdcard(dir,buffer2);
//                        }
                    }else if(inTime>=0&&inTimeResidue==0){

                        byte[] buffer = new byte[512];
                        reciveTime= 0;
                        for (int i = 0; i < inTime ; i++) {
                            for (int k = 0; k < 512; k++) {
                                try {
                                    buffer[k] = (byte) inputStream.read();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            writeFileSdcard(dir, buffer);
                            handler.sendEmptyMessage(99);

                        }
                    }
//                    else if(inTime<1&&inTimeResidue>0){
//                        byte []buffer2=new byte[inTimeResidue];
//                        for (int k = 0; k < inTimeResidue; k++) {
//                            try {
//                                buffer2[k] = (byte) inputStream.read();
//                            } catch (IOException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }
//                        writeFileSdcard(dir,buffer2);
//                    }
                        handler.sendEmptyMessage(98);
                    }

                }catch (Exception e){

                }
            }
        }
    }


    public void writeFileSdcard(File fileName, byte []wb){
        try{
            //FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
            FileOutputStream fout = new FileOutputStream(fileName,true);
            fout.write(wb);
            fout.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case 1:
                    ET_print.getText().append("\n"+"client side:"+reviceMgs.toString());
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    break;
                case 2:
                    ET_print.getText().append("\n"+"*****client connects successfully*****");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    break;
                case 98:
                    ET_print.getText().append("\n"+"*******************");
                    ET_print.getText().append("\n"+"receive file successfully:\n"+dir.toString());
                    ET_print.getText().append("\n"+"*******************");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    break;
                case 99:
                    reciveTime++;
                    if(reciveTime>0){
                        ET_print.getText().append("\n"+"*****received"+reciveTime*512+"Byte*****");
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                    }

                    break;

            }

        }
    };

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
            Log.e("aa","bit of context"+ Arrays.toString(sendByte));
            out.write(sendByte);
            System.out.println("Writing data");
            return true;
            //  out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in writing data");
            return false;
        }
    }

    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;

    }
//    /**
//     * 循环遍历客户端集合，给每个客户端都发送信息。
//     */
//    public void sendmsg(String msg) {
//        System.out.println(msg);
//        int num =mList.size();
//        for (int index = 0; index < num; index ++) {
//            Socket mSocket = mList.get(index);
//            PrintWriter pout = null;
//            try {
//                pout = new PrintWriter(new BufferedWriter(
//                        new OutputStreamWriter(mSocket.getOutputStream())),true);
//                pout.println(msg);
//            }catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }



    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }




}
