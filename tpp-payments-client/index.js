'use strict';
require('dotenv').config();
var dcrLog = require('debug')('tpp:dcr')
  , paymentLog = require('debug')('tpp:payment'), setupLog = require('debug')('tpp:setup'), consentLog = require('debug')('tpp:consent'), commsLog = require('debug')('tpp:communications');
const config = require('./config');


(async () => {
  const {
    Issuer,
    custom,
    generators /*, TokenSet */,
  } = require('openid-client');
  const fs = require('fs');
  const crypto = require('crypto');
  const express = require('express');
  const cookieParser = require('cookie-parser');
  const app = express();
  const path = require('path');
  const https = require('https');
  const { default: axios } = require('axios');
  const certsPath = path.join(__dirname, './certs/');
  const jose = require('jose');
  const { nanoid } = require('nanoid');
  
  //A lot of oauth 2 bodies are form url encoded
  app.use(express.urlencoded({ extended: true }));

  //We need to confirm our private key into a jwks for local signing
  const key = crypto.createPrivateKey(
    fs.readFileSync(certsPath + 'signing.key')
  );
  const privateJwk = await jose.exportJWK(key);
  privateJwk.kid = config.data.signing_kid;
  setupLog('Create private jwk key %O', privateJwk);
  const keyset = {
    keys: [privateJwk],
  };

  //We need to setup an mtls certificate httpsAgent
  //NOTE: Do NOT leave rejectUnauthorized as 'false' as this disables certificate chain verification
  const httpsAgent = new https.Agent({
    ca: fs.readFileSync(certsPath + 'ca.pem'),
    key: fs.readFileSync(certsPath + 'transport.key'),
    cert: fs.readFileSync(certsPath + 'transport.pem'),
    rejectUnauthorized: false,
  });

  // set HBS to be in charge of rendering the HTML
  app.set('views', __dirname + '/views');
  app.set('view engine', 'hbs');

  //Set some logging options so that we log evrey request and response in an easy to use pattern
  custom.setHttpOptionsDefaults({
    hooks: {
      beforeRequest: [
        (options) => {
          commsLog(
            '--> %s %s',
            options.method.toUpperCase(),
            options.url.href
          );
          commsLog('--> HEADERS %o', options.headers);
          if (options.body) {
            commsLog('--> BODY %s', options.body);
          }
          if (options.form) {
            commsLog('--> FORM %s', options.form);
          }
        },
      ],
      afterResponse: [
        (response) => {
          commsLog(
            '<-- %i FROM %s %s',
            response.statusCode,
            response.request.options.method.toUpperCase(),
            response.request.options.url.href
          );
          commsLog('<-- HEADERS %o', response.headers);
          if (response.body) {
            commsLog('<-- BODY %s', response.body);
          }
          return response;
        },
      ],
    },
    timeout: 20000,
    https: {
      certificateAuthority: fs.readFileSync(certsPath + 'ca.pem'),
      certificate: fs.readFileSync(certsPath + 'transport.pem'),
      key: fs.readFileSync(certsPath + 'transport.key'),
      rejectUnauthorized: false,
    },
  });

  //Retrieve the information from the open banking brazil directory of participants on launch
  const instance = axios.create({ httpsAgent });
  setupLog('Retrieving Banks from Directory of Participants');
  const axiosResponse = await instance.get(
    'https://data.sandbox.directory.openbankingbrasil.org.br/participants'
  );


  const TotalBanks = axiosResponse.data;
  const availableBanks = TotalBanks.filter(e => 
    e.AuthorisationServers.some(as =>
      as.ApiResources.some(apifamily => 
        apifamily.ApiFamilyType == "payments-consents")
    ))
  setupLog(availableBanks);


  //These are set as global variables, they should be stored in a memory cache and retrieved based on the users session / state
  let selectedAuthServer;
  let selectedOrganisation;
  let createdConsent;

  //This configures a FAPI Client for the Bank that you have selected from the UI
  async function setupClient(bank) {
    setupLog('Begin Client Setup for Target Bank');
    selectedOrganisation = availableBanks.find((server) => {
      if (
        server.AuthorisationServers &&
        server.AuthorisationServers.some((as) => {
          if (as.CustomerFriendlyName == bank) {
            selectedAuthServer = as;
            setupLog('Target bank found in authorisation servers list');
            setupLog(selectedAuthServer);
            return true;
          }
        })
      ) {
        return server;
      }
    });

    //Check if the client is registered, if it is not, register it.
    dcrLog('Check if client already registered for this target bank');
    dcrLog('Client not registered');
    dcrLog('Beginning DCR Process');
    dcrLog('Discover how to talk to the Directory of Participants');
    //Create a new FAPI Client to talk to the directory.
    const directoryIssuer = await Issuer.discover(
      'https://auth.sandbox.directory.openbankingbrasil.org.br/'
    );

    dcrLog(
      'Discovered directory issuer %s %O',
      directoryIssuer.issuer,
      directoryIssuer.metadata
    );
    const DirectoryFAPIClient = directoryIssuer.FAPI1Client;
    const directoryFapiClient = new DirectoryFAPIClient(config.data.client);
    dcrLog('Create FAPI Client to talk to the directory %O', directoryFapiClient);

    //Set the mutual tls client and certificate to talk to the client.
    directoryFapiClient[custom.http_options] = function (url, options) {
      const result = {};

      result.cert = fs.readFileSync(certsPath + 'transport.pem'); // <string> | <string[]> | <Buffer> | <Buffer[]>
      result.key = fs.readFileSync(certsPath + 'transport.key');
      result.ca = fs.readFileSync(
        certsPath + 'ca.pem'
      ); // <string> | <string[]> | <Buffer> | <Buffer[]> | <Object[]>
      return result;
    };

    //Grab an access token with directory:software scope
    dcrLog('Obtaining Directory Access Token');
    const directoryTokenSet = await directoryFapiClient.grant({
      grant_type: 'client_credentials',
      scope: 'directory:software',
    });

    //Obtain the client ssa
    dcrLog('Obtaining SSA');
    const ssa = await directoryFapiClient.requestResource(
      `https://matls-api.sandbox.directory.openbankingbrasil.org.br/organisations/${config.data.organisation_id}/softwarestatements/${config.data.software_statement_id}/assertion`,
      directoryTokenSet
    );

    //Retrieve the keyset of the directory to validate the SSA, technically this is not required as the Bank is the party that have to validate it but it's good to show
    dcrLog('Obtaining Directory JWKS to validate the SSA');
    const JWKS = await jose.createRemoteJWKSet(
      new URL(
        'https://keystore.sandbox.directory.openbankingbrasil.org.br/openbanking.jwks'
      )
    );

    //Validate the jwt
    dcrLog('Validating SSA and extracting contents');
    const { payload } = await jose.jwtVerify(
      ssa.body.toString('utf-8'),
      JWKS,
      {
        //The expected issuer for production is available on the security specifications
        issuer: 'Open Banking Open Banking Brasil sandbox SSA issuer',
        clockTolerance: 2,
      }
    );
    dcrLog(payload);

    //Find the openid server configuration for the target bank
    dcrLog('Discovering how to talk to target bank');
    const localIssuer = await Issuer.discover(
      selectedAuthServer.OpenIDDiscoveryDocument
    );

    dcrLog(
      'Discovered Target Bank Issuer Configuration %s %O',
      localIssuer.issuer,
      localIssuer.metadata
    );

    dcrLog(`Select how to to authenticate to the bank from Banks advertised mechanisms, ${process.env.PREFERRED_TOKEN_AUTH_MECH} is preferred`);
    const { FAPI1Client } = localIssuer;
    //base on the options that the bank supports we're going to turn some defaults on
    localIssuer.metadata.token_endpoint_auth_methods_supported.includes(
      process.env.PREFERRED_TOKEN_AUTH_MECH
    )
      ? (config.data.client.token_endpoint_auth_method = process.env.PREFERRED_TOKEN_AUTH_MECH)
      : (process.env.PREFERRED_TOKEN_AUTH_MECH == 'private_key_jwt' ? config.data.client.token_endpoint_auth_method = 'tls_client_auth' : config.data.client.token_endpoint_auth_method = 'private_key_jwt'  );
    dcrLog('Mechanism selected based on what bank supports %O', config.data.client.token_endpoint_auth_method);
    //This line will require the bank to enforce par without it the client should be free to choose PAR or standard
    localIssuer.metadata.request_uri_parameter_supported ? config.data.client.require_pushed_authorization_requests = true : config.data.client.require_pushed_authorization_requests = false;
    dcrLog('Use pushed authorisation requests if the bank supports it. Will use PAR? %O', config.data.client.require_pushed_authorization_requests);

    //Set the redirects as they're required as a subset of whats registered
    config.data.client.redirect_uris = payload.software_redirect_uris;
    dcrLog('Set redirect_uris from software statement %O', payload.software_redirect_uris);

    //Add the software statement to your request for registration
    config.data.client.software_statement = ssa.body.toString('utf-8');
    dcrLog('Set softwarestatement from directory into registration metadata %O', config.data.client.software_statement);

    //Set jwks uri from the directory as this is required outside of the SSA if the client is going to be privatekeyjwt
    config.data.client.jwks_uri = payload.software_jwks_uri;
    dcrLog('Set jwks_uri from directory into registration metadata %O', payload.software_jwks_uri);


    let fapiClient;
    try {
      dcrLog('Register Client at Bank');
      //try and register a new client and set the private keys
      //For the new instance
      FAPI1Client[custom.http_options] = function (url,options) {
        // see https://github.com/sindresorhus/got/tree/v11.8.0#advanced-https-api
        const result = {};

        result.cert = fs.readFileSync(certsPath + 'transport.pem'); // <string> | <string[]> | <Buffer> | <Buffer[]>
        result.key = fs.readFileSync(certsPath + 'transport.key');
        result.ca = fs.readFileSync(
          certsPath + 'ca.pem'
        ); // <string> | <string[]> | <Buffer> | <Buffer[]> | <Object[]>
        return result;
      };

      fapiClient = await FAPI1Client.register(config.data.client, {
        jwks: keyset,
      });
      dcrLog('New client created successfully');
      dcrLog(fapiClient);
      dcrLog('TODO: Save client For Later Use');
    } catch (err) {
      console.log(err);
      dcrLog('Error registering client at bank');
      dcrLog(err);
      throw(err);
    }
    //For the new instance set the HTTPS options as well
    fapiClient[custom.http_options] = function (url,options) {
      // see https://github.com/sindresorhus/got/tree/v11.8.0#advanced-https-api
      const result = {};

      result.cert = fs.readFileSync(certsPath + 'transport.pem'); // <string> | <string[]> | <Buffer> | <Buffer[]>
      result.key = fs.readFileSync(certsPath + 'transport.key');
      result.ca = fs.readFileSync(
        certsPath + 'ca.pem'
      ); // <string> | <string[]> | <Buffer> | <Buffer[]> | <Object[]>
      return result;
    };

    setupLog('Client Setup for Target Bank Complete');
    return { fapiClient, localIssuer };
  }

  function getEndpoint(authServer, apiFamilyType, apiEndpointRegex) {
    let consentEndpoint;
    authServer.ApiResources.find((resource) => {
      if (resource.ApiFamilyType === apiFamilyType) {
        //'payments-consents'
        resource.ApiDiscoveryEndpoints.find((endpoint) => {
          if (endpoint.ApiEndpoint.match(apiEndpointRegex)) {
            //'open-banking\/payments\/v1\/consents$'
            consentEndpoint = endpoint.ApiEndpoint;
            return endpoint;
          }
        });
        return resource;
      }
    });
    return consentEndpoint;
  }

  function sleep(ms) {
    paymentLog('Sleeping');
    return new Promise((resolve) => {
      setTimeout(resolve, ms);
    });
  }

  //This pushes an authorization request to the banks pushed authorisation request and returns the authorisation redirect uri
  async function generateRequest(
    fapiClient,
    authServer,
    organisation,
    payment
  ) {
    consentLog('Beginning the generation of a consent record and authorisation process');
    //Find the consent endpoint for this authorisation server
    consentLog('Find the consent endpoint for the payments consent from the selected authorisation server from the directory');
    const consentEndpoint = getEndpoint(
      authServer,
      'payments-consents',
      'open-banking/payments/v1/consents$'
    );
    consentLog('Consent endpoint found %O', consentEndpoint);

    //Turn the payment consent data into a jwt
    consentLog('Creating consent JWT from the previously stored payment consent details');
    consentLog('Log presigning payment consent object');
    consentLog(payment);
    const jwt = await new jose.SignJWT({ data: payment })
      .setProtectedHeader({ alg: 'PS256', typ: 'JWT', kid: privateJwk.kid })
      .setIssuedAt()
      .setIssuer(config.data.organisation_id)
      .setJti(nanoid())
      .setAudience(consentEndpoint)
      .setExpirationTime('5m')
      .sign(key);
    
    consentLog('Log post signing payment consent JWT');
    consentLog(jwt);

    consentLog('Obtaining an access token to create the consent record');
    const ccToken = await fapiClient.grant({
      grant_type: 'client_credentials',
      scope: 'payments',
    });

    //Create the consent
    consentLog('Creating the consent record');
    createdConsent = await fapiClient.requestResource(
      consentEndpoint,
      ccToken,
      {
        method: 'POST',
        body: jwt,
        headers: {
          'content-type': 'application/jwt',
          'x-idempotency-key': nanoid(),
        },
      }
    );
    //Errors processing a JWT are sent as a

    consentLog('Validate the Consent Response JWT to confirm it was signed correctly by the bank');
    consentLog('Retrieve the keyset for the bank sending the consent response from the diretory of participants');
    //Retrieve the keyset of the sending bank
    const JWKS = await jose.createRemoteJWKSet(
      new URL(
        `https://keystore.sandbox.directory.openbankingbrasil.org.br/${organisation.OrganisationId}/application.jwks`
      )
    );

    console.log(createdConsent.body.toString());
      
    //Validate the jwt
    const { payload } = await jose.jwtVerify(
      createdConsent.body.toString(),
      JWKS,
      {
        issuer: organisation.organisation_id,
        audience: config.data.organisation_id,
        clockTolerance: 2,
      }
    );
    if (createdConsent.statusCode != 201) {
      consentLog('Consent NOT created successfully');
      return {error: payload};
    }
    //Update the payment consent
    createdConsent = payload;
    consentLog('Consent response payload validated and extracted successfully');
    consentLog(createdConsent);

    consentLog('Setting parameters for the authorisation flow including nonce and pkce');
    const state = crypto.randomBytes(32).toString('hex');
    const nonce = crypto.randomBytes(32).toString('hex');
    const code_verifier = generators.codeVerifier();

    // store the code_verifier in your framework's session mechanism, if it is a cookie based solution
    // it should be httpOnly (not readable by javascript) and encrypted.
    const code_challenge = generators.codeChallenge(code_verifier);

    consentLog('Request that the bank provide the users authentication time in the id_token, if the bank does not support this then the it should just ignore this attribute and carry on processing the request');
    const claims = {
      id_token: {
        auth_time: {
          essential: true,
        },
      },
      user_info: {
        auth_time: {
          essential: true,
        },
      },
    };

    consentLog('Add the created consent records id to the dynamic consent scope');
    const scope = `openid consent:${payload.data.consentId} payments`;
    consentLog('Create the FAPI request object');
    const requestObject = await fapiClient.requestObject({
      scope,
      response_type: 'code id_token',
      redirect_uri: 'https://tpp.localhost/cb',
      code_challenge,
      code_challenge_method: 'S256',
      response_mode: 'form_post',
      state,
      nonce,
      claims,
      max_age: 900,
    });

    consentLog(requestObject);

    let reference;
    let authUrl;

    //If there is a pushed authorisation request end point then use it as it is a more secure way to talk to the bank
    consentLog('Decide how to communicate the request to the bank, by reference (PAR) or by Value');
    if (fapiClient.issuer.pushed_authorization_request_endpoint) {
      consentLog('The bank supports PAR so we will use this mechanism as it is more secure');
      try {
        consentLog('Create a PAR resource');
        reference = await fapiClient.pushedAuthorizationRequest({
          request: requestObject,
        });
      } catch (e) {
        console.log(e);
      }

      consentLog('Create a authorisation request url using PAR');
      authUrl = await fapiClient.authorizationUrl({
        request_uri: reference.request_uri,
        prompt: 'consent',
      });
      consentLog(authUrl);
      return { authUrl, code_verifier, state, nonce, createdConsent };
    } else {
      consentLog('Create a authorisation request url passing the request object by value');
      authUrl = await fapiClient.authorizationUrl({
        request: requestObject,
        prompt: 'consent',
      });
      consentLog(authUrl);
      return { authUrl, code_verifier, state, nonce, createdConsent };
    }
  }

  //Express setup routes
  app.use(cookieParser());
  app.use(express.static(path.join(__dirname, 'public')));

  app.use('/banks', async (req, res) => {
    consentLog('Providing a list of banks to the customer for them to choose from the UI');
    res.json(availableBanks);
  });

  app.get('/', async (req, res) => {
    //Clear stale cookies on page load
    res.clearCookie('state');
    res.clearCookie('nonce');
    res.clearCookie('code_verifier');

    res.sendFile(path.join(__dirname, './views', 'payment.html'));
  });

  app.get('/payment', async (req, res) => {
    //Clear stale cookies on page load
    setupLog('Starting a new journey, clearing old cookies');
    res.clearCookie('payment');
    res.clearCookie('state');
    res.clearCookie('nonce');
    res.clearCookie('code_verifier');

    res.sendFile(path.join(__dirname, './views', 'payment.html'));
  });

  app.post('/payment', async (req, res) => {
    consentLog('Starting a new payment consent');
    let date = new Date();
    const offset = date.getTimezoneOffset();
    date = new Date(date.getTime() - offset * 60 * 1000);

    const data = {
      debtorAccount: {
        number: req.body.debtorAccount_number,
        accountType: req.body.debtorAccount_accountType,
        ispb: req.body.debtorAccount_ispb,
        issuer: req.body.debtorAccount_issuer,
      },
      loggedUser: {
        document: {
          identification: req.body.loggedUser_document_identification,
          rel: req.body.loggedUser_document_rel,
        },
      },
      creditor: {
        name: req.body.creditor_name,
        cpfCnpj: req.body.creditor_cpfCnpj,
        personType: req.body.creditor_personType,
      },
      payment: {
        date: date.toISOString().split('T')[0],
        amount: req.body.payment_amount,
        currency: 'BRL',
        details: {
          proxy: '12345678901',
          localInstrument: req.body.payment_details_localInstrument,
          creditorAccount: {
            number: req.body.payment_details_creditAccount_number,
            accountType: req.body.payment_details_creditAccount_accountType,
            ispb: req.body.payment_details_creditAccount_ispb,
            issuer: req.body.payment_details_creditAccount_issuer,
          },
        },
        type: 'PIX',
      },
    };

    consentLog('Storing consent payload in a cookie for convenience, server side mechanisms would be more secure and consent payload only just fits in a cookie size');
    res.cookie('consent', JSON.stringify(data), {
      sameSite: 'none',
      secure: true,
    });

    //Clear stale cookies on page load
    res.clearCookie('state');
    res.clearCookie('nonce');
    res.clearCookie('code_verifier');

    consentLog('Sending customer to select the bank they want to make the payment from');
    res.render('idp', {});
  });

  app.get('/idp', async (req, res) => {
    //Clear stale cookies on page load
    res.clearCookie('state');
    res.clearCookie('nonce');
    res.clearCookie('code_verifier');

    res.render('idp', {});
  });

  let client;
  let issuer;

  app.use(express.urlencoded());
  
  //This is used for response mode form_post, query and form_post are the most common
  app.post('/cb', async (req, res) => {
    consentLog('Received redirect from the bank');
    const callbackParams = client.callbackParams(req);
    consentLog('Trying to obtain an access token using the authorization code');
    const tokenSet = await client.callback(
      'https://tpp.localhost/cb',
      callbackParams,
      {
        code_verifier: req.cookies.code_verifier,
        state: req.cookies.state,
        nonce: req.cookies.nonce,
        response_type: 'code',
      },
      {
        clientAssertionPayload: {
          aud: issuer.mtls_endpoint_aliases.token_endpoint,
        },
      }
    );
    consentLog('Payment Access token obtained. %O', tokenSet);

    //Cut down version of the consent process
    consentLog('Find the consent endpoint to check the status of the consent resource');
    const consentEndpoint = getEndpoint(
      selectedAuthServer,
      'payments-consents',
      'open-banking/payments/v1/consents$'
    );
    consentLog('Consent endpoint found %O', consentEndpoint);
    consentLog('Obtaining an access token to create the consent record');
    const ccToken = await client.grant({
      grant_type: 'client_credentials',
      scope: 'payments',
    });

    const JWKS = await jose.createRemoteJWKSet(
      new URL(
        `https://keystore.sandbox.directory.openbankingbrasil.org.br/${selectedOrganisation.OrganisationId}/application.jwks`
      )
    );

    let y = 0;
    while (!['AUTHORISED'].includes(createdConsent.data.status)) {
      //Create the consent
      consentLog('Get the consent record');
      createdConsent = await client.requestResource(
        `${consentEndpoint}/${createdConsent.data.consentId}`,
        ccToken,
        {
          headers: {
            'accept': 'application/jwt',
            'x-idempotency-key': nanoid(),
          },
        }
      );
      //Errors processing a JWT are sent as a
      consentLog('Validate the Consent Response JWT to confirm it was signed correctly by the bank');

      //Validate the jwt
      let { payload } = await jose.jwtVerify(
        createdConsent.body.toString(),
        JWKS,
        {
          issuer: selectedOrganisation.OrganisationId,
          audience: config.data.organisation_id,
          clockTolerance: 2,
        }
      );
      //Update the payment consent
      createdConsent = payload;
      consentLog('Consent response payload validated and extracted successfully');
      consentLog(createdConsent);

      await sleep(process.env.LOOP_PAUSE_TIME);

      y = y + 1;
      if (y > process.env.NUMBER_OF_CHECK_LOOPS) {
        consentLog(
          'Consent has not reached authorised state after 5 iterations, failing'
        );
        payload = { msg: 'Unable To Complete Authorisation - State Not Authorised', payload: payload };
        payload.stringify = JSON.stringify(payload, null, 2);
        return res.render('cb', { claims: tokenSet.claims(), payload });
      }

    }

    consentLog('Consent process finished');

    paymentLog('Find payment endpoint for the selected bank from the directory of participants');
    const paymentEndpoint = getEndpoint(
      selectedAuthServer,
      'payments-pix',
      'open-banking/payments/v1/pix/payments$'
    );
    paymentLog('Payment endpoint found %O', paymentEndpoint);
    let date = new Date();
    const offset = date.getTimezoneOffset();
    date = new Date(date.getTime() - offset * 60 * 1000);

    const payment = {
      creditorAccount: createdConsent.data.payment.details.creditorAccount,
      localInstrument: createdConsent.data.payment.details.localInstrument,
      proxy: createdConsent.data.payment.details.proxy,
      remittanceInformation: 'Making a payment',
      cnpjInitiator: '59285411000113',
      payment: {
        amount: createdConsent.data.payment.amount,
        currency: createdConsent.data.payment.currency,
      },
    };
    paymentLog('Create payment object %O', payment);
    paymentLog('Signing payment');
    const jwt = await new jose.SignJWT({ data: payment })
      .setProtectedHeader({ alg: 'PS256', typ: 'JWT', kid: privateJwk.kid })
      .setIssuedAt()
      .setIssuer(config.data.organisation_id)
      .setJti(nanoid())
      .setAudience(paymentEndpoint)
      .setExpirationTime('5m')
      .sign(key);
    paymentLog('Signed payment JWT %O', jwt);
    paymentLog('Create payment resource using the signed payment JWT ');
    let paymentResponse = await client.requestResource(
      `${paymentEndpoint}`,
      tokenSet,
      {
        body: jwt,
        method: 'POST',
        headers: {
          'content-type': 'application/jwt',
          'x-idempotency-key': nanoid(),
        },
      }
    );
    paymentLog('Payment resource created successfully %O', paymentResponse.body.toString());
    paymentLog('Validate payment response as it is a JWT');
    paymentLog('Retrieve the keyset for the bank (this has already been done and could be cached)');
    //Validate the jwt came from teh correct bank and was meant to be sent to me.
    let { payload } = await jose.jwtVerify(
      paymentResponse.body.toString(),
      JWKS,
      {
        issuer: selectedOrganisation.OrganisationId,
        audience: config.data.organisation_id,
        clockTolerance: 2,
      }
    );
    paymentLog('Payment response extracted and validated');

    if (payload.errors) {
        payload = { msg: 'Payment errored', payload: payload };
        payload.stringify = JSON.stringify(payload, null, 2);
        return res.render('cb', { claims: tokenSet.claims(), payload });
    }

    let x = 0;
    while (!['ACSP', 'ACCC', 'RJCT'].includes(payload.data.status)) {
      paymentLog(
        'Payment still not in a valid end state. Status: %O. Will check again to see if it has gone through.', payload.data.status
      );
      paymentLog(payload);
      paymentLog(
        'Use the self link on the payment to retrieve the latest record status. %O', payload.links.self
      );
      paymentResponse = await client.requestResource(
        payload.links.self,
        tokenSet,
        {
          headers: { accept: 'application/jwt', 'x-idempotency-key': nanoid() },
        }
      );
      
      paymentLog(
        'Validate and extract the payment response from the bank'
      );
      ({ payload } = await jose.jwtVerify(
        paymentResponse.body.toString(),
        JWKS,
        {
          issuer: selectedOrganisation.OrganisationId,
          audience: config.data.organisation_id,
          clockTolerance: 2,
        }
      ));
      x = x + 1;
      await sleep(process.env.LOOP_PAUSE_TIME);
      if (x > process.env.NUMBER_OF_CHECK_LOOPS) {
        paymentLog(
          'Payment has not reached final state after 5 iterations, failing'
        );
        payload = { msg: 'Unable To Complete Payment', payload: payload };
        payload.stringify = JSON.stringify(payload, null, 2);

        const consentPayload = { msg: 'Unable To Complete Payment', payload: createdConsent };
        consentPayload.stringify =  JSON.stringify(createdConsent, null, 2);
          
        return res.render('cb', { claims: tokenSet.claims(), payload, consentPayload });
      }
    }

    paymentLog('Payment has reached a final state of',payload.data.status);
    paymentLog(payload);
    payload.stringify = JSON.stringify(payload, null, 2);

    const consentPayload = createdConsent;
    consentPayload.stringify = JSON.stringify(createdConsent, null, 2);
    
    paymentLog('Payment execution complete');
    return res.render('cb', { claims: tokenSet.claims(), payload, consentPayload });
  });

  app.post('/makepayment', async (req, res) => {
    if (req.body.bank) {
      //Setup the client
      consentLog('Customer has select bank issuer to use %O', req.body.bank);
      const { fapiClient, localIssuer } = await setupClient(req.body.bank);
      consentLog('Client created, ready to talk to the chosen bank');
      client = fapiClient;
      issuer = localIssuer;
    }
    else {
      throw Error('No bank was selected');
    }

    const path = '';

    //Setup the request
    const { authUrl, code_verifier, state, nonce, error } = await generateRequest(
      client,
      selectedAuthServer,
      selectedOrganisation,
      JSON.parse(req.cookies.consent)
    );

    if (error) {
      const payload = { msg: 'Unable To Complete Payment', payload: error };
      payload.stringify = JSON.stringify(payload, null, 2);
      return res.render('cb', { claims: undefined, payload });
    }

    res.cookie('state', state, { path, sameSite: 'none', secure: true });
    res.cookie('nonce', nonce, { path, sameSite: 'none', secure: true });
    res.cookie('code_verifier', code_verifier, {
      path,
      sameSite: 'none',
      secure: true,
    });

    consentLog('Send customer to bank to give consent to the payment');
    return res.redirect(authUrl);
  });

  app.get('/complete', async (req, res) => {
    res.render('complete', {});
  });

  https
    .createServer(
      {
        // ...
        key: fs.readFileSync(certsPath + 'transport.key'),
        cert: fs.readFileSync(certsPath + 'transport.pem'),
        // ...
      },
      app
    )
    .listen(443);

  console.log('Node.js web server at port 443 is running..');
})();
