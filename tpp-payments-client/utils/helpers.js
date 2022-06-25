let  paymentLog = require("debug")("tpp:payment"),
consentLog = require("debug")("tpp:consent");

const { TokenSet } = require("openid-client");
const path = require("path");
const certsPath = path.join(__dirname, "../certs/");
const fs = require("fs");
const crypto = require("crypto");
const  clientObj  = require("../index.js");

const USE_EXISTING_CLIENT = "USE_EXISTING_CLIENT";

async function getFapiClient(req, clientId = "", registrationAccessToken = "", drcOption = "") {

  try {
    const { fapiClient, localIssuer } = await clientObj.setupClient(
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
};

//Fetch data for the given API
async function fetchData(req, apiFamilyType, apiType, path = "") {

  const { fapiClient } = await getFapiClient(req, req.session.clientId, req.session.registrationAccessToken, USE_EXISTING_CLIENT);
  const client = fapiClient;

  const tokenSet = await getToken(req);
  req.session.refreshToken = tokenSet.refresh_token;
  req.session.accessToken = tokenSet.access_token;
  req.session.tokenSet = tokenSet;

  let pathRegex;
  if (apiFamilyType === "accounts") {
    pathRegex = "open-banking/accounts/v1/accounts$";
  } else if (apiFamilyType === "credit-cards-accounts") {
    pathRegex = "/open-banking/credit-cards-accounts/v1/accounts$";
  } else if (apiFamilyType === "resources") {
    pathRegex = "open-banking/resources/v1/resources$";
  } else if (apiFamilyType === "customers-business") {
    pathRegex = `open-banking/customers/v1/business/${apiType}$`;
  } else if (apiFamilyType === "customers-personal") {
    pathRegex = `open-banking/customers/v1/personal/${apiType}$`;
  } else if (apiFamilyType === "loans") {
    pathRegex = "open-banking/loans/v1/contracts$";
  } else if (apiFamilyType === "financings") {
    pathRegex = "open-banking/financings/v1/contracts$";
  } else if (apiFamilyType === "invoice-financings") {
    pathRegex = "open-banking/invoice-financings/v1/contracts$";
  } else if (apiFamilyType === "unarranged-accounts-overdraft") {
    pathRegex = "open-banking/unarranged-accounts-overdraft/v1/contracts$";
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

  paymentLog(`Getting ${apiType} response`);
  const response = await client.requestResource(
    endpoint,
    req.session.accessToken
  );

  const requestData = {
    endpoint,
    accessToken: req.session.accessToken,
  };

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
  const { fapiClient } = await getFapiClient(req, req.session.clientId, req.session.registrationAccessToken, USE_EXISTING_CLIENT);
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

function getPrivateKey(signingKey){
  const key = crypto.createPrivateKey(signingKey);
  return key;
}

function getCerts(certificates = {}){

  const certAuthority = certificates.hasOwnProperty(
    "ca.pem"
  )
    ? certificates["ca.pem"]
    : fs.readFileSync(certsPath + "ca.pem");

  const transportKey = certificates.hasOwnProperty(
    "transport.key"
  )
    ? certificates["transport.key"]
    : fs.readFileSync(certsPath + "transport.key");

  const transportPem = certificates.hasOwnProperty(
    "transport.pem"
  )
    ? certificates["transport.pem"]
    : fs.readFileSync(certsPath + "transport.pem");

  const signingKey = certificates.hasOwnProperty(
    "signing.key"
  )
    ? certificates["signing.key"]
    : fs.readFileSync(certsPath + "signing.key");

    return {certAuthority, transportKey, transportPem, signingKey};
}

module.exports = {
  fetchData,
  getPathWithParams,
  getToken,
  getEndpoint,
  getPrivateKey,
  getCerts,
  getFapiClient
};
