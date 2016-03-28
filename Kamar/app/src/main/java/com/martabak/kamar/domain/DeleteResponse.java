package com.martabak.kamar.domain;

/**
 * A response from a DELETE request.
 */
public class DeleteResponse {

    /**
     * The operation status.
     */
    public final boolean ok;

    public DeleteResponse() {
        this.ok = false;
    }

}
