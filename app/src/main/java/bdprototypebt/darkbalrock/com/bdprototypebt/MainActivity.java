package bdprototypebt.darkbalrock.com.bdprototypebt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

import bdprototypebt.darkbalrock.com.bdprototypebt.GattServer.BTGattServer;
import bdprototypebt.darkbalrock.com.bdprototypebt.devices.device;
import bdprototypebt.darkbalrock.com.bdprototypebt.devices.devicesContract;
import bdprototypebt.darkbalrock.com.bdprototypebt.devices.devicesDBHelper;
import bdprototypebt.darkbalrock.com.bdprototypebt.devices.dispositivos;
import bdprototypebt.darkbalrock.com.bdprototypebt.events.event;
import bdprototypebt.darkbalrock.com.bdprototypebt.events.eventsContract;
import bdprototypebt.darkbalrock.com.bdprototypebt.events.eventsDBHelper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    public static final Object[] DATA_LOCK = new Object[0];

    String devicesFile;

    TextView mStatusBlueTv, mPairedTv, mLogBT;
    Button OnBtn, OffBtn, DiscoverBtn, PairedBtn, BlockBtn, VerLogsBtn, verLogBTABtn, gattBtn;

    BluetoothAdapter mBlueAdapter;
    //ArrayAdapter<String> BTArrayAdapter;

    ImageView mBlueIv;
    private BluetoothSocket bTSocket;

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
        gattBtn = findViewById(R.id.gattBtn);

        String[] mDevices = {
                "Zero"
        };

        //adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        //BTArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_main, mDevices);

        //check if bt is available or not
        if(mBlueAdapter == null){
            showToast("Bluetooth no está disponible en este dispositivo.");
            mStatusBlueTv.setText("Bluetooth no está disponible en este dispositivo.");
        }
        else{
            mStatusBlueTv.setText("Bluetooth disponible.");
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
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                    mStatusBlueTv.setText("Bluetooth iniciado.");
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
                    mBlueAdapter.startDiscovery();
                    registerReceiver(mBR, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                    mStatusBlueTv.setText("Bluetooth detectable.");
                }else{
                    showToast("Detección en curso...");
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
                    mStatusBlueTv.setText("Bluetooth Apagado.");
                }
                else{
                    showToast("Bluetooth ya está apagado.");
                }
            }
        });

        //get paired devices btn click
        PairedBtn.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                devicesFile = "BT paired Devices";
                showToast("Obteniendo dispositivos emparejados...");
                mPairedTv.setMovementMethod(new ScrollingMovementMethod());
                int contador = 1;
                if(mBlueAdapter.isEnabled()){
                    if (((int)Build.VERSION.SDK_INT) >= 24) {
                        mPairedTv.setText(Html.fromHtml("Dispositivos Emparejados", Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                    } else {
                        mPairedTv.setText(Html.fromHtml("Dispositivos Emparejados"));
                    }
                    //mPairedTv.setText(Html.fromHtml("Dispositivos Emparejados", Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for(BluetoothDevice device: devices){
                        mPairedTv.append("\n--- Device: " + device.getName()+ " ---");
                        devicesFile += "\n--- Device: " + device.getName()+ " ---";
                        mPairedTv.append("\nAddress : " + device.getAddress()+ " ---");
                        devicesFile += "\nAddress : " + device.getAddress()+ " ---";
                        mPairedTv.append("\nString : " + device.toString()+ " ---");
                        devicesFile += "\nString : " + device.toString()+ " ---";
                        mPairedTv.append("\nUuids : " + device.getUuids()+ " ---");
                        devicesFile += "\nUuids : " + device.getUuids()+ " ---";
                        mPairedTv.append("\nContents : " + device.describeContents()+ " ---");
                        devicesFile += "\nContents : " + device.describeContents()+ " ---";
                        mPairedTv.append("\nTime : " + Calendar.getInstance().getTime()+ " ---");
                        devicesFile += "\nTime : " + Calendar.getInstance().getTime()+ " ---";
                        mPairedTv.append("\nBondState : " + device.getBondState()+ " ---");
                        devicesFile += "\nBondState : " + device.getBondState()+ " ---";
                        mPairedTv.append("\nhashCode : " + device.hashCode()+ " ---");
                        devicesFile += "\nhashCode : " + device.hashCode()+ " ---";
                        mPairedTv.append("\n--------------------");
                        devicesFile += "\n--------------------";
                        mPairedTv.append("\n");
                        devicesFile += "\n";
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
                getDevicesForBlock();
                Intent intentDev = new Intent(MainActivity.this, dispositivos.class);
                startActivity(intentDev);
                //setContentView(R.layout.activity_dispositivos);

            }
        });

        //ver Logs
        VerLogsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intentLog = new Intent(MainActivity.this, LogsActivity.class);
                startActivity(intentLog);
                //setContentView(R.layout.consulta_logs);
            }
        });

        //GattServer
        gattBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentGatt = new Intent(MainActivity.this, BTGattServer.class);
                startActivity(intentGatt);
                //setContentView(R.layout.gatt_server);
            }
        });

    }

    //Eliminamos el BroadcastReceiver al finalizar
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mBR);
    }

    @Override
    public void onBackPressed() {}
    //toast message function
    private void showToast(String msg){
        Toast.makeText( this, msg, Toast.LENGTH_SHORT).show();
    }

    //Escribe en el Log de app
    public boolean writeLog(String toWrite, String fileName){
        Context context = getBaseContext();
        boolean result = false;
        toWrite += toWrite+"<b>";
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
            Log.e("Main activity", "Archivo no encontrado: " + e.toString());
        } catch (IOException e) {
            Log.e("Main activity", "No se ha podido leer el archivo: " + e.toString());
        }
        return result;
    }

    //BroadcastReceiver
    private final BroadcastReceiver mBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            String evento = "";
            boolean bloqueo = false;
            final Uri uriData = intent.getData();
            event evt = validaUri(uriData, action);

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
                        evento = "<font color='blue'>BluetoothAdapter.STATE_OFF";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        evento = "<font color='blue'>BluetoothAdapter.STATE_ON";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        evento = "<font color='purple'>BluetoothAdapter.STATE_TURNING_OFF";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        evento = "<font color='purple'>BluetoothAdapter.STATE_TURNING_ON";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        evento = "<font color='purple'>BluetoothAdapter.STATE_CONNECTING";
                        log += logAdapter(uriData,evento);
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        bloqueo = validaBloqueo(device.getAddress());
                        UUID uuid = UUID.randomUUID();
                        if(bloqueo){
                            try {
                                Log.w("ACTION_ACL_CONNECTED","Entró a removeBond");
                                Method m = device.getClass()
                                        .getMethod("removeBond", (Class[]) null);
                                m.invoke(device, (Object[]) null);

                                evento = "<font color='red'>Dispositivo Bloqueado ("+uuid+"): "+device.getAddress()+"; "+device.getName();
                                log += logAdapter(uriData,evento);
                                showToast("Dispositivo bloqueado ("+uuid+") : "+device.getAddress()+" : "+ device.getName());
                            } catch (Exception e) {
                                Log.w("ACTION_ACL_CONNECTED", e.getMessage());
                                evento = "<font color='red'>Error de Dispositivo Bloqueado ("+uuid+"): "+device.getAddress()+"; "+device.getName();
                            }
                            log += logAdapter(uriData,evento);
                        }
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        evento = "<font color='green'>BluetoothAdapter.STATE_CONNECTED";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTING:
                        evento = "<font color='green'>BluetoothAdapter.STATE_DISCONNECTING";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED:
                        evento = "<font color='purple'>BluetoothAdapter.STATE_DISCONNECTED";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        evento = "<font color='purple'>BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        evento = "<font color='purple'>BluetoothAdapter.SCAN_MODE_CONNECTABLE";
                        log += logAdapter(uriData,evento);
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        evento = "<font color='purple'>BluetoothAdapter.SCAN_MODE_NONE";
                        log += logAdapter(uriData,evento);
                        break;
                    default:
                        break;
                }
                logger = writeLog(log, "BluetoothAdapter.txt");
                evt.setEventLog(evento);
            }
            if(action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                evento = "<font color='red'>BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED :" + estado;
                log += logAdapter(uriData, evento);
                evt.setEventLog(evento);
                logger = writeLog(log, "BluetoothAdapter.txt");
                showToast(showToastLog);
            }
            if(action.equals(BluetoothAdapter.ERROR)){
                log += String.valueOf(BluetoothAdapter.ERROR);
                evento = "<font color='red'>BluetoothAdapter.ERROR";
                log += logAdapter(uriData,evento);
                evt.setEventLog(evento);
                showToastLog = "BluetoothAdapter.ERROR";
                logger = writeLog(log, "BluetoothAdapter.txt");
                showToast(showToastLog);
            }
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.w("ACTION_ACL_CONNECTED","Entró a ACTION_ACL_CONNECTED"+device.getName()+"\n"+device.getAddress()+"\n"+rssi);
                device dev = new device();
                dev.setId(0);
                dev.setName(device.getName());
                dev.setAddress(device.getAddress());
                dev.setContentDesc(String.valueOf(rssi));
                dev.setBloqueado(String.valueOf("bloqueado"));
                dev.setTime(String.valueOf(Calendar.getInstance().getTime()));
                dev.setBonded(String.valueOf(false));
                dev.setHashCode(String.valueOf(device.hashCode()));
                boolean result = guardaDispositivo(dev);
                evento = "<font color='green'>BluetoothDevice.ACTION_FOUND";
                log += logAdapter(uriData,evento);
                evt.setEventLog(evento);
                logger = writeLog(log, "BluetoothAdapter.txt");
                showToastLog = "BluetoothAdapter.ACTION_FOUND";
                showToast(showToastLog);
            }
            if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                Log.d("ACTION_ACL_CONNECTED","Entró a ACTION_ACL_CONNECTED");
                log += logAdapter(uriData,"BluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_ACL_CONNECTED);
                evento = "<font color='green'>BluetoothDevice.ACTION_ACL_CONNECTED";
                evt.setEventLog(evento);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bloqueo = validaBloqueo(device.getAddress());
                UUID uuid = UUID.randomUUID();
                if(bloqueo){
                    try {
                        Log.w("ACTION_ACL_CONNECTED","Entró a removeBond");
                        Method m = device.getClass()
                                    .getMethod("removeBond", (Class[]) null);
                        m.invoke(device, (Object[]) null);

                        evento = "<font color='red'>Dispositivo Bloqueado ("+uuid+"): "+device.getAddress()+"; "+device.getName();
                        log += logAdapter(uriData,evento);
                        showToast("Dispositivo bloqueado ("+uuid+") : "+device.getAddress()+" : "+ device.getName());
                    } catch (Exception e) {
                        Log.e("ACTION_ACL_CONNECTED", e.getMessage());
                        evento = "<font color='red'>Error de Dispositivo Bloqueado ("+uuid+"): "+device.getAddress()+"; "+device.getName();
                    }
                }else{
                    try {
                        Log.d("ACTION_ACL_CONNECTED", "Start Pairing...");
                        Method m = device.getClass()
                                .getMethod("createBond", (Class[]) null);
                        m.invoke(device, (Object[]) null);
                        Log.w("ACTION_ACL_CONNECTED", "Pairing finished.");
                        evento = "<font color='green'>Dispositivo Conectado ("+uuid+"): "+device.getAddress()+"; "+device.getName();
                        log += logAdapter(uriData,evento);
                    } catch (Exception e) {
                        Log.w("ACTION_ACL_CONNECTED", e.getMessage());
                        evento = "<font color='red'>Error Dispositivo Conectado ("+uuid+"): "+device.getAddress()+"; "+device.getName();
                    }
                }
                logger = writeLog(log, "BluetoothAdapter.txt");
                showToastLog = "BluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_ACL_CONNECTED;
                showToast(showToastLog);
            }
            if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                log += logAdapter(uriData,"BluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_ACL_DISCONNECTED);
                evento = "<font color='green'>BluetoothDevice.ACTION_ACL_DISCONNECTED";
                evt.setEventLog(evento);
                logger = writeLog(log, "BluetoothAdapter.txt");
                showToastLog = "BluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_ACL_DISCONNECTED;
                showToast(showToastLog);
            }
            if(action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                Log.d("ACTION_ACL_CONNECTED","Entró a ACTION_PAIRING_REQUEST");
                log += logAdapter(uriData,"BluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_PAIRING_REQUEST);
                evento = "<font color='green'>BluetoothDevice.ACTION_PAIRING_REQUEST";
                evt.setEventLog(evento);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bloqueo = validaBloqueo(device.getAddress());
                UUID uuid = UUID.randomUUID();
                if(bloqueo){
                    try {
                        device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, false);
                        Log.d("ACTION_PAIRING_REQUEST","Entró a removeBond");
                        Method m = device.getClass()
                                .getMethod("removeBond", (Class[]) null);
                        m.invoke(device, (Object[]) null);
                        evento = "<font color='red'>Dispositivo Bloqueado ("+uuid+"): "+device.getAddress()+"; "+device.getName();
                        log += logAdapter(uriData,evento);
                        showToast("Dispositivo bloqueado ("+uuid+") : "+device.getAddress()+" : "+ device.getName());
                    } catch (Exception e) {
                        Log.e("ACTION_PAIRING_REQUEST", e.getMessage());
                        evento = "<font color='red'> Error de ACTION_PAIRING_REQUEST ("+uuid+"): "+device.getAddress()+"; "+e.getMessage();
                        log += logAdapter(uriData,evento);
                    }
                }else{
                    try {
                        Log.w("ACTION_ACL_CONNECTED", "Start Pairing...");
                        Method m = device.getClass()
                                .getMethod("createBond", (Class[]) null);
                        m.invoke(device, (Object[]) null);
                        evento = "<font color='green'>Comenzando Emparejamiento ("+uuid+"): "+device.getAddress()+"; "+device.getName();
                        Log.w("ACTION_ACL_CONNECTED", "Pairing finished.");
                        showToast("Dispositivo Emparejado ("+uuid+") : "+device.getAddress()+" : "+ device.getName());
                    } catch (Exception e) {
                        Log.e("ACTION_ACL_CONNECTED", e.getMessage());
                        evento = "<font color='red'>Error de Emparejamiento "+uuid+"): "+device.getAddress()+"; "+e.getMessage();
                        log += logAdapter(uriData,evento);
                    }
                }
                dbHelper.saveEvent(evt);
                logger = writeLog(log, "BluetoothAdapter.txt");
                showToastLog = "BluetoothDevice."+BluetoothDevice.EXTRA_NAME+" - "+BluetoothDevice.ACTION_PAIRING_REQUEST;
                showToast(showToastLog);
            }
        }
    };

    public event validaUri(Uri uriData, String action){
        event evt = new event();

        evt.setTime(String.valueOf(Calendar.getInstance().getTime()));

        try{
            evt.setUriData(uriData.toString());
        }catch(Exception e){
            evt.setUriData(action);
        }
            try{
            evt.setHost(uriData.getHost());
        }catch(Exception e){
            evt.setHost(null);
        }
        try{
            evt.setPath(uriData.getPath());
        }catch(Exception e){
            evt.setPath(null);
        }
        try{
            evt.setQuery(uriData.getQuery());
        }catch(Exception e){
            evt.setQuery(null);
        }
        try{
            evt.setScheme(uriData.getScheme());
        }catch(Exception e){
            evt.setScheme(null);
        }
        try{
            evt.setPort(String.valueOf(uriData.getPort()));
        }catch(Exception e){
            evt.setPort(null);
        }
        try{
            evt.setUserInfo(uriData.getUserInfo());
        }catch(Exception e){
            evt.setUserInfo(null);
        }
        try{
            evt.setHashCode(String.valueOf(uriData.hashCode()));
        }catch(Exception e){
            evt.setHashCode(null);
        }
        return evt;
    }

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
                log += "\n" + state + "";
                log += "\n Time : " + String.valueOf(Calendar.getInstance().getTime());
                log += "\n uriData : " + uriData;
                log += "\n Host : " + host;
                log += "\n Path : " + path;
                log += "\n Query : " + query;
                log += "\n Scheme : " + scheme;
                log += "\n Port : " + port;
                log += "\n User Info : " + uInfo;
                log += "\n Hash Code : " + hashCode;
                log += "\n ---------------------------- </font> \n";
            }catch(Exception e){
                System.out.println(e);
                log += "\n" + state + "";
                log += "\n Time : " + String.valueOf(Calendar.getInstance().getTime());
                log += "\n uriData : " + uriData;
                log += "\n ---------------------------- </font>\n";
            }
        }else{
            log += "\n" + state + "";
            log += "\n Time : " + String.valueOf(Calendar.getInstance().getTime());
            log += "\n uriData : " + uriData;
            log += "\n ---------------------------- </font>\n";
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
            dev.setBloqueado(d.getString(d.getColumnIndex(devicesContract.deviceEntry.bloqueado)));
            devicesBT.add(dev);
        }
        d.close();
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
        d.close();
        return eventsBT;
    }

    public void getDevicesForBlock(){
        devicesFile = "BT paired Devices";
        int contador = 1;
        if(mBlueAdapter.isEnabled()){
             Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
            for(BluetoothDevice device: devices){

                //Seteamos los devices para almacenar en la base de datos
                device dev = new device();
                dev.setId(contador);contador++;
                dev.setName(device.getName());
                dev.setAddress(device.getAddress());
                dev.setUUIDs(device.getUuids().toString());
                dev.setContentDesc(String.valueOf(device.describeContents()));
                dev.setTime(String.valueOf(Calendar.getInstance().getTime()));
                dev.setBonded(String.valueOf(device.getBondState()));
                dev.setHashCode(String.valueOf(device.hashCode()));

                boolean result = guardaDispositivo(dev);
            }
            boolean logger = writeLog(devicesFile, "devices.txt");
        }
        else{
            //BT is off so can't get paired devices
            showToast("Inicia Bluetooth para obtener los dispositivos emparejados.");
        }
    }

    public boolean validaBloqueo(String val){
        Log.w("validaBloqueo","Entró a validaBloqueo");
        boolean result = false;
        devicesDBHelper dbHelper = new devicesDBHelper(getApplicationContext());
        Cursor c = dbHelper.raw("SELECT "+devicesContract.deviceEntry.bloqueado+" FROM "+devicesContract.deviceEntry.tableName+" WHERE "+devicesContract.deviceEntry.address+" = '"+val+"' OR "+devicesContract.deviceEntry.name+" = '"+val+"'");
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            String bloqueado = c.getString(c.getColumnIndex(devicesContract.deviceEntry.bloqueado));
            if(bloqueado.equals("bloqueado")) {
                result = true;
            }
        }
        return result;
    }

    public boolean guardaDispositivo(device dev){
        boolean results = false;
        Long result = new Long(0);
        //Instanciamos el dbHelper
        devicesDBHelper dbHelper = new devicesDBHelper(getApplicationContext());
        //Validamos que el dispositivo ya está en la BD
        String args [] = new String[1];
        args[0] = dev.getAddress();
        Cursor c = dbHelper.getDevice(devicesContract.deviceEntry.tableName,null,devicesContract.deviceEntry.address+"=?",args,null,null,null);
        if(c.getCount()<1){
            //Almacenamos la info del dispositivo en la BD
            result = dbHelper.saveDevice(dev);
        }else{
            results = dbHelper.updateDevice(dev);
        }
        if(result > 0){results = true;}
        dbHelper.close();
        c.close();
        return results;
    }

}
