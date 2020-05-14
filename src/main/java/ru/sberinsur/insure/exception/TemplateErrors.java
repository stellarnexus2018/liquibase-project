package ru.sberinsur.insure.exception;

import ru.sberinsur.insure.integrations.exception.InsureErrorCode;

/**
 *
 * Sevices errors
 *
 */
public enum TemplateErrors implements InsureErrorCode {

    TEMPLATE_ERRORS(1, "Ошибка");


    private Integer errorCode;
    private String errorDescription;

    TemplateErrors(Integer errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorDescription() {
        return errorDescription;
    }
}
