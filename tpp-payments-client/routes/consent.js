let consentLog = require("debug")("tpp:consent");
const express = require("express");


const { getFapiClient } = require("../utils/fapiClient.js");
const { generateRequest } = require("../utils/helpers.js");

const router = express.Router();

const USE_EXISTING_CLIENT = "USE_EXISTING_CLIENT";

router.post("/consent/create-consent", async (req, res) => {
  const grantedPermissions = req.body.permissionsArr.map((permissionData) => {
    return permissionData.permissions.map((data) => {
      return data.permission;
    });
  });

  const optionWords = req.body.ApiOption.split("-");
  req.session.ApiVersion = optionWords[optionWords.length - 1];

  //granted permissions categories
  req.session.consentsArr = req.body.permissionsArr.map((permissionData) => {
    return {
      category: permissionData.dataCategory,
      id: permissionData.id,
      group: permissionData.group,
      permissions: permissionData.permissions,
    };
  });

  let permissions = [];
  for (let permission of grantedPermissions) {
    permissions.push(...permission);
  }

  permissions = uniq = [...new Set(permissions)];

  const loggedUserId = req.body.loggedUserId || undefined;
  const loggedUserRel = req.body.loggedUserRel || undefined;

  const businessEntityId = req.body.businessEntityId || undefined;
  const businessEntityRel = req.body.businessEntityRel || undefined;

  const expirationDate = req.body.expirationDate || undefined;
  const expirationTime = req.body.expirationTime || undefined;

  const transactionFromDate = req.body.transactionFromDate || undefined;
  const transactionFromTime = req.body.transactionFromTime || undefined;

  const transactionToDate = req.body.transactionToDate || undefined;
  const transactionToTime = req.body.transactionToTime || undefined;

  const dataObj = {
    data: {
      permissions,
    },
  };

  if (loggedUserId && loggedUserRel) {
    dataObj.data.loggedUser = {
      document: {
        identification: loggedUserId,
        rel: loggedUserRel,
      },
    };
  }

  if (businessEntityId && businessEntityRel) {
    dataObj.data.businessEntity = {
      document: {
        identification: businessEntityId,
        rel: businessEntityRel,
      },
    };
  }

  let transactionFromDateTime;
  if (transactionFromDate && transactionFromTime) {
    transactionFromDateTime =
      transactionFromDate + "T" + transactionFromTime + "Z";
    dataObj.data.transactionFromDateTime = transactionFromDateTime;
  }

  let transactionToDateTime;
  if (transactionToDate && transactionToTime) {
    transactionToDateTime = transactionToDate + "T" + transactionToTime + "Z";
    dataObj.data.transactionToDateTime = transactionToDateTime;
  }

  let expirationDateTime;
  if (expirationDate && expirationTime) {
    expirationDateTime = expirationDate + "T" + expirationTime + "Z";
  } else {
    let date = new Date();
    const offset = date.getTimezoneOffset();
    date = new Date(date.getTime() - offset * 60 * 1000);
    date.setDate(date.getDate() + 30);
    expirationDateTime = date.toISOString();
  }
  dataObj.data.expirationDateTime = expirationDateTime;

  const data = JSON.stringify(dataObj);

  const { fapiClient } = await getFapiClient(
    req,
    req.session.clientId,
    req.session.registrationAccessToken,
    USE_EXISTING_CLIENT
  );
  const client = fapiClient;

  //Setup the request
  const { authUrl, code_verifier, state, nonce, error } = await generateRequest(
    client,
    req.session.selectedAuthServer,
    data,
    "CONSENT",
    req.session.selectedOrganisation,
    req
  );

  if (error) {
    const errorPayload = {
      msg: "Unable To Complete Creating Consent",
      payload: error,
    };
    errorPayload.stringify = JSON.stringify(errorPayload, null, 2);
    const consentInfo = JSON.parse(data);
    consentInfo.stringify = JSON.stringify(consentInfo, null, 2);

    req.session.createdConsent = {
      claims: undefined,
      errorPayload,
      consentInfo,
    };
    return res
      .status(302)
      .redirect("https://tpp.localhost:8080/consent-response-menu");
  }

  req.session.state = state;
  req.session.nonce = nonce;
  req.session.code_verifier = code_verifier;

  if (!authUrl) {
    return res.status(400).send({ message: "Bad Request" });
  }

  consentLog("Send customer to bank to give consent to the customer data");
  return res.status(201).send({ authUrl });
});

router.get("/consent/consent-response", (req, res) => {
  return res.json({
    consent: req.session.createdConsent,
    permissionsData: req.session.consentsArr,
    requestData: req.session.consentRequestData,
  });
});

module.exports = router;
