package com.urlsearch.backend.exception;

import java.util.List;

/**
 * @author matheus.graca
 * @version : $<br/>
 * : $
 * @since 28/01/2021 11:06
 */
public class MandatoryFieldNotPresentException extends RuntimeException {

    private List<String> fieldNotPresent;

    public MandatoryFieldNotPresentException (List<String> fieldNotPresent) {
        this.fieldNotPresent = fieldNotPresent;
    }

    public List<String> getFieldNotPresent () {
        return fieldNotPresent;
    }

}
