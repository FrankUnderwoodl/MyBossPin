package com.yufeng.grace.result;

import java.util.Map;

/**
 * 自定义响应数据类型枚举升级版本
 *
 * @version V2.0
 * @Title: IMOOCJSONResult.java
 * @Package com.yufeng.utils
 * @Description: 自定义响应数据结构
 * 本类可提供给 H5/ios/安卓/公众号/小程序 使用
 * 前端接受此类数据（json object)后，可自行根据业务去实现相关功能
 */
public class GraceJSONResult {

    // 响应业务状态码
    private Integer status;

    // 响应消息
    private String msg;

    // 是否成功
    private Boolean success;

    // 响应数据，可以是Object，也可以是List或Map等
    private Object data;

    /**
     * 成功返回，带有数据的，直接往OK方法丢data数据即可
     */
    public static GraceJSONResult ok(Object data) {
        return new GraceJSONResult(data);
    }

    /**
     * 成功返回，不带有数据的，直接调用ok方法，data无须传入（其实就是null）
     */
    public static GraceJSONResult ok() {
        return new GraceJSONResult(ResponseStatusEnum.SUCCESS);
    }

    /**
     * 成功返回一个带有数据的对象，这里SpringMVC会把这个对象转换成JSON字符串返回给前端
     */
    public GraceJSONResult(Object data) {

        // 状态码，这里是成功状态码：200
        this.status = ResponseStatusEnum.SUCCESS.status();
        // 响应消息，这里是成功消息：操作成功
        this.msg = ResponseStatusEnum.SUCCESS.msg();
        // 是否成功，这里是成功状态：操作成功
        this.success = ResponseStatusEnum.SUCCESS.success();
        this.data = data;
    }


    /**
     * 错误返回，直接调用error方法即可，当然也可以在ResponseStatusEnum中自定义错误后再返回也都可以
     *
     * @return
     */
    public static GraceJSONResult error() {
        return new GraceJSONResult(ResponseStatusEnum.FAILED);
    }


    public static GraceJSONResult error(String msg) {
        return new GraceJSONResult(msg);
    }


    /**
     * 错误返回，map中包含了多条错误信息，可以用于表单验证，把错误统一的全部返回出去
     *
     * @param map
     * @return
     */
    public static GraceJSONResult errorMap(Map map) {
        return new GraceJSONResult(ResponseStatusEnum.FAILED, map);
    }

    /**
     * 错误返回，直接返回错误的消息
     *
     * @param msg
     * @return
     */
    public static GraceJSONResult errorMsg(String msg) {
        return new GraceJSONResult(ResponseStatusEnum.FAILED, msg);
    }

    /**
     * 错误返回，token异常，一些通用的可以在这里统一定义
     *
     * @return
     */
    public static GraceJSONResult errorTicket() {
        return new GraceJSONResult(ResponseStatusEnum.TICKET_INVALID);
    }

    /**
     * 自定义错误范围，需要传入一个自定义的枚举，可以到[ResponseStatusEnum.java[中自定义后再传入
     *
     * @param responseStatus
     * @return
     */
    public static GraceJSONResult errorCustom(ResponseStatusEnum responseStatus) {
        return new GraceJSONResult(responseStatus);
    }


    public static GraceJSONResult exception(ResponseStatusEnum responseStatus) {
        return new GraceJSONResult(responseStatus);
    }

    /**
     * 构造方法，直接传入ResponseStatusEnum枚举，来创建一个GraceJSONResult对象
     *
     * @param responseStatus 响应状态枚举
     */
    public GraceJSONResult(ResponseStatusEnum responseStatus) {
        this.status = responseStatus.status();
        this.msg = responseStatus.msg();
        this.success = responseStatus.success();
    }

    public GraceJSONResult(ResponseStatusEnum responseStatus, Object data) {
        this.status = responseStatus.status();
        this.msg = responseStatus.msg();
        this.success = responseStatus.success();
        this.data = data;
    }

    public GraceJSONResult(ResponseStatusEnum responseStatus, String msg) {
        this.status = responseStatus.status();
        this.msg = msg;
        this.success = responseStatus.success();
    }

    public GraceJSONResult() {
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
