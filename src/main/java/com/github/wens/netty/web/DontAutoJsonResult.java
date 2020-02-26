package com.github.wens.netty.web;

public class DontAutoJsonResult<T> {
    private T res;

    private String contentType;

    public DontAutoJsonResult(T res){
        this.res = res;
    }

    public DontAutoJsonResult(T res, String contentType){
        this.res = res;
        this.contentType = contentType;
    }

    public T getRes() {
        return res;
    }

    public void setRes(T res) {
        this.res = res;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
