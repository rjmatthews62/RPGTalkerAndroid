package au.com.mithril.rpgtalker;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

public class FragmentMain extends Fragment {

    MainActivity main;
    TextView memo1;
    TextView currentFolder;
    TextView currentDevice;

    static FragmentMain newInstance(MainActivity main) {
        FragmentMain result = new FragmentMain();
        result.main=main;
        return result;
    }

    MainActivity getMain() {
        main=(MainActivity) getActivity();
        return main;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        TextView m=rootView.findViewById(R.id.memo1);
        m.setMovementMethod(new ScrollingMovementMethod());
        if (savedInstanceState!=null) {
            String s=savedInstanceState.getString("addln","");
            m.setText(s);
        }
        currentFolder=rootView.findViewById(R.id.currentFolder);
        currentDevice= rootView.findViewById(R.id.currentDevice);
        if (getMain()!=null) {
            currentFolder.setText(main.getShortSound());
            currentDevice.setText(main.getConnectedString());
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getView()!=null) {
            TextView m = getView().findViewById(R.id.memo1);
            String s = m.getText().toString();
            outState.putString("addln", s);
        }
    }
}
