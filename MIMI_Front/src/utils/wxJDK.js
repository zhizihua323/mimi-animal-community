/**
 * 腾讯地图微信小程序SDK v1.2
 * 已添加 module.exports 导出，适配小程序 require 语法
 */
function QQMapWX(options) {
  this.key = options.key;
  this.output = options.output || "json";
  this.sign = options.sign || false;
}
QQMapWX.prototype = {
  getLocation: function (options) {
    var that = this;
    wx.getLocation({
      type: options.type || "wgs84",
      success: function (res) {
        options.success && options.success(res);
      },
      fail: function (res) {
        options.fail && options.fail(res);
      },
      complete: function (res) {
        options.complete && options.complete(res);
      }
    });
  },
  reverseGeocoder: function (options) {
    this._request("/ws/geocoder/v1/", options);
  },
  geocoder: function (options) {
    this._request("/ws/geocoder/v1/", options);
  },
  search: function (options) {
    this._request("/ws/place/v1/search", options);
  },
  suggestion: function (options) {
    this._request("/ws/place/v1/suggestion", options);
  },
  distance: function (options) {
    this._request("/ws/distance/v1/", options);
  },
  _request: function (url, options) {
    var that = this;
    var data = options.location ? {
      location: options.location.latitude + "," + options.location.longitude,
      key: that.key,
      output: that.output,
      sign: that.sign
    } : {
      key: that.key,
      output: that.output,
      sign: that.sign
    };
    if (options.address) data.address = options.address;
    if (options.addressComponent) data.address_component = options.addressComponent;
    if (options.boundary) data.boundary = options.boundary;
    if (options.page_size) data.page_size = options.page_size;
    if (options.page_index) data.page_index = options.page_index;
    if (options.query) data.query = options.query;
    if (options.region) data.region = options.region;
    if (options.suggestion) data.suggestion = options.suggestion;
    if (options.from_coord) data.from_coord = options.from_coord;
    if (options.to_coord) data.to_coord = options.to_coord;
    if (options.mode) data.mode = options.mode;
    wx.request({
      url: "https://apis.map.qq.com" + url,
      data: data,
      method: "GET",
      success: function (res) {
        if (res.statusCode === 200 && res.data.status === 0) {
          options.success && options.success(res.data);
        } else {
          options.fail && options.fail(res.data);
        }
      },
      fail: function (res) {
        options.fail && options.fail(res);
      },
      complete: function (res) {
        options.complete && options.complete(res);
      }
    });
  }
};
// 关键：导出SDK类，供页面引用
exports QQMapWX;