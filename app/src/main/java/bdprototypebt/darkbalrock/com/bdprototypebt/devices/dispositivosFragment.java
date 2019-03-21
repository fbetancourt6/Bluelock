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
import android.widget.ListView;

import bdprototypebt.darkbalrock.com.bdprototypebt.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link dispositivosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class dispositivosFragment extends Fragment {

    private devicesDBHelper mDevDbHelper;
    private ListView mDevicesList;
    private dispositivosCursor mDevicesAdapter;
    private FloatingActionButton mAddButton;

    public dispositivosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment dispositivosFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        mAddButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);

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
                // Mostrar empty state
            }
        }
    }
}
