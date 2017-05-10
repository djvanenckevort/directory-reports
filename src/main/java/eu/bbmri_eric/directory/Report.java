/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bbmri_eric.directory;

import eu.bbmri_eric.directory.contacts.BiobankOrganisationConsumer;
import eu.bbmri_eric.directory.contacts.BiobankCollectionConsumer;
import eu.bbmri_eric.directory.contacts.ContactConsumer;
import eu.bbmri_eric.directory.contacts.BiobankNetworkConsumer;
import eu.bbmri_eric.directory.contacts.ContactReport;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.client.HttpClient;
import org.molgenis.downloader.client.HttpClientFactory;
import org.molgenis.downloader.api.MolgenisClient;
import org.molgenis.downloader.client.MolgenisRestApiClient;

/**
 *
 * @author david
 */
class Report {

    private final Path outFile;
    private URI url;
    private String account;
    private String password;

    public static void main(final String[] args) {
        try {
            final Report app = new Report(args);
            app.run();
        } catch (final Exception ex) {
            System.console().format("An error occurred: %s\n",
                    ex.getLocalizedMessage()).flush();
            ex.printStackTrace(System.err);
        }
    }

    private Report(final String[] args) throws ParseException, URISyntaxException {
        final DefaultParser parser = new DefaultParser();
        final Options options = createCmdLineOptions();
        try {
            final CommandLine parseResult = parser.parse(options, args);
            outFile = Paths.get(parseResult.getOptionValue("o"));
            url = new URI(parseResult.getOptionValue("u"));
            account = parseResult.getOptionValue("a");
            password = parseResult.getOptionValue("p");

        } catch (final ParseException | URISyntaxException ex) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar report.jar [options]", options);
            throw ex;
        }
    }

    private Options createCmdLineOptions() {
        final Options options = new Options();
        final Option outFileOption = Option.builder("o")
                .argName("Output file").hasArg().longOpt("outputFile")
                .desc("Name of the file to write the data to.").required().build();
        final Option urlOption = Option.builder("u")
                .argName("URL").hasArg().longOpt("url")
                .desc("URL of the MOLGENIS instance").required().build();
        final Option userOption = Option.builder("a")
                .argName("account").hasArg().longOpt("account")
                .desc("Directory user account to login with to download the data.").build();
        final Option passwordOption = Option.builder("p")
                .argName("Password").hasArg().longOpt("password")
                .desc("Password for the MOLGENIS user to login").build();
        options.addOption(outFileOption)
                .addOption(urlOption)
                .addOption(userOption)
                .addOption(passwordOption);
        return options;
    }

    private void run() throws Exception {
        final HttpClient client = HttpClientFactory.create(false);

        try (final MolgenisClient molgenis = new MolgenisRestApiClient(client, url)) {

            try {
                if (account != null) {
                    if (password == null) {
                        System.console().writer().append("Password: ").flush();
                        password = String.copyValueOf(System.console().readPassword());
                    }
                    molgenis.login(account, password);
                }
                final ContactConsumer contacts = new ContactConsumer();
                molgenis.streamEntityData("eu_bbmri_eric_persons", contacts);
                final BiobankNetworkConsumer networks = new BiobankNetworkConsumer(contacts.getContacts());
                molgenis.streamEntityData("eu_bbmri_eric_networks", networks);
                final BiobankOrganisationConsumer biobanks = new BiobankOrganisationConsumer(contacts.getContacts(), networks.getNetworks());
                molgenis.streamEntityData("eu_bbmri_eric_biobanks", biobanks);
                final BiobankCollectionConsumer collections = new BiobankCollectionConsumer(biobanks.getBiobanks(), contacts.getContacts(), networks.getNetworks());
                molgenis.streamEntityData("eu_bbmri_eric_collections", collections);
                new ContactReport(collections.getCollections()).create(outFile);
            } finally {
                molgenis.logout();
            }
        }
    }
}
