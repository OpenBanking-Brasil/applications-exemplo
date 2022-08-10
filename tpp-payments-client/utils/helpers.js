let paymentLog = require("debug")("tpp:payment"),
  consentLog = require("debug")("tpp:consent");

const { TokenSet, generators } = require("openid-client");
const crypto = require("crypto");
const jose = require("jose");
const { nanoid } = require("nanoid");

const { getFapiClient, getCerts } = require("./fapiClient.js");

const USE_EXISTING_CLIENT = "USE_EXISTING_CLIENT";

//Fetch data for the given API
async function fetchData(req, apiFamilyType, apiType, path = "", requestMethod = "GET") {
  const { fapiClient } = await getFapiClient(
    req,
    req.session.clientId,
    req.session.registrationAccessToken,
    USE_EXISTING_CLIENT
  );
  const client = fapiClient;

  const tokenSet = await getToken(req);
  req.session.refreshToken = tokenSet.refresh_token;
  req.session.accessToken = tokenSet.access_token;
  req.session.tokenSet = tokenSet;

  const ApiVersion = req.session.ApiVersion;

  let pathRegex;
  if (apiFamilyType === "accounts") {
    pathRegex = `open-banking/accounts/${ApiVersion}/accounts$`;
  } else if (apiFamilyType === "credit-cards-accounts") {
    pathRegex = `/open-banking/credit-cards-accounts/${ApiVersion}/accounts$`;
  } else if (apiFamilyType === "resources") {
    pathRegex = `open-banking/resources/${ApiVersion}/resources$`;
  } else if (apiFamilyType === "customers-business") {
    pathRegex = `open-banking/customers/${ApiVersion}/business/${apiType}$`;
  } else if (apiFamilyType === "customers-personal") {
    pathRegex = `open-banking/customers/${ApiVersion}/personal/${apiType}$`;
  } else if (apiFamilyType === "loans") {
    pathRegex = `open-banking/loans/${ApiVersion}/contracts$`;
  } else if (apiFamilyType === "financings") {
    pathRegex = `open-banking/financings/${ApiVersion}/contracts$`;
  } else if (apiFamilyType === "invoice-financings") {
    pathRegex = `open-banking/invoice-financings/${ApiVersion}/contracts$`;
  } else if (apiFamilyType === "unarranged-accounts-overdraft") {
    pathRegex = `open-banking/unarranged-accounts-overdraft/${ApiVersion}/contracts$`;
  } else if (apiFamilyType === "consents") {
    pathRegex = `open-banking/consents/${ApiVersion}/consents$`;
  }

  paymentLog(
    `Find the ${apiType} endpoint for the selected bank from the directory of participants`
  );
  const endpoint = `${getEndpoint(
    req.session.selectedAuthServer,
    apiFamilyType,
    pathRegex
  )}${path}`;
  consentLog(`The ${apiType} endpoint found %O`, endpoint);

  let accessToken = (apiType === "consents") ? req.session.consentRequestData.tokenSet.access_token : req.session.accessToken;

  const requestData = {
    endpoint,
    accessToken: accessToken,
  };

  if(endpoint.split("/")[0] === "undefined"){
    return {
      responseBody: "could not find the provided API endpoint",
      statusCode: 404,
      requestData,
    };
  }

  paymentLog(`Getting ${apiType} response`);
  const response = await client.requestResource(
    endpoint,
    accessToken,
    {method: requestMethod}
  );

  if (!response.body) {
    return {
      responseBody: "undefined",
      statusCode: response.statusCode,
      requestData,
    };
  }

  return {
    responseBody: JSON.parse(response.body.toString()),
    statusCode: response.statusCode,
    requestData,
  };
}

