package org.ali.gigaspaces.deploy;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;

import static org.openspaces.core.util.MemoryUnit.*;

import java.io.File;
import static java.util.concurrent.TimeUnit.*;

public class Elastic {

    public static final String LOCATOR1="192.168.33.10:4174";
    public static final String LOCATOR2="192.168.33.11:4174";

    public static final String BASE_DEPLOY_DIR = "/Users/ali/Documents/Gigaspaces/vms/jars";
    public static final String PRESERVICE_JAR = "empty-pu.jar";
    public static final String STATELESS_JAR = "stateless-pu.jar";
    public static final String STATEFUL_JAR = "space-pu.jar";

    public static final int PARTITIONS = 4;

    public static void main(String[] args) {
        Admin admin = new AdminFactory().useGsLogging(true).addLocator(LOCATOR1).addLocator(LOCATOR2).create();
        if (admin.getElasticServiceManagers().waitForAtLeastOne(10, SECONDS)==null) { error("No ESM's found."); }
        GridServiceManagers deployer = admin.getGridServiceManagers();
        if (deployer.waitForAtLeastOne(10, SECONDS)==null) { error("No GSM's found"); }

        System.out.println("Found GSM and ESM. Proceeding to deployment.");

        deployPreSpacePu(deployer, true);

        System.out.println("Pre service deployed, proceeding to space PU.");

        deployElasticSpace(deployer, true);

        System.out.println("Space deployed, proceeding to stateless PU.");

        deployPostSpacePu(deployer, true);

        System.out.println("Stateless PU deployed. Closing admin.");

        admin.close();
    }

    private static void deployPostSpacePu(GridServiceManagers deployer, boolean wait) {
        ElasticStatelessProcessingUnitDeployment elasticPu = new ElasticStatelessProcessingUnitDeployment(new File(BASE_DEPLOY_DIR + "/" + STATELESS_JAR))
                .addCommandLineArgument("-Dsecurity.zone=ZoneA")
                .memoryCapacityPerContainer(128, MEGABYTES)
                .scale(new ManualCapacityScaleConfigurer()
                        .numberOfCpuCores(1)
                        .create())
                .sharedMachineProvisioning("zoneASpace", new DiscoveredMachineProvisioningConfigurer()
                        .addGridServiceAgentZone("zone1")
                        .create())
                .addDependency("space-pu");
        ProcessingUnit pu = deployer.deploy(elasticPu);
        if (wait && !pu.waitFor(1, 120, SECONDS)) { error("Failed to deploy stateless elastic pu"); }
    }

    private static void deployPreSpacePu(GridServiceManagers deployer, boolean wait) {
        ElasticStatelessProcessingUnitDeployment elasticPu = new ElasticStatelessProcessingUnitDeployment(new File(BASE_DEPLOY_DIR + "/" + PRESERVICE_JAR))
                .addCommandLineArgument("-Dsecurity.zone=ZoneA")
                .memoryCapacityPerContainer(128, MEGABYTES)
                .scale(new ManualCapacityScaleConfigurer()
                        .numberOfCpuCores(1)
                        .create())
                .sharedMachineProvisioning("zoneASpace", new DiscoveredMachineProvisioningConfigurer()
                        .addGridServiceAgentZone("zone1")
                        .create());
        ProcessingUnit pu = deployer.deploy(elasticPu);
        if (wait && !pu.waitFor(1, 120, SECONDS)) { error("Failed to deploy stateless elastic pu"); }
    }

    private static void deployElasticSpace(GridServiceManagers deployer, boolean wait) {
        ElasticStatefulProcessingUnitDeployment elasticSpace = new ElasticStatefulProcessingUnitDeployment(new File(BASE_DEPLOY_DIR + "/" + STATEFUL_JAR))
                .addCommandLineArgument("-Dsecurity.zone=ZoneA")
                .memoryCapacityPerContainer(128, MEGABYTES)
                .numberOfPartitions(PARTITIONS)
                .scale(new ManualCapacityScaleConfigurer()
                        .memoryCapacity(256, MEGABYTES)
                        .create())
                .sharedMachineProvisioning("zoneASpace", new DiscoveredMachineProvisioningConfigurer()
                        .addGridServiceAgentZone("zone1")
                        .create())
                .addDependency("empty-pu");
        ProcessingUnit space = deployer.deploy(elasticSpace);
        if (wait && !space.waitFor(PARTITIONS*2, 120, SECONDS)) { error("Failed to deploy stateful elastic pu"); }
    }

    private static void error(String s) {
        System.err.println(s);
        System.exit(-1);
    }
}
