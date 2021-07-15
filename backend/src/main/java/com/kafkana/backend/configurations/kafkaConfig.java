package com.kafkana.backend.configurations;

public class kafkaConfig {
    private  boolean allowtopicscreation;
    private  boolean allowtopicsdeletion;
    private  boolean allowproducemessage;
    private  long pollduration;
    private  String consumergroup;

    public boolean isAllowtopicscreation() {
        return allowtopicscreation;
    }

    public void setAllowtopicscreation(boolean allowtopicscreation) {
        this.allowtopicscreation = allowtopicscreation;
    }

    public boolean isAllowtopicsdeletion() {
        return allowtopicsdeletion;
    }

    public void setAllowtopicsdeletion(boolean allowtopicsdeletion) {
        this.allowtopicsdeletion = allowtopicsdeletion;
    }

    public boolean isAllowproducemessage() {
        return allowproducemessage;
    }

    public void setAllowproducemessage(boolean allowproducemessage) {
        this.allowproducemessage = allowproducemessage;
    }


    public long getPollduration() {
        return pollduration;
    }

    public void setPollduration(long pollduration) {
        this.pollduration = pollduration;
    }

    public String getConsumergroup() {
        return consumergroup;
    }

    public void setConsumergroup(String consumergroup) {
        this.consumergroup = consumergroup;
    }
}
