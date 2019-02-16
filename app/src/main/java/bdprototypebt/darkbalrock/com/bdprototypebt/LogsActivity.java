package bdprototypebt.darkbalrock.com.bdprototypebt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class LogsActivity extends AppCompatActivity {

    public static final Object[] DATA_LOCK = new Object[0];
    TextView mLogBT;
    Button VerLogsBtn, verLogBTABtn, verLogDEVBtn, volverBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consulta_logs);

        mLogBT = findViewById(R.id.logTv);
        VerLogsBtn = findViewById(R.id.verLogsBtn);
        verLogBTABtn = findViewById(R.id.verLogBTABtn);
        verLogDEVBtn = findViewById(R.id.verLogDEVBtn);
        volverBtn = findViewById(R.id.volverBtn);

        //ver Log DEVICES
        verLogDEVBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mLogBT.clearComposingText();
                mLogBT.append("\n---Log Devices Paired---");
                mLogBT.append(readLog("devices.txt"));
            }
        });

        //ver Log BLUETOOTH ADAPTER
        verLogBTABtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mLogBT.clearComposingText();
                mLogBT.append("\n---Log Bluetooth Adapter---");
                mLogBT.append(readLog("BluetoothAdapter.txt"));
            }
        });

        //volver
        volverBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intentLog = new Intent(LogsActivity.this, MainActivity.class);
                startActivity(intentLog);
                setContentView(R.layout.activity_main);
            }
        });
    }

    //Escribe en el Log de app
    public StringBuilder readLog(String fileName){
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
        return textLog;
    }

}
