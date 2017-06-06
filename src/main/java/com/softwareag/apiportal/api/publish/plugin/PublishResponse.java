/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareag.apiportal.api.publish.plugin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author VDE
 */
@JsonInclude(Include.NON_EMPTY)
public class PublishResponse implements Serializable {
    
     private static final long serialVersionUID = 1L;
    
     @XmlElement(name="status")
    private String status;
     @XmlElement(name="message")
    private String message;
     @XmlElement(name="link")
    private String link;
     @XmlElement(name="itemLink")
    private String itemLink;
     @XmlElement(name="name")
    private String name;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getItemLink() {
        return itemLink;
    }

    public void setItemLink(String itemLink) {
        this.itemLink = itemLink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
