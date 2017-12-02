package qrcodegenerator.serge.com.bluetoothspeaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnDis;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    TextView multiline;

    public static final String ACTION_MAIN_BROADCAST = MainActivity.class.getName() + "MainBroadcast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //Intent intent = new Intent(ACTION_MAIN_BROADCAST);
        //intent.putExtra("address", address);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


        setContentView(R.layout.activity_main);

        btnDis = (Button) findViewById(R.id.btnDisconnect);
        multiline = (TextView) findViewById(R.id.txtMultiLine);

        Intent serviceIntent = new Intent(MainActivity.this, DeviceService.class);
        serviceIntent.putExtra("address", address);
        this.startService(serviceIntent);

        //new ConnectBT().execute(); //Call the class to connect
        startService();

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   //Disconnect();
                stopService();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        String message = intent.getStringExtra("message");

                        if(message.length() > 0){
                            if(message.equals("finished")){
                                //finish();
                                System.exit(1);
                            }else{
                                multiline.setText(message);
                            }
                        }

                    }
                }, new IntentFilter(DeviceService.ACTION_BLUETOOTH_BROADCAST)
        );
    }

    // Method to start the service
    public void startService() {
        startService(new Intent(getBaseContext(), DeviceService.class));
    }

    // Method to stop the service
    public void stopService() {
        stopService(new Intent(getBaseContext(), DeviceService.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
