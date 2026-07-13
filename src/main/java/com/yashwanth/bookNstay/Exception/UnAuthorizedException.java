package com.yashwanth.bookNstay.Exception;

public class UnAuthorizedException extends RuntimeException {
    public UnAuthorizedException(String message) {
        super(message);
    }
}
