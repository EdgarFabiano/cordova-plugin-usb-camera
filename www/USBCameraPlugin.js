var exec = require('cordova/exec');

exports.StartCamera = function (arg0, success, error) {
    exec(success, error, 'USBCameraPlugin', 'StartCamera', []);
};
