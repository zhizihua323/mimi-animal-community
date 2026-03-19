package com.example.coreshopping.utils;

public enum OrderEnum {

    // 订单状态
    ORDER_STATUS_PENDING(1, "待付款"),
    ORDER_STATUS_PAID(2, "已付款"),
    ORDER_STATUS_SHIPPED(3, "已发货"),
    ORDER_STATUS_DELIVERED(4, "已送达"),
    ORDER_STATUS_COMPLETED(5, "已完成"),
    ORDER_STATUS_CANCELLED(6, "已取消"),

    // 支付状态
    PAY_STATUS_UNPAID(0, "未支付"),
    PAY_STATUS_PAID(1, "已支付"),
    PAY_STATUS_REFUNDED(2, "已退款"),

    // 支付方式
    PAY_METHOD_ALIPAY(1, "支付宝"),
    PAY_METHOD_WECHAT(2, "微信支付"),
    PAY_METHOD_UNIONPAY(3, "银联支付");
    private final Integer code;
    private final String desc;

    // 构造方法
    OrderEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 获取编码
    public Integer getCode() {
        return code;
    }

    // 获取描述
    public String getDesc() {
        return desc;
    }

    // 根据编码获取枚举
    public static OrderEnum getByCode(Integer code) {
        for (OrderEnum orderEnum : values()) {
            if (orderEnum.code.equals(code)) {
                return orderEnum;
            }
        }
        return null;
    }
}
