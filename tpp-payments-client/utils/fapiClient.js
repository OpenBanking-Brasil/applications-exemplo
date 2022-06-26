var dcrLog = require("debug")("tpp:dcr"),
  setupLog = require("debug")("tpp:setup");
const { Issuer, custom } = require("openid-client");
const path = require("path");
const certsPath = path.join(__dirname, "../certs/");
const fs = require("fs");
const jose = require("jose");

const config = require("../config");
const { getDatabase } = require("../database/connectToDb");

const USE_EXISTING_CLIENT = "USE_EXISTING_CLIENT";

async function getFapiClient(
  req,
  clientId = "",
  registrationAccessToken = "",
  drcOption = ""
) {
  try {
    const { fapiClient, localIssuer } = await setupClient(
      req.session.selectedBank,
      drcOption,
      req,
      clientId,
      registrationAccessToken
    );
    return { fapiClient, localIssuer };
  } catch (error) {
    return { error: error };
  }
}

//This configures a FAPI Client for the Bank that you have selected from the UI
async function setupClient(
  bank,
  selectedDcrOption,
  req,
  clientId = "",
  registrationAccessToken = ""
) {
  const useCustomConfig = req.session.useCustomConfig;
  //Use the default config if no custom configuration is provided
  if (!useCustomConfig) {
    req.session.config = JSON.parse(JSON.stringify(config));
  } else {
    req.session.config = JSON.parse(JSON.stringify(req.session.customConfig));
  }
  setupLog("Begin Client Setup for Target Bank");
  req.session.selectedOrganisation = req.session.availableBanks.find(
    (server) => {
      if (
        server.AuthorisationServers &&
        server.AuthorisationServers.some((as) => {
          if (as.CustomerFriendlyName == bank) {
            req.session.selectedAuthServer = as;
            setupLog("Target bank found in authorisation servers list");
            setupLog(req.session.selectedAuthServer);
            return true;
          }
        })
      ) {
        return server;
      }
    }
  );

  //Find the openid server configuration for the target bank
  dcrLog("Discovering how to talk to target bank");
  const localIssuer = await Issuer.discover(
    req.session.selectedAuthServer.OpenIDDiscoveryDocument
  );

  dcrLog(
    "Discovered Target Bank Issuer Configuration %s %O",
    localIssuer.issuer,
    localIssuer.metadata
  );

  const { FAPI1Client } = localIssuer;

  const { certAuthority, transportKey, transportPem } = getCerts(
    req.session.certificates
  );

  let fapiClient;
  if (selectedDcrOption === USE_EXISTING_CLIENT && clientId) {
    try {
      dcrLog("Get the existing client from the Bank");
      //try and get the existing client and set the private keys
      //For the new instance
      FAPI1Client[custom.http_options] = function (url, options) {
        // see https://github.com/sindresorhus/got/tree/v11.8.0#advanced-https-api
        const result = {};

        result.cert = transportPem; // <string> | <string[]> | <Buffer> | <Buffer[]>
        result.key = transportKey;
        result.ca = certAuthority; // <string> | <string[]> | <Buffer> | <Buffer[]> | <Object[]>
        return result;
      };

      try {
        fapiClient = await FAPI1Client.fromUri(
          `https://matls-auth.mockbank.poc.raidiam.io/reg/${clientId}`,
          registrationAccessToken,
          req.session.keyset
        );
      } catch (error) {
        throw error;
      }

      dcrLog("The existing client obtained successfully");
      dcrLog(fapiClient);
    } catch (err) {
      console.log(err);
      dcrLog("Error obtaining the existing client's deatils from the bank");
      dcrLog(err);
      throw err;
    }

    //For the new instance set the HTTPS options as well
    fapiClient[custom.http_options] = function (url, options) {
      // see https://github.com/sindresorhus/got/tree/v11.8.0#advanced-https-api
      const result = {};

      result.cert = transportPem; // <string> | <string[]> | <Buffer> | <Buffer[]>
      result.key = transportKey;
      result.ca = certAuthority; // <string> | <string[]> | <Buffer> | <Buffer[]> | <Object[]>
      return result;
    };

    setupLog("Client Setup for Target Bank Complete");
    return { fapiClient, localIssuer };
  }

  //Check if the client is registered, if it is not, register it.
  dcrLog("Check if client already registered for this target bank");
  dcrLog("Client not registered");
  dcrLog("Beginning DCR Process");
  dcrLog("Discover how to talk to the Directory of Participants");
  //Create a new FAPI Client to talk to the directory.
  const directoryIssuer = await Issuer.discover(
    "https://auth.sandbox.directory.openbankingbrasil.org.br/"
  );

  dcrLog(
    "Discovered directory issuer %s %O",
    directoryIssuer.issuer,
    directoryIssuer.metadata
  );
  const DirectoryFAPIClient = directoryIssuer.FAPI1Client;
  const directoryFapiClient = new DirectoryFAPIClient(
    req.session.config.data.client
  );
  dcrLog("Create FAPI Client to talk to the directory %O", directoryFapiClient);

  //Set the mutual tls client and certificate to talk to the client.
  directoryFapiClient[custom.http_options] = function (url, options) {
    const result = {};

    result.cert = transportPem; // <string> | <string[]> | <Buffer> | <Buffer[]>
    result.key = transportKey;
    result.ca = certAuthority; // <string> | <string[]> | <Buffer> | <Buffer[]> | <Object[]>
    return result;
  };

  //Grab an access token with directory:software scope
  dcrLog("Obtaining Directory Access Token");
  const directoryTokenSet = await directoryFapiClient.grant({
    grant_type: "client_credentials",
    scope: "directory:software",
  });

  //Obtain the client ssa
  dcrLog("Obtaining SSA");
  const ssa = await directoryFapiClient.requestResource(
    `https://matls-api.sandbox.directory.openbankingbrasil.org.br/organisations/${req.session.config.data.organisation_id}/softwarestatements/${req.session.config.data.software_statement_id}/assertion`,
    directoryTokenSet
  );

  //Retrieve the keyset of the directory to validate the SSA, technically this is not required as the Bank is the party that have to validate it but it's good to show
  dcrLog("Obtaining Directory JWKS to validate the SSA");
  const JWKS = await jose.createRemoteJWKSet(
    new URL(
      "https://keystore.sandbox.directory.openbankingbrasil.org.br/openbanking.jwks"
    )
  );

  //Validate the jwt
  dcrLog("Validating SSA and extracting contents");
  const { payload } = await jose.jwtVerify(ssa.body.toString("utf-8"), JWKS, {
    //The expected issuer for production is available on the security specifications
    issuer: "Open Banking Open Banking Brasil sandbox SSA issuer",
    clockTolerance: 2,
  });
  dcrLog(payload);

  dcrLog(
    `Select how to to authenticate to the bank from Banks advertised mechanisms, ${process.env.PREFERRED_TOKEN_AUTH_MECH} is preferred`
  );
  let preferredTokenAuthMech = req.session.useCustomConfig
    ? req.session.customEnvVars.preferred_token_auth_mech
    : process.env.PREFERRED_TOKEN_AUTH_MECH;
  //base on the options that the bank supports we're going to turn some defaults on
  localIssuer.metadata.token_endpoint_auth_methods_supported.includes(
    preferredTokenAuthMech
  )
    ? (req.session.config.data.client.token_endpoint_auth_method =
        preferredTokenAuthMech)
    : preferredTokenAuthMech == "private_key_jwt"
    ? (req.session.config.data.client.token_endpoint_auth_method =
        "tls_client_auth")
    : (req.session.config.data.client.token_endpoint_auth_method =
        "private_key_jwt");
  dcrLog(
    "Mechanism selected based on what bank supports %O",
    req.session.config.data.client.token_endpoint_auth_method
  );
  //This line will require the bank to enforce par without it the client should be free to choose PAR or standard
  localIssuer.metadata.request_uri_parameter_supported
    ? (req.session.config.data.client.require_pushed_authorization_requests = true)
    : (req.session.config.data.client.require_pushed_authorization_requests = false);
  dcrLog(
    "Use pushed authorisation requests if the bank supports it. Will use PAR? %O",
    req.session.config.data.client.require_pushed_authorization_requests
  );

  //Set the redirects as they're required as a subset of whats registered
  req.session.config.data.client.redirect_uris = payload.software_redirect_uris;
  dcrLog(
    "Set redirect_uris from software statement %O",
    payload.software_redirect_uris
  );

  //Add the software statement to your request for registration
  req.session.config.data.client.software_statement =
    ssa.body.toString("utf-8");
  dcrLog(
    "Set softwarestatement from directory into registration metadata %O",
    req.session.config.data.client.software_statement
  );

  //Set jwks uri from the directory as this is required outside of the SSA if the client is going to be privatekeyjwt
  req.session.config.data.client.jwks_uri = payload.software_jwks_uri;
  dcrLog(
    "Set jwks_uri from directory into registration metadata %O",
    payload.software_jwks_uri
  );

  const db = getDatabase();
  try {
    dcrLog("Register Client at Bank");
    //try and register a new client and set the private keys
    //For the new instance
    FAPI1Client[custom.http_options] = function (url, options) {
      // see https://github.com/sindresorhus/got/tree/v11.8.0#advanced-https-api
      const result = {};

      result.cert = transportPem; // <string> | <string[]> | <Buffer> | <Buffer[]>
      result.key = transportKey;
      result.ca = certAuthority; // <string> | <string[]> | <Buffer> | <Buffer[]> | <Object[]>
      return result;
    };

    fapiClient = await FAPI1Client.register(req.session.config.data.client, {
      jwks: req.session.keyset,
    });
    dcrLog("New client created successfully");
    dcrLog(fapiClient);
    const result = await db.collection("clients").insertOne({
      bank: bank,
      clientId: fapiClient.client_id,
      registrationAccessToken: fapiClient.registration_access_token,
    });
    dcrLog("Client added to the database", result);
  } catch (err) {
    console.log(err);
    dcrLog("Error registering client at bank");
    dcrLog(err);
    throw err;
  }
  //For the new instance set the HTTPS options as well
  fapiClient[custom.http_options] = function (url, options) {
    // see https://github.com/sindresorhus/got/tree/v11.8.0#advanced-https-api
    const result = {};

    result.cert = transportPem; // <string> | <string[]> | <Buffer> | <Buffer[]>
    result.key = transportKey;
    result.ca = certAuthority; // <string> | <string[]> | <Buffer> | <Buffer[]> | <Object[]>
    return result;
  };

  setupLog("Client Setup for Target Bank Complete");
  return { fapiClient, localIssuer };
}

function getCerts(certificates = {}) {
  const certAuthority = certificates.hasOwnProperty("ca.pem")
    ? certificates["ca.pem"]
    : fs.readFileSync(certsPath + "ca.pem");

  const transportKey = certificates.hasOwnProperty("transport.key")
    ? certificates["transport.key"]
    : fs.readFileSync(certsPath + "transport.key");

  const transportPem = certificates.hasOwnProperty("transport.pem")
    ? certificates["transport.pem"]
    : fs.readFileSync(certsPath + "transport.pem");

  const signingKey = certificates.hasOwnProperty("signing.key")
    ? certificates["signing.key"]
    : fs.readFileSync(certsPath + "signing.key");

  return { certAuthority, transportKey, transportPem, signingKey };
}

module.exports = {
  setupClient,
  getCerts,
  getFapiClient,
};
