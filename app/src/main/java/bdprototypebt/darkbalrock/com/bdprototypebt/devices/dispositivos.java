package bdprototypebt.darkbalrock.com.bdprototypebt.devices;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import bdprototypebt.darkbalrock.com.bdprototypebt.R;

public class dispositivos extends AppCompatActivity {

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
    }

}
