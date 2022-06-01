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

    static final String BUILTIN_MIC = "Built-in Microphone";
    static final String BLUETOOTH_MIC = "Bluetooth Microphone";
    static final String WIRED_MIC = "Wired Microphone";
    static final String USB_MIC = "USB Microphone";
    static final String UNKNOWN_MIC = "Unknown Microphone";

    static final String BUILTIN_SPEAKER = "Built-in Speaker";
    static final String BUILTIN_EARPIECE_SPEAKER = "Built-in Earphone Speaker";
    static final String BLUETOOTH_SPEAKER = "Bluetooth Speaker";
    static final String WIRED_SPEAKER = "Wired Speaker";
    static final String UNKNOWN_SPEAKER = "Unknown Speaker";

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
        getSpeakers();
        getCameras();
        return devices;
    }

    private void addDevice(JSONObject device) throws JSONException {
        int number = 0;
        for (int i = 0; i < devices.length(); i++) {
            JSONObject existDevice = devices.getJSONObject(i);
            String label = existDevice.getString("label");
            if (label.equals(device.getString("label"))) {
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
            device.put("label", this.getMicType(mic));
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
            device.put("label", this.getSpeakerType(speaker));
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

    private String getMicType(AudioDeviceInfo input) {
        String deviceType;

        switch (input.getType()) {
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                deviceType = BLUETOOTH_MIC;
                break;
            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
                deviceType = BUILTIN_MIC;
                break;
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                deviceType = WIRED_MIC;
                break;
            case AudioDeviceInfo.TYPE_USB_DEVICE:
                deviceType = USB_MIC;
                break;
            default:
                deviceType = UNKNOWN_MIC;
                break;
        }

        return input.getProductName().toString() + " " +  deviceType;
    }

    private String getSpeakerType(AudioDeviceInfo input) {
        String deviceType;
        switch (input.getType()) {
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
                deviceType = BUILTIN_SPEAKER;
                break;
            case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:
                deviceType = BUILTIN_EARPIECE_SPEAKER;
                break;
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
                deviceType = BLUETOOTH_SPEAKER;
                break;
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                deviceType = WIRED_SPEAKER;
                break;
            default:
                deviceType = UNKNOWN_SPEAKER;
                break;
        }
        return input.getProductName().toString() + " " + deviceType;
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
