package org.lappsgrid.serialization.service

import org.lappsgrid.discriminator.Constants
import org.lappsgrid.serialization.aas.Token

/**
 * @author Keith Suderman
 */
class GetMetadata extends ServiceCall {

    public GetMetadata() { }
    public GetMetadata(Token token) {
        super(Constants.Uri.GETMETADATA, token)
    }
}
