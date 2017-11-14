package javax.jms;

public class JMSException extends Exception {
    public JMSException() {
        super();
    }

    public JMSException(String message) {
        super(message);
    }

    public JMSException(String message, Throwable cause) {
        super(message, cause);
    }

    public JMSException(Throwable cause) {
        super(cause);
    }

    protected JMSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
