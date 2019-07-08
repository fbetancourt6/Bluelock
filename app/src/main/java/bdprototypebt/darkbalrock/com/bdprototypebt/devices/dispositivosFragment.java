package bdprototypebt.darkbalrock.com.bdprototypebt.devices;


import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;

import bdprototypebt.darkbalrock.com.bdprototypebt.R;

public class dispositivosFragment extends Fragment {

    private devicesDBHelper mDevDbHelper;
    private ListView mDevicesList;
    private dispositivosCursor mDevicesAdapter;
    private FloatingActionButton mAddButton;

    public dispositivosFragment() {
        // Required empty public constructor
    }

    /**
     * Usamos este metodo factory para crear una nueva instancia de este fragmento.
     *
     * @return una nueva instancia de dispositivosFragment.
     */
    public static dispositivosFragment newInstance() {
        return new dispositivosFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_dispositivos, container, false);

        //Referencias al UI
        mDevicesList = (ListView) root.findViewById(R.id.dispositivos_list);
        mDevicesAdapter = new dispositivosCursor(getActivity(), null);

        //Setup
        mDevicesList.setAdapter(mDevicesAdapter);

        //Instanciamos el helper
        mDevDbHelper = new devicesDBHelper(getActivity());

        //carga de dispositivos
        loadDevices();

        return root;
    }

    private void loadDevices() {
        new devicesLoadTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public class devicesLoadTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {
            return mDevDbHelper.getDevices();
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {
                mDevicesAdapter.swapCursor(cursor);
            } else {
                cursor.close();
            }
        }
    }
}
