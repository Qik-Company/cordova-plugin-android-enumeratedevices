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
            String label = existDevice.getString("label");
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

    private void getMics() throws JSONException {
        AudioManager audioManager = (AudioManager) this.cordova.getContext().getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] mics = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
        for (AudioDeviceInfo mic : mics) {
            JSONObject device = new JSONObject();
            device.put("deviceId", "" + mic.getId());
            device.put("groupId", "");
            device.put("kind", "audioinput");
            device.put("label", this.typeToString(mic.getType()));
            addDevice(device);
        }
    }

    private void getSpeakers() throws JSONException {
        AudioManager audioManager = (AudioManager) this.cordova.getContext().getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] speakers = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo speaker : speakers) {
            JSONObject device = new JSONObject();
            device.put("deviceId", "" + speaker.getId());
            device.put("groupId", "");
            device.put("kind", "audiooutput");
            device.put("label", this.typeToString(speaker.getType()));
            addDevice(device);
        }
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

    private String typeToString(int type){
        switch (type) {
            case AudioDeviceInfo.TYPE_AUX_LINE:
                return "Aux line-level connectors";
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
                return "Bluetooth device A2DP profile";
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                return "Bluetooth device telephony";
            case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:
                return "Built-in earphone speaker";
            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
                return "Built-in microphone";
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
                return "Built-in speaker";
            case AudioDeviceInfo.TYPE_BUS:
                return "BUS";
            case AudioDeviceInfo.TYPE_DOCK:
                return "DOCK";
            case AudioDeviceInfo.TYPE_FM:
                return "FM";
            case AudioDeviceInfo.TYPE_FM_TUNER:
                return "FM tuner";
            case AudioDeviceInfo.TYPE_HDMI:
                return "HDMI";
            case AudioDeviceInfo.TYPE_HDMI_ARC:
                return "HDMI audio return channel";
            case AudioDeviceInfo.TYPE_IP:
                return "IP";
            case AudioDeviceInfo.TYPE_LINE_ANALOG:
                return "Line analog";
            case AudioDeviceInfo.TYPE_LINE_DIGITAL:
                return "Line digital";
            case AudioDeviceInfo.TYPE_TELEPHONY:
                return "Telephony";
            case AudioDeviceInfo.TYPE_TV_TUNER:
                return "TV tuner";
            case AudioDeviceInfo.TYPE_USB_ACCESSORY:
                return "USB accessory";
            case AudioDeviceInfo.TYPE_USB_DEVICE:
                return "USB device";
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
                return "Wired headphones";
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                return "Wired headset";
            default:
            case AudioDeviceInfo.TYPE_UNKNOWN:
                return "Unknown";
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
