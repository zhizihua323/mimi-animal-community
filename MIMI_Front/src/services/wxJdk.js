class QQMapWX {
  constructor(options) {
    if (!options.key) {
      console.error("请传入腾讯地图Key");
      return;
    }
    this.key = options.key;
    this.output = options.output || "json";
    this.sign = options.sign || false;
  }

  // 获取当前位置
  getLocation(options) {
    if (typeof uni !== 'undefined') {
      uni.getLocation({
        type: options.type || "gcj02",
        success: (res) => options.success && options.success(res),
        fail: (res) => options.fail && options.fail(res),
        complete: (res) => options.complete && options.complete(res)
      });
    }
  }

  // 逆地理编码（坐标转地址）- 修正路径
  reverseGeocoder(options) {
    this._request("/ws/geocoder/v1", options);
  }

  // 地理编码（地址转坐标）
  geocoder(options) {
    this._request("/ws/geocoder/v1", options);
  }

  // 地点搜索
  search(options) {
    this._request("/ws/place/v1/search", options);
  }

  // 地点提示
  suggestion(options) {
    this._request("/ws/place/v1/suggestion", options);
  }

  // 距离计算
  distance(options) {
    this._request("/ws/distance/v1", options);
  }

  // 统一请求方法
  _request(url, options) {
    // 基础参数
    const data = {
      key: this.key,
      output: this.output,
      sign: this.sign
    };

    // 处理坐标参数
    if (options.location) {
      data.location = typeof options.location === 'string' 
        ? options.location 
        : `${options.location.latitude},${options.location.longitude}`;
    }

    // 处理其他常用参数
    if (options.address) data.address = options.address;
    if (options.boundary) data.boundary = options.boundary;
    if (options.query) data.query = options.query;
    if (options.region) data.region = options.region;
    if (options.get_poi !== undefined) data.get_poi = options.get_poi;
    if (options.page_size) data.page_size = options.page_size;
    if (options.page_index) data.page_index = options.page_index;

    // 支持自定义参数
    Object.assign(data, options.otherParams || {});

    if (typeof uni !== 'undefined') {
      uni.request({
        url: `https://apis.map.qq.com${url}`, // 腾讯地图API基础域名
        data,
        method: "GET",
        success: (res) => {
          if (res.statusCode === 200) {
            // 腾讯地图返回的status=0表示成功
            if (res.data.status === 0) {
              options.success && options.success(res.data);
            } else {
              options.fail && options.fail({
                status: res.data.status,
                message: res.data.message || "接口调用失败",
                request_id: res.data.request_id
              });
            }
          } else {
            options.fail && options.fail({
              status: res.statusCode,
              message: "网络请求失败",
              request_id: Date.now().toString()
            });
          }
        },
        fail: (res) => options.fail && options.fail({
          status: -1,
          message: "请求发送失败",
          request_id: Date.now().toString()
        }),
        complete: (res) => options.complete && options.complete(res)
      });
    }
  }
}

export default QQMapWX;