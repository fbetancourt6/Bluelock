package bdprototypebt.darkbalrock.com.bdprototypebt.GattServer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import android.text.format.DateFormat;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import bdprototypebt.darkbalrock.com.bdprototypebt.MainActivity;
import bdprototypebt.darkbalrock.com.bdprototypebt.R;
import bdprototypebt.darkbalrock.com.bdprototypebt.devices.devicesContract;
import bdprototypebt.darkbalrock.com.bdprototypebt.devices.devicesDBHelper;

public class BTGattServer extends Activity {

    private static final String TAG = BTGattServer.class.getSimpleName();

    /*Local UI*/
    private TextView localTimeView, text_Gatt_Rec;
    Button volverBtn;
    /*BT API*/
    private BluetoothManager btManager;
    private BluetoothGattServer btGattServer;
    private BluetoothLeAdvertiser btLeAdvertiser;
    /*Coleccion de subscriptores de notificacion*/
    private Set<BluetoothDevice> registerDevices = new HashSet<>();
    /**/
    public static final Object[] DATA_LOCK = new Object[0];

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_server); //activity server
        localTimeView = (TextView) findViewById(R.id.text_time); // Tiempo
        text_Gatt_Rec = (TextView) findViewById(R.id.text_Gatt_Rec); //Registro
        volverBtn = findViewById(R.id.volverBtn);

        //Los dispositivos con pantalla no deben irse a dormir.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        //No podemos continuar sin el soporte indicado de BT
        if(!checkBluetoothSupport(btAdapter)){
            finish();
        }

        //Registro para eventos BLuetooth
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btBroadCastRec, filter);//Broadcast Receiver
        if(!btAdapter.isEnabled()){
            Log.w(TAG, "Bluetooth is currently disabled...enabling");
            displayEvent("Bluetooth is currently disabled...enabling");
            btAdapter.enable();
        }else{
            Log.w(TAG, "Bluetooth enabled...starting services");
            displayEvent("Bluetooth enabled...starting services");
            startAdvertising();
            startServer();
        }

        //volver
        volverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMain = new Intent(BTGattServer.this, MainActivity.class);
                startActivity(intentMain);
                //setContentView(R.layout.activity_main);
                BTGattServer.this.finish();
            }
        });
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    protected void onStart(){
        super.onStart();
        //Registro para eventos de reloj
        IntentFilter filter = new IntentFilter();
        filter.addAction(getIntent().ACTION_TIME_TICK);
        filter.addAction(getIntent().ACTION_TIME_CHANGED);
        filter.addAction(getIntent().ACTION_TIMEZONE_CHANGED);
        registerReceiver(timeReceiver,filter);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    protected void onStop(){
        super.onStop();
        unregisterReceiver(timeReceiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy(){
        super.onDestroy();

        BluetoothAdapter btAdapter = btManager.getAdapter();
        if(btAdapter.isEnabled()){
            stopServer();
            stopAdvertising();
        }
        unregisterReceiver(btBroadCastRec);
    }

    @Override
    public void onBackPressed() {
        volverBtn.callOnClick();
    }

    /*
    * Verifica el nivel de soporte Bluetooth proporcionado por el hardware.
     * @param bluetoothAdapter System {@link BluetoothAdapter}.
     * @return true si Bluetooth es soportado adecuadamente, falso de lo contrario.
    * */

    private boolean checkBluetoothSupport(BluetoothAdapter btAdapter){
        if(btAdapter == null){
            Log.w(TAG, "Bluetooth no está soportado");
            displayEvent("Bluetooth no está soportado");
            return false;
        }

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Log.w(TAG,"Bluetooth LE no está soportado");
            displayEvent("Bluetooth LE no está soportado");
            return false;
        }

        return true;
    }

    /*
     * Escucha los cambios de hora del sistema y activa una notificación para
     * Suscriptores de Bluetooth.
     * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    private BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte adjustReason = 0;
            String log = "";
            boolean logger = false;
            switch(intent.getAction()){
                case Intent.ACTION_TIME_CHANGED:
                    adjustReason = TimeProfile.ADJUST_MANUAL;
                    log = "<font color='blue'>ACTION_TIME_CHANGED: ADJUST_MANUAL</font>";
                    logger = writeLog(log, "BluetoothAdapter.txt");
                    displayEvent("ACTION_TIME_CHANGED: ADJUST_MANUAL");
                    break;
                case Intent.ACTION_TIMEZONE_CHANGED:
                    adjustReason = TimeProfile.ADJUST_TIMEZONE;
                    log = "<font color='blue'>ACTION_TIME_CHANGED: ADJUST_TIMEZONE</font>";
                    logger = writeLog(log, "BluetoothAdapter.txt");
                    displayEvent("ACTION_TIME_CHANGED: ADJUST_TIMEZONE");
                    break;
                case Intent.ACTION_TIME_TICK:
                    adjustReason = TimeProfile.ADJUST_NONE;
                    log = "<font color='blue'>ACTION_TIME_CHANGED: ADJUST_NONE</font>";
                    logger = writeLog(log, "BluetoothAdapter.txt");
                    displayEvent("ACTION_TIME_CHANGED: ADJUST_NONE");
                    break;
            }
            long now = System.currentTimeMillis();
            notifyRegisteredDevices(now, adjustReason);
            updateLocalUI(now);
        }
    };

    /*
    * Escucha los eventos del adaptador Bluetooth para habilitar / deshabilitar
    * Publicidad y funcionalidad del servidor.
    * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    private BroadcastReceiver btBroadCastRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            String log = "";
            boolean logger = false;
            switch(state){
                case BluetoothAdapter.STATE_ON:
                    startAdvertising();
                    startServer();
                    log = "BluetoothAdapter.STATE_ON";
                    logger = writeLog(log, "BluetoothAdapter.txt");
                    displayEvent("BluetoothAdapter.STATE_ON");
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopServer();
                    stopAdvertising();
                    log = "BluetoothAdapter.STATE_OFF";
                    logger = writeLog(log, "BluetoothAdapter.txt");
                    displayEvent("BluetoothAdapter.STATE_OFF");
                    break;
                default:
                    //nada
            }
        }
    };

    /*
    * Comienza el broadcast por Bluetooth de que este dispositivo es conectable.
    * y es compatible con el servicio de hora actual.
    * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    private void startAdvertising(){
        BluetoothAdapter btAdapter = btManager.getAdapter();
        btLeAdvertiser = btAdapter.getBluetoothLeAdvertiser();
        if(btLeAdvertiser == null){
            Log.w(TAG, "Fallo al crear Advertiser");
            String log = "<font color='red'>Fallo al crear Advertiser</font>";
            boolean logger = writeLog(log, "BluetoothAdapter.txt");
            displayEvent("Fallo al crear Advertiser");
            return;
        }
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(new ParcelUuid(TimeProfile.TIME_SERVICE))
                .build();
        btLeAdvertiser.startAdvertising(settings,data,btLeAdvertiserCallBack);

        displayEvent("Advertiser Creado!");
    }

    /*
    * Detener anuncios de Bluetooth.
    * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    private void stopAdvertising(){
        if(btLeAdvertiser==null)return;
        btLeAdvertiser.stopAdvertising(btLeAdvertiserCallBack);
    }

    /*
    * Inicializa la instancia del servidor GATT con los
    * servicios / características del perfil de tiempo.
    * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    private void startServer(){
        btGattServer = btManager.openGattServer(this,btGattServerCallBack);
        if(btGattServer==null){
            Log.w(TAG,"No se ha podido crear el GATT Server");
            String log = "<font color='red'>No se ha podido crear el GATT Server</font>";
            boolean logger = writeLog(log, "BluetoothAdapter.txt");
            displayEvent("No se ha podido crear el GATT Server");
            return;
        }
        btGattServer.addService(TimeProfile.createTimeService());

        //Iniciamos la UI local
        updateLocalUI(System.currentTimeMillis());

        displayEvent("GATT Server Creado!");
    }

    /*
    * Apagamos el GATT server
    * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopServer(){
        if(btGattServer == null) return;

        btGattServer.close();
        String log = "<font color='purple'>Deteniendo GATT Server.</font>";
        boolean logger = writeLog(log, "BluetoothAdapter.txt");
        displayEvent("Deteniendo GATT Server.");
    }

    /*
    * Retorno de llamada para recibir información sobre el proceso de publicidad
    * */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseCallback btLeAdvertiserCallBack = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i(TAG, "LE Advertise Started.");
            String log = "<font color='blue'>LE Advertise Iniciado.</font>";
            boolean logger = writeLog(log, "BluetoothAdapter.txt");
            displayEvent("DE Advertise Iniciado.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.w(TAG, "LE Advertise Failed: "+errorCode);
            String log = "<font color='red'>LE Advertise Failed: "+errorCode+"</font>";
            boolean logger = writeLog(log, "BluetoothAdapter.txt");
            displayEvent("LE Advertise Failed: "+errorCode);
        }
    };

    /*
     * Envía una notificación de tiempo a cualquier
     * dispositivo suscrito.
     * */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void notifyRegisteredDevices(long timestamp, byte adjustReason){
        if(registerDevices.isEmpty()){
            Log.i(TAG, "No hay dispositivos suscritos");
            String log = "<font color='purple'>No hay dispositivos suscritos</font>";
            boolean logger = writeLog(log, "BluetoothAdapter.txt");
            displayEvent("No hay dispositivos suscritos.");
            return;
        }
        byte[] exactTime = TimeProfile.getExactTime(timestamp, adjustReason);
        Log.i(TAG, "Enviando actualizacion a "+registerDevices.size()+" suscritpos");
        String log = "<font color='purple'>Enviando actualizacion a "+registerDevices.size()+" suscritpos</font>";
        boolean logger = writeLog(log, "BluetoothAdapter.txt");
        displayEvent("Enviando actualizacion a "+registerDevices.size());
        for(BluetoothDevice device : registerDevices){
            BluetoothGattCharacteristic timeCharacteristic = btGattServer
            .getService(TimeProfile.TIME_SERVICE)
            .getCharacteristic(TimeProfile.CURRENT_TIME);
            timeCharacteristic.setValue(exactTime);
            btGattServer.notifyCharacteristicChanged(device, timeCharacteristic, false);
        }
    }

    /*
    * Actualiza la UI grafica en los dispositivos de la lista que lo soportan
    *
    * */
    private void updateLocalUI(long timestamp){
        Date date = new Date(timestamp);
        String displatDate = DateFormat.getMediumDateFormat(this).format(date)
                +"\n"
                +DateFormat.getTimeFormat(this).format(date);
        localTimeView.setText(displatDate);
    }

    //toast message function
    private void showToast(String msg){
        Toast.makeText( this, msg, Toast.LENGTH_SHORT).show();
    }

    /*
    * Retorno de llamada para manejo de las solicitudes entrantes al servidor GATT.
    * Todas las solicitudes de lectura / escritura de características y descriptores se manejan aquí.
    * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    private BluetoothGattServerCallback btGattServerCallBack = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            String log = "";
            boolean logger = false;
            if(newState == BluetoothProfile.STATE_CONNECTED){
                boolean bloqueo = validaBloqueo(device.getAddress());
                if(bloqueo){
                    try {
                        Log.w("ACTION_ACL_CONNECTED","Entró a removeBond");
                        Method m = device.getClass()
                                .getMethod("removeBond", (Class[]) null);
                        m.invoke(device, (Object[]) null);
                        log = "<font color='red'>Dispositivo Bloqueado: "+device.getAddress()+"; "+device.getName()+"</font>";
                        showToast("Dispositivo bloqueado: "+device.getAddress()+" : "+ device.getName());
                    } catch (Exception e) {
                        Log.w("ACTION_ACL_CONNECTED", e.getMessage());
                        log = "<font color='red'>Error de Dispositivo Bloqueado: "+device.getAddress()+"; "+device.getName()+"</font>";
                    }
                }
                Log.i(TAG,"BluetoothDevice CONNECTED: "+device);
                log = "<font color='green'>BluetoothDevice CONNECTED: "+device+"</font>";
                logger = writeLog(log, "BluetoothAdapter.txt");
                displayEvent("BluetoothDevice CONNECTED: "+device);
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i(TAG, "BluetoothDevice DISCONNECTED: "+device);
                log = "<font color='red'>BluetoothDevice DISCONNECTED: "+device+"</font>";
                logger = writeLog(log, "BluetoothAdapter.txt");
                displayEvent("BluetoothDevice DISCONNECTED: "+device);
                //Quitamos el dispositivo de las subscripciones activas
                registerDevices.remove(device);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            long now = System.currentTimeMillis();
            String log = ""; boolean logger = false;
            if(TimeProfile.CURRENT_TIME.equals(characteristic.getUuid())){
                Log.i(TAG,"Lee CurrentTime");
                log = "<font color='blue'>TimeProfile.CURRENT_TIME: "+device+"</font>";
                logger = writeLog(log, "BluetoothAdapter.txt");
                displayEvent("TimeProfile.CURRENT_TIME: "+device);
                btGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getExactTime(now, TimeProfile.ADJUST_NONE));
            }else if(TimeProfile.LOCAL_TIME_INFO.equals(characteristic.getUuid())){
                Log.i(TAG, "Lee LocalTimeInfo");
                log = "<font color='blue'>TimeProfile.LOCAL_TIME_INFO: "+device+"</font>";
                logger = writeLog(log, "BluetoothAdapter.txt");
                displayEvent("TimeProfile.LOCAL_TIME_INFO: "+device);
                btGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getLocalTimeInfo(now));
            }else{
                //Caracteristica no validada
                Log.w(TAG,"Invalid Characteristic Read: "+characteristic.getUuid());
                log = "<font color='red'>Invalid Characteristic Read: "+characteristic.getUuid()+"</font>";
                logger = writeLog(log, "BluetoothAdapter.txt");
                displayEvent("Invalid Characteristic Read: "+characteristic.getUuid());
                btGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            String log = "";
            boolean logger = false;
            if(TimeProfile.CLIENT_CONFIG.equals(descriptor.getUuid())){
                Log.d(TAG,"lectura de congif descriptor");
                log = "<font color='blue'>TimeProfile.CLIENT_CONFIG.: "+descriptor.getUuid()+"</font>";
                logger = writeLog(log, "BluetoothAdapter.txt");
                displayEvent("TimeProfile.CLIENT_CONFIG.: "+descriptor.getUuid());
                byte[] returnValue;
                if(registerDevices.contains(device)){
                    returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                }else{
                    returnValue = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                btGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        returnValue);
            }else{
                Log.w(TAG,"Lectura de descriptor desconocida");
                log = "<font color='red'>Lectura de descriptor desconocida"+descriptor.getUuid()+"</font>";
                logger = writeLog(log, "BluetoothAdapter.txt");
                displayEvent("Lectura de descriptor desconocida"+descriptor.getUuid());
                btGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            String log = "";
            boolean logger = false;
            if(TimeProfile.CLIENT_CONFIG.equals(descriptor.getUuid())){
                if(Arrays.equals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE,value)){
                    Log.d(TAG,"Suscribe el dispositivo: "+device);
                    log = "<font color='blue'>TimeProfile.CLIENT_CONFIG: "+descriptor.getUuid()+"</font>";
                    logger = writeLog(log, "BluetoothAdapter.txt");
                    displayEvent("TimeProfile.CLIENT_CONFIG: "+descriptor.getUuid());
                    registerDevices.add(device);
                }else if(Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE,value)){
                    Log.d(TAG,"DesSuscribe el dispositivo: "+device);
                    log = "<font color='purple'>DISABLE_NOTIFICATION_VALUE: "+descriptor.getUuid()+"</font>";
                    logger = writeLog(log, "BluetoothAdapter.txt");
                    displayEvent("DISABLE_NOTIFICATION_VALUE: "+descriptor.getUuid());
                    registerDevices.remove(device);
                }

                if(responseNeeded){
                    btGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            0,
                            null);
                }
            }else{
                Log.w(TAG,"Solicitud de escritura desconocida ");
                log = "<font color='red'>Solicitud de escritura desconocida : "+descriptor.getUuid()+"</font>";
                logger = writeLog(log, "BluetoothAdapter.txt");
                displayEvent("Solicitud de escritura desconocida : "+descriptor.getUuid());
                if(responseNeeded){
                    btGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            0,
                            null);

                }
            }
        }
    };

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
            Log.e("Main activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Main activity", "Can not read file: " + e.toString());
        }
        return result;
    }

    public boolean validaBloqueo(String val){
        boolean result = false;
        devicesDBHelper dbHelper = new devicesDBHelper(getApplicationContext());
        Cursor c = dbHelper.raw("SELECT "+ devicesContract.deviceEntry.bloqueado+" FROM "+devicesContract.deviceEntry.tableName+" WHERE "+devicesContract.deviceEntry.address+" = "+val+" OR "+devicesContract.deviceEntry.name+" = "+val);
        if(c!=null && c.getCount()>0){
            c.moveToFirst();
            String bloqueado = c.getString(c.getColumnIndex(devicesContract.deviceEntry.bloqueado));
            if(bloqueado.equals("bloqueado")) {
                result = true;
            }
        }
        c.close();
        return result;
    }

    public void displayEvent(String texto){
        String rec = String.valueOf(text_Gatt_Rec.getText());
        texto += "\n"+rec;
        text_Gatt_Rec.setText(texto);
    }
}
