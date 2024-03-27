package cn.yoube.afrpc.core.api;

/**
 * @author LimMF
 * @since 2024/3/27
 **/
public class RpcException extends RuntimeException{

    private String errCode;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(Throwable cause, String errCode) {
        super(cause);
        this.errCode = errCode;
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     *  X-->技术异常
     *  Y-->业务异常
     *  Z-->unknown
     */
    public static final String SocketTimeoutEx = "X001" + "-" + "http_timeout exception";
    public static final String NoSuchMethod = "X001" + "-" + "http_timeout";
    public static final String UnknownEx = "Z001" + "-" + "unknown exception";
}
