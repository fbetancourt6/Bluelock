package bdprototypebt.darkbalrock.com.bdprototypebt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Set;

import bdprototypebt.darkbalrock.com.bdprototypebt.Retrofit.IMyService;
import bdprototypebt.darkbalrock.com.bdprototypebt.Retrofit.RetrofitClient;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    public static final Object[] DATA_LOCK = new Object[0];

    String devicesFile;

    TextView mStatusBlueTv, mPairedTv;
    Button OnBtn, OffBtn, DiscoverBtn, PairedBtn, BlockBtn, VerLogsBtn, verLogBTABtn, verLogDEVBtn;

    BluetoothAdapter mBlueAdapter;

    ImageView mBlueIv;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService iMyService;

    @Override
    protected void onStop(){
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init service
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iMyService = retrofitClient.create(IMyService.class);

        //Instanciamos el intent y el BroadcastReceiver
        IntentFilter filtroBT = new IntentFilter();
        filtroBT.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filtroBT.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filtroBT.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filtroBT.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filtroBT.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filtroBT.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(mBR, filtroBT);

        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv = findViewById(R.id.pairedTv);
        mBlueIv = findViewById(R.id.bluetoothIv);
        OnBtn = findViewById(R.id.onBtn);
        OffBtn = findViewById(R.id.offBtn);
        DiscoverBtn = findViewById(R.id.discoverableBtn);
        PairedBtn = findViewById(R.id.pairedBtn);
        BlockBtn = findViewById(R.id.blockBtn);
        VerLogsBtn = findViewById(R.id.verLogsBtn);
        verLogBTABtn = findViewById(R.id.verLogBTABtn);
        verLogDEVBtn = findViewById(R.id.verLogDEVBtn);

        //adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        //check if bt is available or not
        if(mBlueAdapter == null){
            mStatusBlueTv.setText("Bluetooth is not available.");
        }
        else{
            mStatusBlueTv.setText("Bluetooth is available.");
        }

        //set image according to the bluetooth status on/off
        if(mBlueAdapter.isEnabled()){
            mBlueIv.setImageResource(R.drawable.ic_action_on);
        }
        else{
            mBlueIv.setImageResource(R.drawable.ic_action_off);
        }

        //on btn click
        OnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mBlueAdapter.isEnabled()){
                    showToast("Turning on Bluetooth...");
                    //intent to on Bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                else{
                    showToast("Bluetooth is already on!");
                }
            }
        });

        //discover bluetooth btn
        DiscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mBlueAdapter.isDiscovering()){
                    showToast("Making your device discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });

        //off bluetooth btn click
        OffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBlueAdapter.isEnabled()){
                    mBlueAdapter.disable();
                    showToast("Turning Bluetooth of");
                    mBlueIv.setImageResource(R.drawable.ic_action_off);
                }
                else{
                    showToast("Bluetooth is already off");
                }
            }
        });

        //ver Logs
        VerLogsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });

        //get paired devices btn click
        PairedBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                devicesFile = "BT paired Devices";
                showToast("getting paired devices...");
                if(mBlueAdapter.isEnabled()){
                    mPairedTv.setText("Paired Devices");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for(BluetoothDevice device: devices){
                        mPairedTv.append("\n--- Device: " + device.getName()+ " ---");
                        devicesFile += "\n--- Device: " + device.getName()+ " ---";
                        mPairedTv.append("\nName : " + device.getName());
                        devicesFile += "\nName : " + device.getName();
                        mPairedTv.append("\nAddress : " + device.getAddress());
                        devicesFile += "\nAddress : " + device.getAddress();
                        mPairedTv.append("\nString : " + device.toString());
                        devicesFile += "\nString : " + device.toString();
                        mPairedTv.append("\nUuids : " + device.getUuids());
                        devicesFile += "\nUuids : " + device.getUuids();
                        mPairedTv.append("\nContents : " + device.describeContents());
                        devicesFile += "\nContents : " + device.describeContents();
                        mPairedTv.append("\nTime : " + Calendar.getInstance().getTime());
                        devicesFile += "\nTime : " + Calendar.getInstance().getTime();
                        mPairedTv.append("\nBondState : " + device.getBondState());
                        devicesFile += "\nBondState : " + device.getBondState();
                        mPairedTv.append("\nhashCode : " + device.hashCode());
                        devicesFile += "\nhashCode : " + device.hashCode();
                        mPairedTv.append("\nEvento : " + "Consulta de Dispositivos enlazados.");
                        devicesFile += "\nEvento : " + "Consulta de Dispositivos enlazados.";
                        mPairedTv.append("\n");
                        devicesFile += "\n";
                        /*GATT connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback)*/

                        /*compositeDisposable.add(iMyService.saveDevices(device.getName(),device.getAddress())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<String>() {
                                    @Override
                                    public void accept(String response) throws Exception {
                                        Toast.makeText(MainActivity.this,""+response, Toast.LENGTH_SHORT).show();
                                    }
                                }));*/
                    }
                    boolean logger = writeLog(devicesFile, "devices.txt");
                }
                else{
                    //BT is off so can't get paired devices
                    showToast("Turn on Bluetooth to get paired devices");
                }

            }
        });

        //off bluetooth btn click
        BlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Acción en desarrollo!");
            }
        });
    }



    //Eliminamos el BroadcastReceiver al finalizar
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mBR);
    }

    //toast message function
    private void showToast(String msg){
        Toast.makeText( this, msg, Toast.LENGTH_SHORT).show();
    }

    //Escribe en el Log de app
    public boolean writeLog(String toWrite, String fileName){
        Context context = getBaseContext();
        boolean result = false;
        File path = context.getExternalFilesDir(null);
        File file = new File(path, fileName);
        try {
            synchronized (DATA_LOCK){
                if(file != null && file.canWrite()){
                    file.createNewFile();
                    Writer out = new BufferedWriter(new FileWriter(file, true), 1024);
                    out.write(toWrite);
                    out.close();
                    result = true;
                }
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return result;
    }

    //BroadcastReceiver
    private final BroadcastReceiver mBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            String log = "", showToastLog = "";
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (estado) {
                    case BluetoothAdapter.STATE_OFF:
                        log += "\nBluetoothAdapter.STATE_OFF";
                        showToastLog = "BluetoothAdapter.STATE_OFF";
                        break;
                    case BluetoothAdapter.STATE_ON:
                        log += "\nBluetoothAdapter.STATE_ON";
                        showToastLog = "BluetoothAdapter.STATE_ON";
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        log += "\nBluetoothAdapter.STATE_TURNING_OFF";
                        showToastLog = "BluetoothAdapter.STATE_TURNING_OFF";
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        log += "\nBluetoothAdapter.STATE_TURNING_ON";
                        showToastLog = "BluetoothAdapter.STATE_TURNING_ON";
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        log += "\nBluetoothAdapter.STATE_CONNECTING";
                        showToastLog = "BluetoothAdapter.STATE_CONNECTING";
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        log += "\nBluetoothAdapter.STATE_CONNECTED";
                        showToastLog = "BluetoothAdapter.STATE_CONNECTED";
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTING:
                        log += "\nBluetoothAdapter.STATE_DISCONNECTING";
                        showToastLog = "BluetoothAdapter.STATE_DISCONNECTING";
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED:
                        log += "\nBluetoothAdapter.STATE_DISCONNECTED";
                        showToastLog = "BluetoothAdapter.STATE_DISCONNECTED";
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        log += "\nBluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE";
                        showToastLog = "BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE";
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        log += "\nBluetoothAdapter.SCAN_MODE_CONNECTABLE";
                        showToastLog = "BluetoothAdapter.SCAN_MODE_CONNECTABLE";
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        log += "\nBluetoothAdapter.SCAN_MODE_NONE";
                        showToastLog = "BluetoothAdapter.SCAN_MODE_NONE";
                        break;
                    default:
                        break;
                }
                showToast(showToastLog);
                boolean logger = writeLog(log, "BluetoothAdapter.txt");
            }
            if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) || action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                log += "\nBluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_ACL_CONNECTED+":"+BluetoothDevice.ACTION_ACL_DISCONNECTED;
                showToastLog = "BluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_ACL_CONNECTED+":"+BluetoothDevice.ACTION_ACL_DISCONNECTED;
                showToast(showToastLog);
                boolean logger = writeLog(log, "BluetoothAdapter.txt");
            }
        }
    };


}
