# RPGTalkerAndroid
Use cheap earpieces to pass audio queues in an RPG setting.

<p>RPG Talker allows you to hook to an arbitrary number of Bluetooth headsets, and send short pre-recorded messages.</p>
<h2>Pairing</h2>
<p>Use your normal Bluetooth management screen to pair headsets/earpieces. Remember to give them meaningful names.</p>
<h2>Sound Folder</h2>
<p>Use a file manager to copy sound files into a folder. For each Character, create a subfolder. Each subfolder can contain
sounds meant only for that character. Sound files in the main folder are for all characters. (Subfolder may be empty)</p>
<p>Example:</p>
<ul>
    <li>itsatrap.mp3</li>
    <li>lying.mp3</li>
    <li>Cleo
        <ul>
            <li>yousensedanger.mp3</li>
            <li>imahallucination.mp3</li>
        </ul>
    </li>
    <li>Maggie</li>
    <li>Dusty</li>
</ul>
<h2>Screens</h2>
<p><i>Swipe left or right to select other screens. Hitting the <b>BACK</b> button will go left. Hitting back at the first screen will quit the application.</i></p>
<h3>Main</h3>
<p><b>INFO</b> lists paired devices, and does a discovery to see which devices are within range.</p>
<p><b>TOGGLE BLUETOOTH</b> toggles the bluetooth adapter on or off.</p>
<p><b>Select Folder</b> opens a folder selection dialog to locate the <b>Sound Folder</b></p>
<p>The selected folder is shown and currently connected sound devices are displayed under the buttons. Under that is a screen showing progress messages, useful for debugging.</p>
<h3>Devices</h3>
<p>A selectable list of paired devices. You can use the <b>Connect</b> and <b>Disconnect</b> button to connect and disconnect the selected device from the sound system.</p>
<p>Devices currently in range will show a number in brackets. This is how long ago the device was seen (in seconds). The currently selected device shows an asterisk (*)</p>
<h3>Destinations</h3>
<p>Selectable list of Character/Destinations. To assign a device to a Character, do a <b>Long Press</b> on a Character. A list of available devices will pop up.</p>
<p>Speakers: There is a special destination called <b>Speakers</b> which will play on the phone speakers</p>
<p>Current: There is a special destination called <b>Current</b> which will allow you to play all sounds to the currently selected device.</p>
<p>Tapping the Destination will attempt to connect to the associated headset, and bring up a list of available sounds (include sounds just for that player.</p>
<p><i>Note that it takes a few seconds to connect. The currently selected device is displayed at the top of the screen. You need to wait for this to update before trying to play messages.</i></p>
<h2>Menu Options</h2>
<p><b>Details</b> Show details on the currently selected device.</p>
<p><b>Global Sounds</b> Select a folder include in addition to the selected sound folder.</p>
<p><b>Clear Global Sounds</b> Clear the global folder settings.</p>
<p><b>HFP Keep Awake</b> Switch between Ping and HFP keepawake strategies.</p>
<p><b>Help</b> This screen.</p>
<p><b>About</b> Details about RPGTalker.</p>
<h2>Keep Awake</h2>
<p>Non-connected earpieces will often power down after a short period to save battery. RPGTalker uses one of two techniques to keep the devices awake when not currently in use.
<ul>
    <li><b>Ping</b> The default, pings the device every 30 seconds. This may result in annoying beeps, but should in theory work for a potentially large number of devics.</li>
    <li><b>HFP</b> will attempt to connect via the HFP protocol and keep that connection open indefinitely. This is much more reliable and less disruptive, but due to bluetooth limitations is only good for up to 7 devices at a time.</li>
</ul>
<p>The keep-awake will only function while the RPGTalker is active. For long sessions, disable the screen timeout on your Android device.</p>
<h2>Stop</h2>
<p>The <b>Stop</b> button is on the lower right of all screens and will stop the current sound playing.</p>
<h2>Device Details</h2>
<p>When the device is displayed, it looks like the following:
    <br><b>S530 Blue(9)*</b>
    <br><b>Phillip SH550(0)H B5</b>
    <br>(9) = time in seconds since last seen. If this number gets much above 30, the device is probably powered down.
    <br>* = connected (to the sound system, A2DP)
    <br>h=attempting HFP connection, H=HFP connection established. (Only occurs if HFP Keep Awake is selected.)
    <br>B5=battery level, which is between 1 and 9. Not available on all devices. Only updated in HFP mode.
</p>