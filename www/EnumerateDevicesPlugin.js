var exec = require('cordova/exec');

module.exports = {
  getEnumerateDevices: getEnumerateDevices,
};

function enumerateDevices() {
  return new Promise(function (resolve, reject) {
    exec(resolve, reject, 'EnumerateDevicesPlugin', 'enumerateDevices', []);
  });
}
