package io.voluble.michellelib.config;

/**
 * Runtime exception thrown for YAML document load/save errors.
 */
public final class YamlDocumentException extends RuntimeException {
    public YamlDocumentException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public YamlDocumentException(final String message) {
        super(message);
    }
}



