package au.com.mithril.rpgtalker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FragmentDevices extends Fragment {

    ListView mDevices;

    MainActivity getMain() {
        return (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root=inflater.inflate(R.layout.fragment_devices, container, false);
        mDevices=root.findViewById(R.id.deviceList);
        mDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        MainActivity main = getMain();
        if (main!=null) {
            main.setDeviceList(mDevices);
        }

        return root;
    }
}
