package org.mimi.exception;

public class BusinessException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private Integer code;

    private String message;

    public BusinessException(String message){
        super(message);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(String message,Integer code){
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message,Throwable e){
        super(message,e);
        this.message = message;
    }
}
