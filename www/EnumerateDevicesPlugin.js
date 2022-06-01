var exec = require('cordova/exec');

var originalEnumerateDevices = null;

function enumerateDevices() {
  return new Promise(function (resolve, reject) {
    exec(resolve, reject, 'EnumerateDevicesPlugin', 'enumerateDevices', []);
  });
}

function groupDevicesByKind(devices) {
  var groupedDevices = {};
  devices.forEach(function (device) {
    if (!groupedDevices[device.kind]) {
      groupedDevices[device.kind] = [];
    }
    groupedDevices[device.kind].push(device);
  });
  return groupedDevices;
}

function updateEmptyLabel(devices, nativeDevices, defaultLabel) {
  devices.forEach(function (device, index) {
    if (device.label) {
      return;
    }
    if (nativeDevices[index]) {
      device.label = nativeDevices[index].label;
      return;
    }
    device.label = defaultLabel + ' ' + (index + 1);
  });
}

// Add label for devices that have no label
function labeledDevices(devices, nativeDevices) {
  // Default label
  devices.forEach(function (device) {
    if (device.deviceId === 'default') {
      device.label = device.label || 'Default';
    }
  });

  var groupedDevices = groupDevicesByKind(devices);
  var groupedNativeDevices = groupDevicesByKind(nativeDevices);

  // Add label for camera devices
  var cameras = groupedDevices.videoinput;
  if (groupedDevices.videoinput.length === 2) {
    cameras[0].label = cameras[0].label || 'Front Camera';
    cameras[1].label = cameras[1].label || 'Back Camera';
  } else {
    updateEmptyLabel(cameras, groupedNativeDevices.videoinput, 'Camera');
  }

  // Add label for audio input devices
  updateEmptyLabel(
    groupedDevices.audioinput,
    groupedNativeDevices.audioinput,
    'Microphone'
  );

  // Add label for audio output devices
  updateEmptyLabel(
    groupedDevices.audiooutput,
    groupedNativeDevices.audiooutput,
    'Speaker'
  );
}

// Override the default enumerateDevices function to return a promise
// Because device's label is empty in Android Webview
// So we need to get the label from the native code
function overrideEnumerateDevices() {
  if (navigator.mediaDevices && navigator.mediaDevices.enumerateDevices) {
    originalEnumerateDevices = navigator.mediaDevices.enumerateDevices;
    navigator.mediaDevices.enumerateDevices = function () {
      return new Promise(function (resolve, reject) {
        originalEnumerateDevices
          .call(navigator.mediaDevices)
          .then(function (devices) {
            var isLabelEmptyFound = false;

            for (var i = 0; i < devices.length; i++) {
              if (!devices[i].label) {
                isLabelEmptyFound = true;
                break;
              }
            }

            if (isLabelEmptyFound) {
              // Add label for devices that have no label
              enumerateDevices()
                .then(function (nativeDevices) {
                  // Device is immutable, so we need to clone it
                  var mutableDevices = [];
                  devices.forEach(function (device) {
                    mutableDevices.push({
                      deviceId: device.deviceId,
                      kind: device.kind,
                      label: device.label,
                      groupId: device.groupId,
                    });
                  });

                  labeledDevices(mutableDevices, nativeDevices);
                  resolve(mutableDevices);
                })
                .catch(function () {
                  resolve(devices);
                });
            } else {
              resolve(devices);
            }
          })
          .catch(reject);
      });
    };
  }
}

function revertOriginalEnumerateDevices() {
  if (originalEnumerateDevices) {
    navigator.mediaDevices.enumerateDevices = originalEnumerateDevices;
    originalEnumerateDevices = null;
  }
}

overrideEnumerateDevices();

module.exports = {
  enumerateDevices: enumerateDevices,
  overrideEnumerateDevices: overrideEnumerateDevices,
  revertOriginalEnumerateDevices: revertOriginalEnumerateDevices,
};
