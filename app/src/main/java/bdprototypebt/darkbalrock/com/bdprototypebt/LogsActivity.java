package bdprototypebt.darkbalrock.com.bdprototypebt;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import bdprototypebt.darkbalrock.com.bdprototypebt.events.eventsDBHelper;

public class LogsActivity extends AppCompatActivity {

    public static final Object[] DATA_LOCK = new Object[0];
    TextView mLogBT;
    Button VerLogsBtn, verLogBTABtn,  volverBtn, borrarLogsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consulta_logs);

        mLogBT = findViewById(R.id.logTv);
        VerLogsBtn = findViewById(R.id.verLogsBtn);
        verLogBTABtn = findViewById(R.id.verLogBTABtn);
        volverBtn = findViewById(R.id.volverBtn);
        borrarLogsBtn = findViewById(R.id.borrarLogsBtn);

        //ver Log BLUETOOTH ADAPTER
        verLogBTABtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                mLogBT.setText("");
                mLogBT.append("\n---Log Bluetooth Adapter---");
                if (((int)Build.VERSION.SDK_INT) >= 24) {
                    mLogBT.setText(Html.fromHtml(readLog("BluetoothAdapter.txt"), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    mLogBT.setText(Html.fromHtml(readLog("BluetoothAdapter.txt")));
                }
                mLogBT.setMovementMethod(new ScrollingMovementMethod());
            }
        });

        //borrar Logs BLUETOOTH
        borrarLogsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getBaseContext();
                boolean result = false;
                File path = context.getExternalFilesDir(null);
                File filedevices = new File(path, "devices.txt");
                File fileBluetoothAdapter = new File(path, "BluetoothAdapter.txt");
                Writer out1 = null, out2 = null;
                try {
                    synchronized (DATA_LOCK) {
                        if (filedevices != null || fileBluetoothAdapter != null) {
                            filedevices.createNewFile();
                            fileBluetoothAdapter.createNewFile();
                            out1 = new BufferedWriter(new FileWriter(filedevices, false), 1024);
                            out2 = new BufferedWriter(new FileWriter(fileBluetoothAdapter, false), 1024);
                            out1.write("");
                            out1.close();
                            out2.write("");
                            out2.close();
                            result = true;
                        }
                    }
                } catch (FileNotFoundException e) {
                    Log.e("logs activity", "File not found: " + e.toString());
                } catch (IOException e) {
                    Log.e("logs activity", "Can not read file: " + e.toString());
                }
                eliminaEventos();
                verLogBTABtn.callOnClick();
            }
        });

        //volver
        volverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMain = new Intent(LogsActivity.this, MainActivity.class);
                startActivity(intentMain);
                //setContentView(R.layout.activity_main);
                LogsActivity.this.finish();
            }
        });
    }

    //lee el Log de app
    public String readLog(String fileName){
        Context context = getBaseContext();
        File path = context.getExternalFilesDir(null);
        File file = new File(path, fileName);
        StringBuilder textLog = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null){
                textLog.append(line);
                textLog.append('\n');
            }
            br.close();
            if(textLog.toString() == ""){
                textLog.append("Empty file");
            }
        }
        catch (FileNotFoundException e) {
            Log.e("read log activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("read log activity", "Can not read file: " + e.toString());
        }
        return String.valueOf(textLog);
    }

    //Borra la tabla de eventos
    public boolean eliminaEventos(){
        boolean result = false;
        try {
            eventsDBHelper e = new eventsDBHelper(this);
            e.deleteEvents();
            result = true;
        }catch(Exception e){
            Log.w("eliminaEventos",e.getMessage());
        }
        return result;
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        volverBtn.callOnClick();
    }
}
