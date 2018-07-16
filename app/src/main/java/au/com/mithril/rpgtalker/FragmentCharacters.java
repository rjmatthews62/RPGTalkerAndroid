package au.com.mithril.rpgtalker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class FragmentCharacters extends Fragment {
    ListView mDestinations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_characters, container, false);
        mDestinations=root.findViewById(R.id.destinations);
        mDestinations.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final MainActivity main = (MainActivity) getActivity();
        mDestinations.setAdapter(main.mDestinations);
        mDestinations.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long arg) {
                Destination dest=main.mDestinations.getItem(pos);
                if (!dest.speaker) linkDestination(dest);

                return true;
            }
        });
        mDestinations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long arg) {
                Destination dest = (Destination) mDestinations.getItemAtPosition(pos);
                main.setCurrentDest(dest);
            }
        });
        return root;
    }

    void linkDestination(Destination dest) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set Destination");
        builder.setMessage(dest.name);
// Set up the input
        final Destination localdest = dest;
        final Spinner input = new Spinner(getContext());
        final MainActivity main = (MainActivity) getActivity();
        main.setDeviceList(input);
        if (dest.device!=null) {
            for(int i=0; i<input.getCount(); i++) {
                DevHolder h = (DevHolder) input.getItemAtPosition(i);
                if (h.file.equals(dest.device)) {
                    input.setSelection(i);
                    break;
                }
            }
        }
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                localdest.device=(DevHolder) input.getSelectedItem();
                main.saveDestinations();
                mDestinations.setAdapter(null);
                mDestinations.setAdapter(main.mDestinations);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();


    }


}
