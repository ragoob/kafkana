package com.kafkana.backend.models;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class messageModel {
    private int partition;
    private long offset;
    private String message;
    private String key;
    private Map<String, String> headers;
    private Date timestamp;

    public  messageModel(){

    }
    public messageModel(int partition, long offset, String message, String key, Map<String, String> headers, Date timestamp) {
        this.partition = partition;
        this.offset = offset;
        this.message = message;
        this.key = key;
        this.headers = headers;
        this.timestamp = timestamp;
    }

    public int getPartition() { return partition; }
    public void setPartition(int partition) { this.partition = partition; }

    public long getOffset() { return offset; }
    public void setOffset(long offset) { this.offset = offset; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeadersFormatted() {
        if (headers.isEmpty()) {
            return "empty";
        } else {
            return headers.entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));
        }
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
