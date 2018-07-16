package au.com.mithril.rpgtalker;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    Handler mainHandler = null;
    MyReceiver mReceiver = new MyReceiver();
    public static final int SOUND_FOLDER_READ = 1;
    public BluetoothA2dp mA2DP;
    Timer mTimer = null;
    public final static ArrayList<DevHolder> devices = new ArrayList<>();
    static MediaPlayer mPlayer;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    TextView memo1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String mSoundFolder;
    public ArrayAdapter<Destination>  mDestinations;
    public ArrayAdapter<DocHolder> mSounds;
    private Destination mCurrentDest;

    public String getSoundFolder() {
        return mSoundFolder;
    }

    public void setSoundFolder(String mSoundFolder) {
        this.mSoundFolder = mSoundFolder;
        populateDestinations();
        setText(R.id.currentFolder, getShortSound());
    }

    public String getShortSound() {
        String s = getSoundFolder();
        if (s == null || s.isEmpty()) return "No Sound Folder";
        try {
            Uri uri = Uri.parse(s);
            s = uri.getPath();
        } catch (Exception e) {
        }
        return s;
    }

    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainHandler = new Handler(getMainLooper());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.main = this;

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mDestinations = new ArrayAdapter<Destination>(this, android.R.layout.simple_list_item_single_choice);
        mSounds = new ArrayAdapter<DocHolder>(this, android.R.layout.simple_list_item_single_choice);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
            }
        });


        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
        mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.A2DP);
        mTimer = new Timer("ticker", true);
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateConnected();
                    }
                });
            }
        }, 1000, 1000);
        updateDeviceList();
        setSoundFolder(mPreferences.getString("soundfolder", null));
    }


    private void updateDeviceList() {
        Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : paired) {
            if (isAudio(bd)) {
                updateDevice(bd);
            }
        }
        for (Iterator<DevHolder> it = devices.iterator(); it.hasNext(); ) {
            DevHolder h = it.next();
            if (!paired.contains(h.file)) it.remove();
        }
        Collections.sort(devices);
        setDeviceList((ListView) findViewById(R.id.deviceList));
    }


    private void updateDevice(BluetoothDevice bd) {
        DevHolder h = findDevHolder(bd);
        if (h == null) {
            h = new DevHolder(friendlyName(bd), bd);
            devices.add(h);
        }
        h.name = friendlyName(bd);
        h.file = bd;
    }

    private DevHolder findDevHolder(BluetoothDevice bd) {
        for (DevHolder h : devices) {
            if (h.file.getAddress().equals(bd.getAddress())) return h;
        }
        return null;
    }

    private DevHolder findDevHolder(String address) {
        for (DevHolder h : devices) {
            if (h.file.getAddress().equals(address)) return h;
        }
        return null;
    }



    @Override
    protected void onResume() {
        super.onResume();
        getMemo1();
    }

    @Override
    protected void onDestroy() {
        mTimer.cancel();
        unregisterReceiver(mReceiver);
        mBluetoothAdapter.cancelDiscovery();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SOUND_FOLDER_READ) {
            if (data == null) {
                addln("No folder selected.");
            } else {
                addln("Folder=" + data.getData());
                setSoundFolder(data.getData().toString());
                SharedPreferences.Editor e = mPreferences.edit();
                e.putString("soundfolder", mSoundFolder);
                e.apply();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setText(int id, String value) {
        TextView t = findViewById(id);
        if (t != null) t.setText(value);
    }

    public void addln(Object msg) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) { // Not in main loop.
            final String holdmsg = msg.toString();
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    addln(holdmsg);
                }
            });
            return;
        }
        if (getMemo1() == null) return;
        String s = memo1.getText().toString();
        if (s.length() > 6000) { // Stop memo getting too large.
            memo1.setText(s.substring(s.length() - 5000));
        }
        memo1.append(msg + "\n");
    }

    private TextView getMemo1() {
        memo1 = findViewById(R.id.memo1);
        return memo1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onInfoClick(View view) {
        if (getMemo1() == null) return;
        memo1.setText("");
        addln("Bluetooth: " + (mBluetoothAdapter.isEnabled() ? "Enabled" : "Disabled"));
        if (mBluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice bd : pairedDevices) {
                if (isAudio(bd)) {
                    addln(friendlyName(bd) + " " + bd.getAddress());
                }
            }
        }
        mBluetoothAdapter.startDiscovery();
    }

    String friendlyName(BluetoothDevice bd) {
        if (bd == null) return "";
        String result = getAlias(bd);
        if (result != null) return result;
        return bd.getName();
    }

    String getAlias(BluetoothDevice bd) {
        Method m = null;
        String result = null;
        try {
            m = bd.getClass().getDeclaredMethod("getAlias"); // Name may be different in different versions.
        } catch (NoSuchMethodException e) {
            m = null;
        }
        if (m == null) {
            try {
                m = bd.getClass().getDeclaredMethod("getAliasName");
            } catch (NoSuchMethodException e) {
                m = null;
            }
        }
        if (m != null) {
            try {
                result = (String) m.invoke(bd);
            } catch (IllegalAccessException e) {
                result = null;
            } catch (InvocationTargetException e) {
                result = null;
            }
        }
        return result;
    }

    /**
     * Wrapper around some reflection code to get the hidden 'connect()' method
     *
     * @return the connect(BluetoothDevice) method, or null if it could not be found
     */
    private Method getConnectMethod() {
        try {
            return BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
        } catch (NoSuchMethodException ex) {
            addln("Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.");
            return null;
        }
    }

    private Method getDisconnectMethod() {
        try {
            return BluetoothA2dp.class.getDeclaredMethod("disconnect", BluetoothDevice.class);
        } catch (NoSuchMethodException ex) {
            addln("Unable to find disconnect(BluetoothDevice) method in BluetoothA2dp proxy.");
            return null;
        }
    }

    public void connectDevice(BluetoothDevice bd) {
        Method connect = getConnectMethod();
        //If either is null, just return. The errors have already been logged
        if (connect == null || bd == null || mA2DP==null) {
            addln("No method found.");
            return;
        }

        try {
            connect.setAccessible(true);
            connect.invoke(mA2DP, bd);
        } catch (InvocationTargetException ex) {
            addln("Unable to invoke connect(BluetoothDevice) method on proxy. " + ex.toString());
        } catch (IllegalAccessException ex) {
            addln("Illegal Access! " + ex.toString());
        }
    }
    
    public void connectDevice(String address) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : pairedDevices) {
            if (bd.getAddress().equals(address)) {
                addln("Found: " + bd);
                connectDevice(bd);
            }
        }
    }

    public boolean isAudio(BluetoothDevice bd) {
        for (ParcelUuid uuid : bd.getUuids()) {
            if (uuid.toString().toUpperCase().startsWith("0000110B")) {
                return true;
            }
        }
        return false;
    }

    public void onBluetoothToggle(View view) {
        if (mBluetoothAdapter.isEnabled()) {
            addln("Disabling bluetooth...");
            mBluetoothAdapter.disable();
        } else {
            addln("Enabling bluetooth...");
            mBluetoothAdapter.enable();
        }
    }

    public void onSelectFolder(View view) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, "Choose directory"), SOUND_FOLDER_READ);
    }

    /**
     * Called when main fragment is populated.
     */
    public void mainFragmentPopulated(View mainroot) {
    }

    public void updateConnected() {
        String s=getConnectedString();
        TextView t=findViewById(R.id.currentDevice);
        if (t!=null && !t.getText().toString().equals(s)) refreshDevices();
        setText(R.id.currentDevice, s);
        setText(R.id.connectDev1,s);
        setText(R.id.connectDev2,s);
    }

    public String getConnectedString() {
        clearConnected();
        if (mA2DP == null) return "No Device Selected";
        String s = "";
        if (mA2DP != null) {
            for (BluetoothDevice bd : mA2DP.getConnectedDevices()) {
                if (!s.isEmpty()) s += "\n";
                s += friendlyName(bd);
                DevHolder h = findDevHolder(bd);
                if (h!=null) {
                    h.lastSeen=new Date();
                    h.isConnected=true;
                }

            }
        }
        if (s.isEmpty()) s = "No Device Connected";
        return s;
    }

    private void clearConnected() {
        for (DevHolder h : devices) {
            h.isConnected=false;
        }
    }

    public void setDeviceList(ListView lv) {
        if (lv == null) return;
        int oldpos = lv.getCheckedItemPosition();
        ArrayAdapter<DevHolder> la = new ArrayAdapter<DevHolder>(this, android.R.layout.simple_list_item_single_choice);
        la.addAll(devices);
        lv.setAdapter(la);
        if (oldpos>=0) lv.setItemChecked(oldpos,true);
    }

    public void setDeviceList(Spinner lv) {
        if (lv == null) return;
        int oldpos = lv.getSelectedItemPosition();
        ArrayAdapter<DevHolder> la = new ArrayAdapter<DevHolder>(this, android.R.layout.simple_spinner_item);
        la.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        la.addAll(devices);
        lv.setAdapter(la);
        if (oldpos>=0) lv.setSelection(oldpos);
    }


    public void refreshDevices() {
        setDeviceList((ListView) findViewById(R.id.deviceList));
    }

    DevHolder getSelectedDevice() {
        ListView lv = findViewById(R.id.deviceList);
        if (lv==null) return null;
        int pos=lv.getCheckedItemPosition();
        if (pos<0) return null;
        DevHolder h = (DevHolder) lv.getAdapter().getItem(pos);
        return h;
    }

    public void onConnect(View view) {
        DevHolder h=getSelectedDevice();
        if (h==null) return;
        disconnectAll();
        connectDevice(h.file);
    }

    public void onDisconnect(View view) {
        disconnectAll();
    }

    private void disconnectAll() {
        if (mA2DP != null) {
            addln("Disconnecting...");
            Method disconnect = getDisconnectMethod();
            if (disconnect == null) {
                addln("No disconnect method found.");
                return;
            }
            for (BluetoothDevice bd : mA2DP.getConnectedDevices()) {
                disconnect.setAccessible(true);
                try {
                    disconnect.invoke(mA2DP, bd);
                    addln("Disconnected.");
                } catch (IllegalAccessException e) {
                    addln("Illegal access! " + e.toString());
                } catch (InvocationTargetException e) {
                    addln("UNable to invoke disconnect.");
                }
            }
        }
    }

    public void saveDestinations() {
        SharedPreferences.Editor e = mPreferences.edit();
        for (int i=0; i<mDestinations.getCount(); i++) {
            Destination dest = (Destination) mDestinations.getItem(i);
            String key=dest.getKey();
            if (!dest.speaker) {
                e.putString(key,dest.getAddress());
            }
        }
        e.apply();
    }

    public Destination getCurrentDest() {
        return mCurrentDest;
    }

    public void setCurrentDest(Destination mCurrentDest) {
        this.mCurrentDest = mCurrentDest;
        mSounds.clear();
        if (mSoundFolder!=null && !mSoundFolder.isEmpty()) {
            try {
                Uri uri = Uri.parse(mSoundFolder);
                DocumentFile docfile = DocumentFile.fromTreeUri(this, uri);
                for (DocumentFile f : docfile.listFiles()) {
                    if (f.isDirectory()) {
                        if (mCurrentDest.speaker || (f.getName().equals(mCurrentDest.name))) {
                            populateSubFolder(f);
                        }
                    } else if (f.isFile()) {
                        mSounds.add(new DocHolder(f.getName(),f));
                    }
                }
            } catch (Exception e) {
                // Trap URI parsing error
            }
            mSounds.sort(new Comparator<DocHolder>() {
                @Override
                public int compare(DocHolder docHolder, DocHolder t1) {
                    return docHolder.compareTo(t1);
                }
            });
        }
        ListView lv = findViewById(R.id.soundList);
        if (lv!=null) lv.setAdapter(mSounds);
        if (mCurrentDest.device!=null && mCurrentDest.device.file!=null) connectDevice(mCurrentDest.device.file);
        else disconnectAll();
        mViewPager.setCurrentItem(3);
    }

    private void populateSubFolder(DocumentFile folder) {
        for (DocumentFile f : folder.listFiles()) {
            if (f.isFile()) {
                DocHolder doc = new DocHolder(folder.getName()+":"+f.getName(),f);
                mSounds.add(doc);
            }
        }
    }

    public void stopPlaying() {
        if (mPlayer!=null) {
            mPlayer.release();
            mPlayer=null;
        }
    }

    public void play(DocumentFile file) {
        stopPlaying();
        updateConnected();
        addln("Loading player...");
        mPlayer = MediaPlayer.create(this, file.getUri());
        mPlayer.start();
        addln("Playing.");
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) ||
                    intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                onInfoClick(null);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) ||
                    action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) ||
                    action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice bd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addln(action+" "+friendlyName(bd));
                updateSeen(bd,new Date());
                refreshDevices();
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                addln("Discovery done.");
                updateDeviceList();
            }
        }
    }

    private void updateSeen(BluetoothDevice bd, Date date) {
        DevHolder h = findDevHolder(bd);
        if (h==null) return;
        h.lastSeen=date;
    }

    private void populateDestinations() {
        mDestinations.clear();
        Destination speakers=new Destination("Speakers",null);
        speakers.speaker=true;
        mDestinations.add(speakers);
        if (mSoundFolder!=null && !mSoundFolder.isEmpty()) {
            try {
                Uri uri = Uri.parse(mSoundFolder);
                DocumentFile docfile = DocumentFile.fromTreeUri(this, uri);
                for (DocumentFile f : docfile.listFiles()) {
                    if (f.isDirectory()) {
                        mDestinations.add(new Destination(f.getName(),null));
                    }
                }
            } catch (Exception e) {
                // Trap URI parsing error
            }
        }
        mDestinations.sort(new Comparator<Destination>() {
            @Override
            public int compare(Destination dest, Destination source) {
                return dest.compareTo(source);
            }
        });
        for (int i=0; i<mDestinations.getCount(); i++) {
            Destination dest = mDestinations.getItem(i);
            String address=mPreferences.getString(dest.getKey(),null);
            if (address!=null) {
                dest.device=findDevHolder(address);
            }
        }
        updateDestinationList();
    }

    private void updateDestinationList() {
        ListView v = findViewById(R.id.destinations);
        if (v!=null) {
            v.setAdapter(mDestinations);
        }

    }


    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                mA2DP = (BluetoothA2dp) proxy;
                addln("Bluetooth A2DP found: " + proxy);
                updateConnected();
            }
        }

        @Override
        public void onServiceDisconnected(int i) {
            mA2DP = null;
            updateConnected();
        }
    };

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public MainActivity main;

        @Override
        public Fragment getItem(int position) {
            Fragment result;
            if (position == 1) result = new FragmentDevices();
            else if (position==2) result = new FragmentCharacters();
            else if (position==3) result = new FragmentSounds();
            else result = FragmentMain.newInstance(main);
            return result;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}

