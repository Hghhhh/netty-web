package com.github.wens.file;

public class ImageMessage {

    private Boolean image;

    private Object att;

    private String[] paths;

    private Integer size;

    private String imageType;

    public ImageMessage(String[] paths, Integer size, String imageType){
        this.image = true;
        this.paths = paths;
        this.size = size;
        this.imageType = imageType.equals("png") ? "png" : "jpeg";
    }

    public ImageMessage(Object att){
        this.image = false;
        this.att = att;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPath(String[] paths) {
        this.paths = paths;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public Boolean getImage() {
        return image;
    }

    public void setImage(Boolean image) {
        this.image = image;
    }

    public Object getAtt() {
        return att;
    }

    public void setAtt(Object att) {
        this.att = att;
    }
}
