package app;

final class BadRequestException extends RuntimeException {
    BadRequestException(String message) {
        super(message);
    }

    BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
