package com.example.churnpoc.controller;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Turns scan failures into messages an agent can act on -
 * "service down" and "service rejected the data" need different fixes.
 */
public final class ScanErrorMessages {

    private ScanErrorMessages() {
    }

    public static String describe(Exception exc) {
        if (exc instanceof ResourceAccessException) {
            return "the ML service is unreachable - is it running?";
        }
        if (exc instanceof RestClientResponseException responseExc) {
            return "the ML service rejected the request (HTTP "
                    + responseExc.getStatusCode().value() + ").";
        }
        return exc.getMessage();
    }
}
