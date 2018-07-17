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
<p><i>Swipe left or right to select other screens.</i></p></i>
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
<p><b>Keep Awake</b> Attempt to connect to selected device to keep it awake. (Debugging)</p>
<p><b>Global Sounds</b> Select a folder include in addition to the selected sound folder.</p>
<p><b>Clear Global Sounds</b> Clear the global folder settings.</p>
<p><b>Help</b> This screen.</p>
<h2>Keep Awake</h2>
<p>Every 30 seconds RPG Talker will attempt to connect to all devices associated with characters to keep them awake. Some headsets will beep when this happens. This is a
limitation of the technology. Also, the keep-awake will only function while the RPGTalker is active. For long sessions, disable the screen timeout on your Android device.</p>
<h2>Stop</h2>
<p>The <b>Stop</b> button is on the lower right of all screens and will stop the current sound playing.</p>

