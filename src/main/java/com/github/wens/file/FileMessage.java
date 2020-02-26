package com.github.wens.file;

/**
 * @author hgh
 * 19-12-11
 */
public class FileMessage {

    private Boolean file;

    /**
     * 如果是文件的话，att为文件路径，否则为返回的json对象
     */
    private Object att;

    private String fileName;

    private Long fileLength;

    public FileMessage(Object att){
        this.file = false;
        this.att = att;
    }

    public FileMessage(String[] att, String fileName, Long fileLength){
        this.file = true;
        this.att = att;
        this.fileName = fileName;
        this.fileLength = fileLength;
    }

    public Boolean isFile() {
        return file;
    }

    public void setFile(Boolean file) {
        this.file = file;
    }

    public Object getAtt() {
        return att;
    }

    public void setAtt(Object att) {
        this.att = att;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileLength() {
        return fileLength;
    }

    public void setFileLength(Long fileLength) {
        this.fileLength = fileLength;
    }
}
