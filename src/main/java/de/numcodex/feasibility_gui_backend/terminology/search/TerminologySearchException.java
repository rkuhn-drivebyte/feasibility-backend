package de.numcodex.feasibility_gui_backend.terminology.search;

/**
 * An exception for when an error occurs while searching through the terminology.
 */
public class TerminologySearchException extends Exception {
    public TerminologySearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
