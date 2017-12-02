package qrcodegenerator.serge.com.bluetoothspeaker;



import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.os.AsyncTask;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


public class DeviceService extends Service implements TextToSpeech.OnInitListener{

    String word = "";
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String finalMessage = "";


    public static final String
            ACTION_BLUETOOTH_BROADCAST = DeviceService.class.getName() + "BluetoothBroadcast";
    private static String TAG = DeviceService.class.getSimpleName();
    public boolean isRunning = false;
    private TextToSpeech tts;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        tts = new TextToSpeech(getApplicationContext(), this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if(isRunning){
            Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
            Thread.currentThread().interrupt();
            new ConnectBT().cancel(true);
        }

        //send message to finish
        Intent intent = new Intent(ACTION_BLUETOOTH_BROADCAST);
        intent.putExtra("message", "finished");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart");
        if(!isRunning){
            Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

            address = intent.getStringExtra("address");

            new ConnectBT().execute();
            isRunning = true;
        }
    }





    //bluettoth event
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                //speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private void speakOut(String str) {
        String say = "";
        switch (str){
            case "A" :
                say = "Hello World!";
                break;
            case "B" :
                say = "Good Morning!";
                break;
            case "AB" :
                say = "Good Afternoon!";
                break;
            case "C" :
                say = "Good Evening!";
                break;
            case "AC" :
                say = "Good Night!";
                break;
            case "BC" :
                say = "I need Water!";
                break;
            case "ABC" :
                say = "I need Air!";
                break;
            case "D" :
                say = "I need Company!";
                break;
            case "AD" :
                say = "I need Food!";
                break;
            case "BD" :
                say = "I need assistance!";
                break;
            case "ABD" :
                say = "I need a nurse!";
                break;
            case "CD" :
                say = "I need a doctor!";
                break;
            case "ACD" :
                say = "I need to pee!";
                break;
            case "BCD" :
                say = "I need to go to bathroom!";
                break;
            case "ABCD" :
                say = "Help!";
                break;
            default:
                say = "Sorry I don't know this command!";
        }

        Intent intent = new Intent(ACTION_BLUETOOTH_BROADCAST);
        intent.putExtra("message", say);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        tts.speak(say, TextToSpeech.QUEUE_FLUSH, null);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            //progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
                Log.d("TAG", "Message: "+ e.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                isBtConnected = false;

                //finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            //progress.dismiss();

            if(isBtConnected){
                //start reading data
                new Thread(new Runnable() {
                    public void run() {
                        // loop until the thread is interrupted
                        // loop until the thread is interrupted
                        while (true) {
                            // do something in the loop
                            receiveData();
                        }
                    }
                }).start();
            }


        }
    }

    private void receiveData()
    {
        if (btSocket!=null)
        {
            try
            {
                //btSocket.getOutputStream().write("0".toString().getBytes());
                if(btSocket.isConnected()){
                    byte[] buffer = new byte[1024];
                    int bytes;
                    InputStream inFromServer = btSocket.getInputStream();

                    bytes = inFromServer.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);


                    if(readMessage.contains("END")){
                        //cut last 2 characters , and S
                        if(finalMessage.length() >= 2){

                            String realMessage = finalMessage.substring(0, finalMessage.length() - 2);
                            String string = "";
                            String finStr = "";
                            List<String> myList = new ArrayList<String>(Arrays.asList(realMessage.split(",")));

                            for (int x = 0; x < myList.size(); x++){
                                string = string + myList.get(x);
                            }

                            char[] chars = string.toCharArray();
                            Set<Character> charSet = new LinkedHashSet<Character>();
                            for (char c : chars) {
                                charSet.add(c);
                            }

                            StringBuilder sb = new StringBuilder();
                            for (Character character : charSet) {
                                sb.append(character);
                            }

                            finStr = sb.toString().replace("S","").replace("END","");

                            speakOut(finStr.trim());
                            Log.d("TAG", "Message: "+ finStr.trim());
                            finalMessage = "";
                        }

                    }else{
                        finalMessage = finalMessage + readMessage;
                    }
                }

            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
}
