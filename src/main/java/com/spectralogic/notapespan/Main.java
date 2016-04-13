/*
 * *****************************************************************************
 *    Copyright 2015 Spectra Logic Corporation. All Rights Reserved.
 *    Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *    this file except in compliance with the License. A copy of the License is located at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file.
 *    This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *    CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *    specific language governing permissions and limitations under the License.
 *  ****************************************************************************
 */

package com.spectralogic.notapespan;

import ch.qos.logback.classic.Level;
import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.models.common.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Main {


    public static void main(final String[] args) {
        // args are endpoint, accessid, secret key, userid, storage_domain_id
        final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        // Turn off logging
        rootLogger.setLevel(Level.OFF);

        if (args.length < 4 || args.length > 5) {
            System.out.println("Missing the required number of arguments.  They are: endpoint access_id secret_key user_id [storage_domain_id]");
            System.out.println("Args " + Arrays.toString(args));
            return;
        }

        try (final Ds3Client client = Ds3ClientBuilder.create(args[0], new Credentials(args[1], args[2])).withHttps(false).build()) {

            final UUID storageDomainId;

            final GetSystemInformationSpectraS3Response systemInformationSpectraS3 = client.getSystemInformationSpectraS3(new GetSystemInformationSpectraS3Request());
            verifyBuildNumber(systemInformationSpectraS3.getSystemInformationResult().getBuildInformation());

            if (args.length != 5) {
                final GetStorageDomainsSpectraS3Response storageDomainsSpectraS3 = client.getStorageDomainsSpectraS3(new GetStorageDomainsSpectraS3Request());
                final List<StorageDomain> storageDomains = storageDomainsSpectraS3.getStorageDomainListResult().getStorageDomains();
                if (storageDomains.size() == 0) {
                    System.out.println("The black pearl you are communicating to does not have any storage domains");
                    return;
                } else if (storageDomains.size() > 1) {
                    System.out.println("Please rerun adding the storage domain id to use:");

                    // print all the storage domains
                    printStorageDomains(storageDomains);
                    return;
                }
                else {
                    storageDomainId = storageDomains.get(0).getId();
                }
            } else {
                storageDomainId = UUID.fromString(args[4]);
            }

            final PutDataPolicySpectraS3Response noTapeSpan = client.putDataPolicySpectraS3(new PutDataPolicySpectraS3Request("no_tape_span").withBlobbingEnabled(false).withVersioning(VersioningLevel.NONE));
            client.putDataPersistenceRuleSpectraS3(new PutDataPersistenceRuleSpectraS3Request(noTapeSpan.getDataPolicyResult().getId(), DataIsolationLevel.BUCKET_ISOLATED, storageDomainId, DataPersistenceRuleType.PERMANENT));
            client.modifyUserSpectraS3(new ModifyUserSpectraS3Request(args[3]).withDefaultDataPolicyId(noTapeSpan.getDataPolicyResult().getId()));

            System.out.println("Successfully created the data policy and assigned it as the default data policy for user: " + args[3]);

        } catch (final IOException|SignatureException e) {
            System.out.println("Failed to connect to black pearl");
            e.printStackTrace();
        }
    }

    private static void printStorageDomains(final List<StorageDomain> storageDomains) {
        System.out.println(ASCIITable.getInstance().getTable(getHeaders(), formatStorageDomainList(storageDomains)));
    }

    private static String[][] formatStorageDomainList(final List<StorageDomain> storageDomains) {
        final String[][] formatArray = new String[storageDomains.size()][];

        for (int i = 0; i < storageDomains.size(); i++) {
            final StorageDomain storageDomain = storageDomains.get(i);
            final String[] storageDomainArray = new String[2];
            storageDomainArray[0] = storageDomain.getName();
            storageDomainArray[1] = storageDomain.getId().toString();
            formatArray[i] = storageDomainArray;
        }

        return formatArray;
    }

    private static ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Id", ASCIITable.ALIGN_LEFT)
        };
    }

    private static void verifyBuildNumber(final BuildInformation buildInformation) {
        if (!buildInformation.getVersion().startsWith("3")) {
            System.out.println("The black pearl must be at version 3 but is at version " + buildInformation.getVersion().charAt(0));
            System.exit(1);
        }
    }
}