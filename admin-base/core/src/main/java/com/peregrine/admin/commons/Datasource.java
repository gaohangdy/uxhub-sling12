/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peregrine.admin.commons;

/**
 *
 * @author gaoha
 */
public class Datasource {

    private String value;
    private String name;

    public Datasource(String value, String name) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
