package bdprototypebt.darkbalrock.com.bdprototypebt.devices;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import bdprototypebt.darkbalrock.com.bdprototypebt.R;

public class dispositivosCursor extends CursorAdapter {

    Context context;

    public dispositivosCursor (Context context, Cursor cursor){
        super(context, cursor, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup){
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.list_item_dispositivo, viewGroup, false);
    }

    @Override
    public void bindView(View view,  final Context context, Cursor cursor){
        View v = view;
        //Referencias de UI
        TextView nameText = (TextView) view.findViewById(R.id.tv_deviceName);
        TextView addressText = (TextView) view.findViewById(R.id.tv_deviceAddress);
        ImageView btnBlock = (ImageView) view.findViewById(R.id.iv_btnBlock);

        //Get valores
        final String name = cursor.getString(cursor.getColumnIndex(devicesContract.deviceEntry._ID))+" : "+cursor.getString(cursor.getColumnIndex(devicesContract.deviceEntry.name));
        final String address = cursor.getString(cursor.getColumnIndex(devicesContract.deviceEntry.address));

        //Setup
        nameText.setText(name);
        addressText.setText(address);

        //Validamos que el estado del dispositivo en la BD
        devicesDBHelper dbHelper = new devicesDBHelper(context);
        //Cursor c = dbHelper.raw("select "+devicesContract.deviceEntry.bloqueado+" from "+devicesContract.deviceEntry.tableName+" where "+devicesContract.deviceEntry.address+"='"+devicesContract.deviceEntry.address+"'");

        //Validamos que el dispositivo ya está en la BD
        String args [] = new String[1];
        String cols [] = new String[1];
        args[0] = address;
        cols[0] = devicesContract.deviceEntry.bloqueado;
        Cursor c = dbHelper.getDevice(devicesContract.deviceEntry.tableName,cols,devicesContract.deviceEntry.address+"=?",args,null,null,null);
        String lock = "empty";
        if (c!= null && c.getCount() > 0){
            c.moveToFirst();
            // All the logic of retrieving data from cursor
            lock = c.getString(c.getColumnIndex(devicesContract.deviceEntry.bloqueado));
        }
        Log.w("Valida Internal Lock","Validando Bloqueo!"+lock);
        if(lock == null){
            btnBlock.setImageResource(R.drawable.ic_unlock);
            Log.w("Valida Internal Lock","empty");
         }
        else if(lock.equals("bloqueado")) {
            Log.w("Valida Internal Lock","bloqueado");
            btnBlock.setImageResource(R.drawable.ic_lock);
        }
        else{
            Log.w("Valida Internal Lock","else");
            btnBlock.setImageResource(R.drawable.ic_unlock);
        }

        //btn Bloqueo Dispositivo
        btnBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,address, Toast.LENGTH_SHORT).show();
                device dev = new device();
                dev.setAddress(address);
                dev.setName(name);
                devicesDBHelper devHelper = new devicesDBHelper(context);
                boolean result = devHelper.blockDevice(dev);
                ImageView btnBlock = (ImageView) v.findViewById(R.id.iv_btnBlock);
                if(result){
                    btnBlock.setImageResource(R.drawable.ic_lock);
                    Toast.makeText( context, "Dispositivo: "+dev.getName()+" Bloqueado.", Toast.LENGTH_SHORT).show();
                }else{
                    btnBlock.setImageResource(R.drawable.ic_unlock);
                    Toast.makeText( context, "Dispositivo: "+dev.getName()+" Desbloqueado.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
