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
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import bdprototypebt.darkbalrock.com.bdprototypebt.LogsActivity;
import bdprototypebt.darkbalrock.com.bdprototypebt.MainActivity;
import bdprototypebt.darkbalrock.com.bdprototypebt.R;

public class BTGattServer extends Activity {

    private static final String TAG = BTGattServer.class.getSimpleName();

    /*Local UI*/
    private TextView localTimeView;
    Button volverBtn;
    /*BT API*/
    private BluetoothManager btManager;
    private BluetoothGattServer btGattServer;
    private BluetoothLeAdvertiser btLeAdvertiser;
    /*Coleccion de subscriptores de notificacion*/
    private Set<BluetoothDevice> registerDevices = new HashSet<>();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_server); //activity server
        localTimeView = (TextView) findViewById(R.id.text_time); // Text Time
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
            Log.d(TAG, "Bluetooth is currently disabled...enabling");
            btAdapter.enable();
        }else{
            Log.d(TAG, "Bluetooth enabled...starting services");
            startAdvertising();
            startServer();
        }

        //volver
        volverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMain = new Intent(BTGattServer.this, MainActivity.class);
                startActivity(intentMain);
                setContentView(R.layout.activity_main);
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

    /*
    * Verifica el nivel de soporte Bluetooth proporcionado por el hardware.
     * @param bluetoothAdapter System {@link BluetoothAdapter}.
     * @return true si Bluetooth es soportado adecuadamente, falso de lo contrario.
    * */

    private boolean checkBluetoothSupport(BluetoothAdapter btAdapter){
        if(btAdapter == null){
            Log.w(TAG, "Bluetooth no está soportado");
            return false;
        }

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Log.w(TAG,"Bluetooth LE no está soportado");
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
            switch(intent.getAction()){
                case Intent.ACTION_TIME_CHANGED:
                    adjustReason = TimeProfile.ADJUST_MANUAL;
                    break;
                case Intent.ACTION_TIMEZONE_CHANGED:
                    adjustReason = TimeProfile.ADJUST_TIMEZONE;
                    break;
                case Intent.ACTION_TIME_TICK:
                    adjustReason = TimeProfile.ADJUST_NONE;
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
            switch(state){
                case BluetoothAdapter.STATE_ON:
                    startAdvertising();
                    startServer();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopServer();
                    stopAdvertising();
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
            return;
        }
        btGattServer.addService(TimeProfile.createTimeService());

        //Iniciamos la UI local
        updateLocalUI(System.currentTimeMillis());
    }

    /*
    * Apagamos el GATT server
    * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopServer(){
        if(btGattServer == null) return;

        btGattServer.close();
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
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.w(TAG, "LE Advertise Failed: "+errorCode);
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
            return;
        }
        byte[] exactTime = TimeProfile.getExactTime(timestamp, adjustReason);
        Log.i(TAG, "Enviando actualizacion a "+registerDevices.size()+" suscritpos");
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

    /*
    * Retorno de llamada para manejo de las solicitudes entrantes al servidor GATT.
    * Todas las solicitudes de lectura / escritura de características y descriptores se manejan aquí.
    * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2|Build.VERSION_CODES.LOLLIPOP)
    private BluetoothGattServerCallback btGattServerCallBack = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.i(TAG,"BluetoothDevice CONNECTED: "+device);
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i(TAG, "BluetoothDevice DISCONNECTED: "+device);
                //Quitamos el dispositivo de las subscripciones activas
                registerDevices.remove(device);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            long now = System.currentTimeMillis();
            if(TimeProfile.CURRENT_TIME.equals(characteristic.getUuid())){
                Log.i(TAG,"Lee CurrentTime");
                btGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getExactTime(now, TimeProfile.ADJUST_NONE));
            }else if(TimeProfile.LOCAL_TIME_INFO.equals(characteristic.getUuid())){
                Log.i(TAG, "Lee LocalTimeInfo");
                btGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getLocalTimeInfo(now));
            }else{
                //Caracteristica no validada
                Log.w(TAG,"Invalid Characteristic Read: "+characteristic.getUuid());
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
            if(TimeProfile.CLIENT_CONFIG.equals(descriptor.getUuid())){
                Log.d(TAG,"lectura de congif descriptor");
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
            if(TimeProfile.CLIENT_CONFIG.equals(descriptor.getUuid())){
                if(Arrays.equals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE,value)){
                    Log.d(TAG,"Suscribe el dispositivo: "+device);
                    registerDevices.add(device);
                }else if(Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE,value)){
                    Log.d(TAG,"DesSuscribe el dispositivo: "+device);
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
}