function getPathWithParams(queryParams) {
  let path = "";
  let isFirstIteration = true;
  for (let queryParam in queryParams) {
    if (queryParams[queryParam]) {
      if (!isFirstIteration) {
        path += `&${queryParam}=${queryParams[queryParam]}`;
      } else {
        isFirstIteration = false;
        path = `?${queryParam}=${queryParams[queryParam]}`;
      }
    }
  }

  return path;
}

async function getToken(req) {
  const { fapiClient } = await getFapiClient(
    req,
    req.session.clientId,
    req.session.registrationAccessToken,
    USE_EXISTING_CLIENT
  );
  const client = fapiClient;

  const theTokenSet = req.session.tokenSet;
  const tokenSet = new TokenSet(theTokenSet);
  if (tokenSet.expired()) {
    return await client.refresh(theTokenSet.refresh_token);
  }
  return theTokenSet;
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

//This pushes an authorization request to the banks pushed authorisation request and returns the authorisation redirect uri
async function generateRequest(
  fapiClient,
  authServer,
  payloadData,
  flag,
  organisation,
  req
) {
  consentLog(
    "Beginning the generation of a consent record and authorisation process"
  );
  //Find the consent endpoint for this authorisation server
  req.session.flag = flag;
  let apiFamilyType, apiEndpointRegex;
  if (flag === "PAYMENTS") {
    consentLog(
      "Find the consent endpoint for the payments consent from the selected authorisation server from the directory"
    );
    apiFamilyType = "payments-consents";
    apiEndpointRegex = "open-banking/payments/v1/consents$";
  } else {
    consentLog(
      "Find the consent endpoint from the selected authorisation server from the directory"
    );
    apiFamilyType = "consents";
    apiEndpointRegex = "open-banking/consents/v1/consents$";
  }
  const consentEndpoint = getEndpoint(
    authServer,
    apiFamilyType,
    apiEndpointRegex
  );
  consentLog("Consent endpoint found %O", consentEndpoint);

  let theScope;
  let signedPayload;
  let requestOptions;
  if (flag === "PAYMENTS") {
    //Turn the payment consent data into a jwt
    consentLog(
      "Creating consent JWT from the previously stored payment consent details"
    );
    consentLog("Log presigning payment consent object");
    consentLog(payloadData);
    const { signingKey } = getCerts(req.session.certificates);
    const key = getPrivateKey(signingKey);
    
    try {
      signedPayload = await new jose.SignJWT({ data: payloadData })
      .setProtectedHeader({
        alg: "PS256",
        typ: "JWT",
        kid: req.session.privateJwk.kid,
      })
      .setIssuedAt()
      .setIssuer(req.session.config.data.organisation_id)
      .setJti(nanoid())
      .setAudience(consentEndpoint)
      .setExpirationTime("5m")
      .sign(key);
    } catch (error){
      console.log(error.message);
      throw error;
    }
    
    console.log("test4");
    consentLog("Log post signing payment consent JWT");
    consentLog(signedPayload);

    theScope = "payments";
    requestOptions = {
      method: "POST",
      body: signedPayload,
      headers: {
        "content-type": "application/jwt",
        "x-idempotency-key": nanoid(),
      },
    };
  } else {
    theScope = "consents";
    requestOptions = {
      method: "POST",
      body: payloadData,
      headers: {
        "content-type": "application/json",
      },
    };
  }

  consentLog("Obtaining an access token to create the consent record");
  const ccToken = await fapiClient.grant({
    grant_type: "client_credentials",
    scope: theScope,
  });

  //Create the consent
  consentLog("Creating the consent record");
  const response = await fapiClient.requestResource(
    consentEndpoint,
    ccToken,
    requestOptions
  );

  req.session.consentRequestObject = {
    consentEndpoint,
    ccToken,
    requestOptions
  };

  let payload;
  if (flag === "PAYMENTS") {
    consentLog(
      "Validate the Consent Response JWT to confirm it was signed correctly by the bank"
    );
    consentLog(
      "Retrieve the keyset for the bank sending the consent response from the diretory of participants"
    );
    //Retrieve the keyset of the sending bank
    const JWKS = await jose.createRemoteJWKSet(
      new URL(
        `https://keystore.sandbox.directory.openbankingbrasil.org.br/${organisation.OrganisationId}/application.jwks`
      )
    );

    //Validate the jwt
    let result = {};
    try {
      result = await jose.jwtVerify(response.body.toString(), JWKS, {
        issuer: organisation.organisation_id,
        audience: req.session.config.data.organisation_id,
        clockTolerance: 5,
        maxTokenAge: 300
      });
    } catch(error){
      result.payload = JSON.parse(response.body.toString());
    }
    payload = result.payload;
    req.session.createdConsent = payload;
  } else {
    req.session.createdConsent = JSON.parse(response.body.toString());
  }

  if (response.statusCode != 201) {
    consentLog("Consent NOT created successfully");
    return { error: payload };
  }

  consentLog("Consent response payload validated and extracted successfully");

  consentLog(
    "Setting parameters for the authorisation flow including nonce and pkce"
  );
  const state = crypto.randomBytes(32).toString("hex");
  const nonce = crypto.randomBytes(32).toString("hex");
  const code_verifier = generators.codeVerifier();

  // store the code_verifier in your framework's session mechanism, if it is a cookie based solution
  // it should be httpOnly (not readable by javascript) and encrypted.
  const code_challenge = generators.codeChallenge(code_verifier);

  consentLog(
    "Request that the bank provide the users authentication time in the id_token, if the bank does not support this then the it should just ignore this attribute and carry on processing the request"
  );
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

  consentLog("Add the created consent records id to the dynamic consent scope");

  const scope =
    flag === "PAYMENTS"
      ? `openid consent:${payload.data.consentId} payments`
      : `openid consent:${
          JSON.parse(response.body.toString()).data.consentId
        } accounts resources credit-cards-accounts unarranged-accounts-overdraft customers loans financings invoice-financings`;
  consentLog("Create the FAPI request object");
  const requestObject = await fapiClient.requestObject({
    scope,
    response_type: "code id_token",
    redirect_uri: "https://tpp.localhost/cb",
    code_challenge,
    code_challenge_method: "S256",
    response_mode: "form_post",
    state,
    nonce,
    claims,
    max_age: 900,
  });

  consentLog(requestObject);

  let reference;
  let authUrl;

  //If there is a pushed authorisation request end point then use it as it is a more secure way to talk to the bank
  consentLog(
    "Decide how to communicate the request to the bank, by reference (PAR) or by Value"
  );
  if (fapiClient.issuer.pushed_authorization_request_endpoint) {
    consentLog(
      "The bank supports PAR so we will use this mechanism as it is more secure"
    );
    try {
      consentLog("Create a PAR resource");
      reference = await fapiClient.pushedAuthorizationRequest({
        request: requestObject,
      });
    } catch (e) {
      console.log(e);
    }

    consentLog("Create a authorisation request url using PAR");
    authUrl = await fapiClient.authorizationUrl({
      request_uri: reference.request_uri,
      prompt: "consent",
    });
    consentLog(authUrl);
    return {
      authUrl,
      code_verifier,
      state,
      nonce,
      createdConsent: req.session.createdConsent,
    };
  } else {
    consentLog(
      "Create a authorisation request url passing the request object by value"
    );
    authUrl = await fapiClient.authorizationUrl({
      request: requestObject,
      prompt: "consent",
    });
    consentLog(authUrl);
    return {
      authUrl,
      code_verifier,
      state,
      nonce,
      createdConsent: req.session.createdConsent,
    };
  }
}

function getPrivateKey(signingKey) {
  const key = crypto.createPrivateKey(signingKey);
  return key;
}

module.exports = {
  fetchData,
  getPathWithParams,
  getToken,
  getEndpoint,
  getPrivateKey,
  generateRequest
};
