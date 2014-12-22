package org.ali.gigaspaces.service;

import org.ali.gigaspaces.remote.RemoteInterface;
import org.openspaces.core.GigaSpace;
import org.openspaces.remoting.RemotingService;

import javax.annotation.PostConstruct;

/**
 * Stateless bean
 */
@RemotingService
public class StatelessServiceBean implements RemoteInterface {

    private GigaSpace gigaSpace;

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    @PostConstruct
    public void init() {
        // initialize
    }

    @Override
    public void performOp() {

    }

    @Override
    public void performOp(String argument) {

    }
}
