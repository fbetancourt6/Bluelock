package bdprototypebt.darkbalrock.com.bdprototypebt.devices;
/**
 * Actividad de listado de dispositivos
 *
 * */
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import bdprototypebt.darkbalrock.com.bdprototypebt.MainActivity;
import bdprototypebt.darkbalrock.com.bdprototypebt.R;

public class dispositivos extends AppCompatActivity {
    Button volverBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dispositivosFragment  fragment = (dispositivosFragment) getSupportFragmentManager().findFragmentById(R.id.content_dispositivos);

        if (fragment == null){
            fragment = dispositivosFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.content_dispositivos, fragment).commit();
        }

        volverBtn = findViewById(R.id.volverBtn2);

        //volver
        volverBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dispositivosFragment  fragment = (dispositivosFragment) getSupportFragmentManager().findFragmentById(R.id.content_dispositivos);
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();

                Intent intentMain = new Intent(dispositivos.this, MainActivity.class);
                startActivity(intentMain);
                //setContentView(R.layout.activity_main);
                dispositivos.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        volverBtn.callOnClick();
    }

    public boolean limpiaDispositivos(){
        //limpiamos la BD de dispositivos
        boolean result = false;
        try {
            devicesDBHelper dbHelper;
            dbHelper = new devicesDBHelper(getApplicationContext());
            dbHelper.deleteDevices();
            dbHelper.close();
            result = true;
        }catch(Exception e){
            Log.e("dispositivos activity", "Limpiando la BD de dispositivos: " + e.toString());
        }
        return result;
    }

}
