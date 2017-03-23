package com.alpha.csv.exceptions;

public class CsvException extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CsvException() {
        super();
    }

    public CsvException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvException(String message) {
        super(message);
    }

    public CsvException(Throwable cause) {
        super(cause);
    }

}
