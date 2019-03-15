package bdprototypebt.darkbalrock.com.bdprototypebt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import bdprototypebt.darkbalrock.com.bdprototypebt.devices.*;
import bdprototypebt.darkbalrock.com.bdprototypebt.events.event;
import bdprototypebt.darkbalrock.com.bdprototypebt.events.eventsContract;
import bdprototypebt.darkbalrock.com.bdprototypebt.events.eventsDBHelper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    public static final Object[] DATA_LOCK = new Object[0];

    String devicesFile;

    TextView mStatusBlueTv, mPairedTv, mLogBT;
    Button OnBtn, OffBtn, DiscoverBtn, PairedBtn, BlockBtn, VerLogsBtn, verLogBTABtn, verLogDEVBtn;

    BluetoothAdapter mBlueAdapter;
    final ArrayAdapter<String> BTArrayAdapter = null;

    ImageView mBlueIv;

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instanciamos el intent y el BroadcastReceiver
        IntentFilter filtroBT = new IntentFilter();

        filtroBT.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filtroBT.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filtroBT.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filtroBT.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filtroBT.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        filtroBT.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filtroBT.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(mBR, filtroBT);

        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv = findViewById(R.id.pairedTv);
        mBlueIv = findViewById(R.id.bluetoothIv);
        mLogBT = findViewById(R.id.logTv);
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
            showToast("Bluetooth no está disponible en este dispositivo.");
            mStatusBlueTv.setText("Bluetooth no está disponible en este dispositivo.");
        }
        else{
            mStatusBlueTv.setText("Bluetooth está disponible.");
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
                    showToast("Iniciando Bluetooth...");
                    //intent to on Bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                else{
                    showToast("Bluetooth ya está iniciado!");
                }
            }
        });

        //discover bluetooth btn
        DiscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mBlueAdapter.isDiscovering()){
                    showToast("Haciendo tu dispositivo detectable...");
                    if(BTArrayAdapter != null) {
                        BTArrayAdapter.clear();
                    }
                    mBlueAdapter.startDiscovery();
                    registerReceiver(mBR, new IntentFilter(BluetoothDevice.ACTION_FOUND));
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
                    showToast("Apagando Bluetooth...");
                    mBlueIv.setImageResource(R.drawable.ic_action_off);
                }
                else{
                    showToast("Bluetooth ya está apagado.");
                }
            }
        });

        //get paired devices btn click
        PairedBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                devicesFile = "BT paired Devices";
                showToast("Obteniendo dispositivos emparejados...");
                mPairedTv.setMovementMethod(new ScrollingMovementMethod());
                int contador = 1;
                if(mBlueAdapter.isEnabled()){
                    mPairedTv.setText("Dispositivos Emparejados");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for(BluetoothDevice device: devices){
                        mPairedTv.append("\n--- Device: " + device.getName()+ " ---");
                        devicesFile += "\n--- Device: " + device.getName()+ " ---";
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
                        mPairedTv.append("\n--------------------");
                        devicesFile += "\n--------------------";
                        mPairedTv.append("\n");
                        devicesFile += "\n";

                        //Seteamos los devices para almacenar en la base de datos
                        device dev = new device();
                        dev.setId(contador);contador++;
                        dev.setTime(device.getName());
                        dev.setAddress(device.getAddress());
                        dev.setUUIDs(device.getUuids().toString());
                        dev.setContentDesc(String.valueOf(device.describeContents()));
                        dev.setTime(String.valueOf(Calendar.getInstance().getTime()));
                        dev.setBonded(String.valueOf(device.getBondState()));
                        dev.setHashCode(String.valueOf(device.hashCode()));

                        //Almacenamos la info del dispositivo en la BD
                        devicesDBHelper dbHelper = new devicesDBHelper(getApplicationContext());
                        Long result = dbHelper.saveDevice(dev);
                    }
                    boolean logger = writeLog(devicesFile, "devices.txt");
                }
                else{
                    //BT is off so can't get paired devices
                    showToast("Inicia Bluetooth para obtener los dispositivos emparejados.");
                }

            }
        });

        //block bluetooth device btn click
        BlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Acción en desarrollo!");
            }
        });

        //ver Logs
        VerLogsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intentLog = new Intent(MainActivity.this, LogsActivity.class);
                startActivity(intentLog);
                setContentView(R.layout.consulta_logs);
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
        Writer out = null;
        try {
            synchronized (DATA_LOCK){
                    if(file != null){
                        file.createNewFile();
                        if(fileName == "devices.txt"){
                            out = new BufferedWriter(new FileWriter(file, false), 1024);
                        }
                        if(fileName == "BluetoothAdapter.txt"){
                            out = new BufferedWriter(new FileWriter(file, true), 1024);
                        }
                        out.write(toWrite);
                        out.close();
                        result = true;
                }
            }
        }
        catch (FileNotFoundException e) {
            Log.e("Main activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Main activity", "Can not read file: " + e.toString());
        }
        return result;
    }

    //BroadcastReceiver
    private final BroadcastReceiver mBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            String evento = "";
            final Uri uriData = intent.getData();
            event evt = new event();
            evt.setTime(String.valueOf(Calendar.getInstance().getTime()));
            evt.setUriData(uriData.toString());
            evt.setHost(uriData.getHost());
            evt.setPath(uriData.getPath());
            evt.setQuery(uriData.getQuery());
            evt.setScheme(uriData.getScheme());
            evt.setPort(String.valueOf(uriData.getPort()));
            evt.setUserInfo(uriData.getUserInfo());
            evt.setHashCode(String.valueOf(uriData.hashCode()));
            eventsDBHelper dbHelper = new eventsDBHelper(getApplicationContext());
            String log = "", showToastLog = "";
            boolean logger;
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)
                    ||action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                        ||action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                            ||action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (estado) {
                    case BluetoothAdapter.STATE_OFF:
                        evento = "BluetoothAdapter.STATE_OFF";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        evento = "BluetoothAdapter.STATE_ON";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        evento = "BluetoothAdapter.STATE_TURNING_OFF";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        evento = "BluetoothAdapter.STATE_TURNING_ON";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        evento = "BluetoothAdapter.STATE_CONNECTING";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        evento = "BluetoothAdapter.STATE_CONNECTED";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTING:
                        evento = "BluetoothAdapter.STATE_DISCONNECTING";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED:
                        evento = "BluetoothAdapter.STATE_DISCONNECTED";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        evento = "BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        evento = "BluetoothAdapter.SCAN_MODE_CONNECTABLE";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        evento = "BluetoothAdapter.SCAN_MODE_NONE";
                        log += logAdapter(uriData,evento);
                        break;
                    default:
                        break;
                };
                logger = writeLog(log, "BluetoothAdapter.txt");
                evt.setEventLog(evento);
                dbHelper.saveEvent(evt);
            }
            if(action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                evento = "BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED :" + estado;
                log += logAdapter(uriData, evento);
                evt.setEventLog(evento);
                dbHelper.saveEvent(evt);
                logger = writeLog(log, "BluetoothAdapter.txt");
                showToast(showToastLog);
            }
            if(action.equals(BluetoothAdapter.ERROR)){
                log += String.valueOf(BluetoothAdapter.ERROR);
                evento = "BluetoothAdapter.ERROR";
                log += logAdapter(uriData,evento);
                evt.setEventLog(evento);
                dbHelper.saveEvent(evt);
                showToastLog = "BluetoothAdapter.ERROR";
                logger = writeLog(log, "BluetoothAdapter.txt");
                showToast(showToastLog);
            }
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                BTArrayAdapter.add(device.getName()+"\n"+device.getAddress()+"\n"+String.valueOf(rssi));
                BTArrayAdapter.notifyDataSetChanged();
                evento = "BluetoothDevice.ACTION_FOUND";
                log += logAdapter(uriData,evento);
                evt.setEventLog(evento);
                dbHelper.saveEvent(evt);
                showToastLog = "BluetoothAdapter.ACTION_FOUND";
            }
            if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)
                    || action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                log += logAdapter(uriData,"BluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_ACL_CONNECTED+":"+BluetoothDevice.ACTION_ACL_DISCONNECTED);
                evento = "BluetoothDevice.ACTION_FOUND";
                evt.setEventLog(evento);
                dbHelper.saveEvent(evt);
                showToastLog = "BluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_ACL_CONNECTED+":"+BluetoothDevice.ACTION_ACL_DISCONNECTED;
            }
        }
    };

    public String logAdapter(Uri uriData, String state){
        String log = "";
        if(uriData!=null){
            try {
                String host = uriData.getHost();
                String path = uriData.getPath();
                String query = uriData.getQuery();
                String scheme = uriData.getScheme();
                int port = uriData.getPort();
                String uInfo = uriData.getUserInfo();
                int hashCode = uriData.hashCode();
                log += "\n-----" + state + "-----";
                log += "\n Time : " + String.valueOf(Calendar.getInstance().getTime());
                log += "\n uriData : " + uriData;
                log += "\n Host : " + host;
                log += "\n Path : " + path;
                log += "\n Query : " + query;
                log += "\n Scheme : " + scheme;
                log += "\n Port : " + port;
                log += "\n User Info : " + uInfo;
                log += "\n Hash Code : " + hashCode;
                log += "\n ---------------------------- \n";
            }catch(Exception e){
                System.out.println(e);
            }
        }else{
            log += "\n-----" + state + "-----";
            log += "\n Time : " + String.valueOf(Calendar.getInstance().getTime());
            log += "\n uriData : " + uriData;
            log += "\n ---------------------------- \n";
        }
        return log;
    }

    public ArrayList getDevices(){
        ArrayList<device> devicesBT = new ArrayList();
        String tabla = devicesContract.deviceEntry.tableName,
                selection = null,
                groupBy = null,
                having = null,
                orderBy = null;
        String[] columnas = null, selectionArgs = null;
        devicesDBHelper dbHelper = new devicesDBHelper(getApplicationContext());
        Cursor d = dbHelper.getDevice(tabla,columnas,selection,selectionArgs,groupBy,having,orderBy);
        while(d.moveToNext()){
            device dev = new device();
            dev.setId(d.getInt(d.getColumnIndex(devicesContract.deviceEntry.ID)));
            dev.setName(d.getString(d.getColumnIndex(devicesContract.deviceEntry.name)));
            dev.setAddress(d.getString(d.getColumnIndex(devicesContract.deviceEntry.address)));
            dev.setTime(d.getString(d.getColumnIndex(devicesContract.deviceEntry.time)));
            dev.setContentDesc(d.getString(d.getColumnIndex(devicesContract.deviceEntry.contentDesc)));
            dev.setBonded(d.getString(d.getColumnIndex(devicesContract.deviceEntry.bonded)));
            dev.setUUIDs(d.getString(d.getColumnIndex(devicesContract.deviceEntry.UUIDs)));
            dev.setHashCode(d.getString(d.getColumnIndex(devicesContract.deviceEntry.hashCode)));
            devicesBT.add(dev);
        }
        return devicesBT;
    }

    public ArrayList getEvents(){
        ArrayList<event> eventsBT = new ArrayList();
        String tabla = eventsContract.eventEntry.tableName,
                selection = null,
                groupBy = null,
                having = null,
                orderBy = null;
        String[] columnas = null, selectionArgs = null;
        eventsDBHelper dbHelper = new eventsDBHelper(getApplicationContext());
        Cursor d = dbHelper.getEvent(tabla,columnas,selection,selectionArgs,groupBy,having,orderBy);
        while(d.moveToNext()){
            event evt = new event();
            evt.setId(d.getInt(d.getColumnIndex(eventsContract.eventEntry.id)));
            evt.setEventLog(d.getString(d.getColumnIndex(eventsContract.eventEntry.eventLog)));
            evt.setTime(d.getString(d.getColumnIndex(eventsContract.eventEntry.Time)));
            evt.setUriData(d.getString(d.getColumnIndex(eventsContract.eventEntry.uriData)));
            evt.setHost(d.getString(d.getColumnIndex(eventsContract.eventEntry.Host)));
            evt.setPath(d.getString(d.getColumnIndex(eventsContract.eventEntry.Path)));
            evt.setQuery(d.getString(d.getColumnIndex(eventsContract.eventEntry.Query)));
            evt.setScheme(d.getString(d.getColumnIndex(eventsContract.eventEntry.Scheme)));
            evt.setPort(d.getString(d.getColumnIndex(eventsContract.eventEntry.Port)));
            evt.setUserInfo(d.getString(d.getColumnIndex(eventsContract.eventEntry.userInfo)));
            evt.setHashCode(d.getString(d.getColumnIndex(eventsContract.eventEntry.hashCode)));
            eventsBT.add(evt);
        }
        return eventsBT;
    }
}
