package es.us.contextualy.observable.google;

public class GoogleAPIConnectionSuspendedException extends RuntimeException {

    private final int cause;

    GoogleAPIConnectionSuspendedException(int cause) {
        this.cause = cause;
    }

    public int getErrorCause() {
        return cause;
    }
}