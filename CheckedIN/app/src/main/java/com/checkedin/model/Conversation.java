package com.checkedin.model;

/**
 * The Class Conversation is a Java Bean class that represents a single chat
 * conversation message.
 */
public class Conversation {

    /**
     * The msg.
     */
    private String msg;

    /**
     * The is sent.
     */
    private boolean isSent;

    /**
     * The date.
     */
    private String date;

    /**
     * Instantiates a new conversation.
     *
     * @param msg    the msg
     * @param date   the date
     * @param isSent the is sent
     */
    public Conversation(String msg, String date, boolean isSent) {
        this.msg = msg;
        this.isSent = isSent;
        this.date = date;
    }

    /**
     * Gets the msg.
     *
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Sets the msg.
     *
     * @param msg the new msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * Checks if is sent.
     *
     * @return true, if is sent
     */
    public boolean isSent() {
        return isSent;
    }

    /**
     * Sets the sent.
     *
     * @param isSent the new sent
     */
    public void setSent(boolean isSent) {
        this.isSent = isSent;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date.
     *
     * @param date the new date
     */
    public void setDate(String date) {
        this.date = date;
    }
}
