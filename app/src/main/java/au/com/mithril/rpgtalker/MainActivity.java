package au.com.mithril.rpgtalker;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
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


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    TextView memo1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String mSoundFolder;

    public String getSoundFolder() {
        return mSoundFolder;
    }

    public void setSoundFolder(String mSoundFolder) {
        this.mSoundFolder = mSoundFolder;
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

        setSoundFolder(mPreferences.getString("soundfolder", null));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.A2DP);
        mTimer = new Timer("ticker",true);
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
        },1000,1000);
        updateDeviceList();
    }


    private void updateDeviceList() {
        Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice bd : paired) {
            if (isAudio(bd)) {
                updateDevice(bd);
            }
        }
        for (Iterator<DevHolder> it=devices.iterator(); it.hasNext();) {
            DevHolder h=it.next();
            if (!paired.contains(h.file)) it.remove();
        }
        Collections.sort(devices);
        setDeviceList((ListView) findViewById(R.id.deviceList));
    }


    private void updateDevice(BluetoothDevice bd) {
      DevHolder h = findDevHolder(bd);
      if (h==null) {
          h=new DevHolder(friendlyName(bd),bd);
          devices.add(h);
      }
      h.name=friendlyName(bd);
      h.file=bd;
    }

    private DevHolder findDevHolder(BluetoothDevice bd) {
        for (DevHolder h : devices) {
            if (h.file.getAddress().equals(bd.getAddress())) return h;
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
        setText(R.id.currentDevice, getConnectedString());
    }

    public String getConnectedString() {
        if (mA2DP == null) return "No Device Selected";
        String s = "";
        if (mA2DP != null) {
            for (BluetoothDevice bd : mA2DP.getConnectedDevices()) {
                if (!s.isEmpty()) s += "\n";
                s += friendlyName(bd);
            }
        }
        if (s.isEmpty()) s = "No Device Connected";
        return s;
    }

    public void setDeviceList(ListView lv) {
        if (lv==null) return;
        ArrayAdapter<DevHolder> la = new ArrayAdapter<DevHolder>(this, android.R.layout.simple_list_item_single_choice);
        la.addAll(devices);
        lv.setAdapter(la);
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) ||
                    intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                onInfoClick(null);
            }
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
            if (position == 1) result = new FragmentTemp();
            else result = FragmentMain.newInstance(main);
            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}

