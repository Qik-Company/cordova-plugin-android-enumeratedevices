package cordova.plugin.android.enumeratedevices;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;

import android.media.AudioManager;
import android.media.AudioDeviceInfo;
import android.os.Build;
import androidx.annotation.RequiresApi;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;

@RequiresApi(api = Build.VERSION_CODES.M)
public class EnumerateDevicesPlugin extends CordovaPlugin {

    static final String FRONT_CAM = "Front Camera";
    static final String BACK_CAM = "Back Camera";
    static final String EXTERNAL_CAM = "External Camera";
    static final String UNKNOWN_CAM = "Unknown Camera";

    private JSONArray devices;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("enumerateDevices")) {
            try {
                callbackContext.success(enumerateDevices());
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
            return true;
        }

        return false;
    }

    private JSONArray enumerateDevices() throws JSONException, CameraAccessException {
        devices = new JSONArray();
        getMics();
        getCameras();
        getSpeakers();
        return devices;
    }

    private void addDevice(JSONObject device) throws JSONException {
        int number = 0;
        for (int i = 0; i < devices.length(); i++) {
            JSONObject existDevice = devices.getJSONObject(i);
            if (    existDevice.getString("kind").equals(device.getString("kind")) &&
                    existDevice.getString("label").equals(device.getString("label"))) {
                number++;
            }
        }
        if (number > 0) {
            String numberedLabel = device.getString("label") + " " + number;
            device.put("label", numberedLabel);
        }
        devices.put(device);
    }

    private void sortedAndAddAudioDevice(ArrayList<JSONObject> preSortedAudioDevices) throws JSONException {
        // Move built-in to first
        Collections.sort(preSortedAudioDevices, (obj1, obj2) -> {
            int obj1Order = 0;
            int obj2Order = 0;
            try {
                obj1Order = obj1.getInt("order");
            } catch (Exception ignored) {
            }
            try {
                obj2Order = obj2.getInt("order");
            } catch (Exception ignored) {
            }
            return obj2Order - obj1Order;
        });

        for (JSONObject device: preSortedAudioDevices) {
            addDevice(device);
        }
    }

    private void getMics() throws JSONException {
        AudioManager audioManager = (AudioManager) this.cordova.getContext().getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] mics = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
        ArrayList<JSONObject> preSortedMics = new ArrayList<>();
        for (AudioDeviceInfo mic : mics) {
            JSONObject device = new JSONObject();
            device.put("deviceId", "" + mic.getId());
            device.put("groupId", "");
            device.put("kind", "audioinput");
            addLabelByType(device, mic.getType());
            preSortedMics.add(device);
        }
        sortedAndAddAudioDevice(preSortedMics);
    }

    private void getSpeakers() throws JSONException {
        AudioManager audioManager = (AudioManager) this.cordova.getContext().getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] speakers = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        ArrayList<JSONObject> preSortedSpeakers = new ArrayList<>();
        for (AudioDeviceInfo speaker : speakers) {
            JSONObject device = new JSONObject();
            device.put("deviceId", "" + speaker.getId());
            device.put("groupId", "");
            device.put("kind", "audiooutput");
            addLabelByType(device, speaker.getType());
            preSortedSpeakers.add(device);
        }
        sortedAndAddAudioDevice(preSortedSpeakers);
    }

    private void getCameras() throws JSONException, CameraAccessException {
        CameraManager cameraManager = (CameraManager) this.cordova.getContext().getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIds = cameraManager.getCameraIdList();
        for (String cameraId : cameraIds) {
            JSONObject device = new JSONObject();
            String label = this.getCameraType(cameraManager.getCameraCharacteristics(cameraId));
            device.put("deviceId", cameraId);
            device.put("groupId", "");
            device.put("kind", "videoinput");
            device.put("label", label);
            addDevice(device);
        }
    }

    private void addLabelByType(JSONObject device, int type) throws JSONException {
        switch (type) {
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                device.put("label", "Bluetooth device");
                device.put("type", "TYPE_BLUETOOTH_SCO");
                break;
            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
                device.put("label", "Built-in microphone");
                device.put("type", "TYPE_BUILTIN_MIC");
                device.put("order", 1);
                break;
            case AudioDeviceInfo.TYPE_TELEPHONY:
                device.put("label", "Telephony");
                device.put("type", "TYPE_TELEPHONY");
//                device.put("order", 1);
                break;
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
                device.put("label", "Built-in speaker");
                device.put("type", "TYPE_BUILTIN_SPEAKER");
                device.put("order", 1);
                break;
            case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:
                device.put("label", "Built-in earphone speaker");
                device.put("type", "TYPE_BUILTIN_EARPIECE");
                device.put("order", 1);
                break;
            case AudioDeviceInfo.TYPE_BUS:
                device.put("label", "Bus");
                device.put("type", "TYPE_BUS");
                break;
            case AudioDeviceInfo.TYPE_DOCK:
                device.put("label", "Dock");
                device.put("type", "DOCK");
                break;
            case AudioDeviceInfo.TYPE_FM:
                device.put("label", "FM");
                device.put("type", "TYPE_FM");
                break;
            case AudioDeviceInfo.TYPE_FM_TUNER:
                device.put("label", "FM tuner");
                device.put("type", "TYPE_FM_TUNER");
                break;
            case AudioDeviceInfo.TYPE_HDMI:
                device.put("label", "HDMI");
                device.put("type", "TYPE_HDMI");
                break;
            case AudioDeviceInfo.TYPE_HDMI_ARC:
                device.put("label", "HDMI audio return channel");
                device.put("type", "TYPE_HDMI_ARC");
                break;
            case AudioDeviceInfo.TYPE_IP:
                device.put("label", "IP");
                device.put("type", "TYPE_IP");
                break;
            case AudioDeviceInfo.TYPE_LINE_ANALOG:
                device.put("label", "Line analog");
                device.put("type", "TYPE_LINE_ANALOG");
                break;
            case AudioDeviceInfo.TYPE_LINE_DIGITAL:
                device.put("label", "Line digital");
                device.put("type", "TYPE_LINE_DIGITAL");
                break;
            case AudioDeviceInfo.TYPE_TV_TUNER:
                device.put("label", "TV tuner");
                device.put("type", "TYPE_TV_TUNER");
                break;
            case AudioDeviceInfo.TYPE_USB_ACCESSORY:
                device.put("label", "USB accessory");
                device.put("type", "TYPE_USB_ACCESSORY");
                break;
            case AudioDeviceInfo.TYPE_USB_DEVICE:
                device.put("label", "USB device");
                device.put("type", "TYPE_USB_DEVICE");
                break;
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
                device.put("label", "Wired headphones");
                device.put("type", "TYPE_WIRED_HEADPHONES");
                break;
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                device.put("label", "Wired headset");
                device.put("type", "TYPE_WIRED_HEADSET");
                break;
            default:
                device.put("label", "Unknown");
                device.put("type", "TYPE_UNKNOWN");
                break;
        }
    }

    private String getCameraType(CameraCharacteristics input) {
        switch (input.get(CameraCharacteristics.LENS_FACING)) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                return FRONT_CAM;
            case CameraCharacteristics.LENS_FACING_BACK:
                return BACK_CAM;
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                return EXTERNAL_CAM;
            default:
                return UNKNOWN_CAM;
        }
    }
}
