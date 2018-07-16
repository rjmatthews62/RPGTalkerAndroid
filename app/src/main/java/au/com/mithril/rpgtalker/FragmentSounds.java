package au.com.mithril.rpgtalker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class FragmentSounds extends Fragment {
    MainActivity main;
    ListView mSoundList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sounds, container, false);
        main=(MainActivity) getActivity();
        mSoundList = root.findViewById(R.id.soundList);
        mSoundList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long arg) {
                DocHolder doc = (DocHolder) mSoundList.getItemAtPosition(pos);
                main.play(doc.file);
            }
        });
        mSoundList.setAdapter(main.mSounds);
        return root;
    }
}
