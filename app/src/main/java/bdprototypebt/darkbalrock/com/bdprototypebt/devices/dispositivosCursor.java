package bdprototypebt.darkbalrock.com.bdprototypebt.devices;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import bdprototypebt.darkbalrock.com.bdprototypebt.R;

public class dispositivosCursor extends CursorAdapter {

    public dispositivosCursor (Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup){
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.list_item_dispositivo, viewGroup, false);
    }

    @Override
    public void bindView(View view,  final Context context, Cursor cursor){
        //Referencias de UI
        TextView nameText = (TextView) view.findViewById(R.id.tv_deviceName);

        //Get valores
        String name = cursor.getString(cursor.getColumnIndex(devicesContract.deviceEntry.name));

        //Setup
        nameText.setText(name);

    }
}
