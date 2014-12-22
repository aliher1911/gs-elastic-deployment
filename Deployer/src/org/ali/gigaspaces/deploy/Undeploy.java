package org.ali.gigaspaces.deploy;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.pu.ProcessingUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 */
public class Undeploy {

    public static final String LOCATOR1="192.168.33.10:4174";
    public static final String LOCATOR2="192.168.33.11:4174";

    public static final String PU_NAME = "empty-pu";

    public static void main(String[] args) {
        Admin admin = new AdminFactory().useGsLogging(true).addLocator(LOCATOR1).addLocator(LOCATOR2).create();
        GridServiceManagers deployer = admin.getGridServiceManagers();
        if (deployer.waitForAtLeastOne(10, SECONDS)==null) { error("No GSM's found"); }
        ProcessingUnit pu = admin.getProcessingUnits().waitFor(PU_NAME, 10, SECONDS);
        if (pu==null) error("Failed to find pu " + PU_NAME);
        pu.undeploy();
        admin.close();
    }

    private static void error(String s) {
        System.err.println(s);
        System.exit(-1);
    }
}
