const express = require("express");
let paymentLog = require("debug")("tpp:payment"),
  consentLog = require("debug")("tpp:consent");
  
const cors = require("cors");
const jose = require("jose");
const { nanoid } = require("nanoid");

const {
  getEndpoint,
  getPrivateKey,
  generateRequest,
} = require("../utils/helpers.js");

const { getCerts, getFapiClient } = require("../utils/fapiClient.js");

const router = express.Router();

const USE_EXISTING_CLIENT = "USE_EXISTING_CLIENT";

router.post("/payments/payment-consent", async (req, res) => {
  consentLog("Starting a new payment consent");
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
      date: date.toISOString().split("T")[0],
      amount: req.body.payment_amount,
      currency: "BRL",
      details: {
        proxy: "12345678901",
        localInstrument: req.body.payment_details_localInstrument,
        creditorAccount: {
          number: req.body.payment_details_creditAccount_number,
          accountType: req.body.payment_details_creditAccount_accountType,
          ispb: req.body.payment_details_creditAccount_ispb,
          issuer: req.body.payment_details_creditAccount_issuer,
        },
      },
      type: "PIX",
    },
  };

  //if date has been selected
  if (req.body.selected === "Yes") {
    delete data.payment.date;
    req.session.paymentIsScheduled = true;
    data.payment.schedule = {
      single: {
        date: req.body.date,
      },
    };
  } else {
    req.session.paymentIsScheduled = false;
  }

  req.session.paymentConsentPayload = JSON.stringify(data);

  consentLog(
    "Sending customer to select the bank they want to make the payment from in the next request from the front-end"
  );
  return res.status(200).json({ message: "success" });
});

router.options("/payments/make-payment", cors());
router.post("/payments/make-payment", async (req, res) => {
  const { fapiClient } = await getFapiClient(
    req,
    req.session.clientId,
    req.session.registrationAccessToken,
    USE_EXISTING_CLIENT
  );
  const client = fapiClient;

  let response;
  try {
    //Setup the request
    response = await generateRequest(
      client,
      req.session.selectedAuthServer,
      JSON.parse(req.session.paymentConsentPayload),
      "PAYMENTS",
      req.session.selectedOrganisation,
      req
    );
  } catch (error) {
    return res
      .status(500)
      .json({ message: "unable to generate request", error: error });
  }

  const { authUrl, code_verifier, state, nonce, error } = response;

  if (error) {
    const errorPayload = {
      msg: "Unable To Complete Payment",
      payload: error,
    };
    errorPayload.stringify = JSON.stringify(errorPayload, null, 2);
    const paymentInfo = JSON.parse(req.session.paymentConsentPayload);
    paymentInfo.stringify = JSON.stringify(paymentInfo, null, 2);

    req.session.paymentResData = {
      claims: undefined,
      errorPayload,
      paymentInfo,
    };
    return res
      .status(500)
      .send(error);
  }

  req.session.state = state;
  req.session.nonce = nonce;
  req.session.code_verifier = code_verifier;

  consentLog("Send customer to bank to give consent to the payment");
  return res.json({ authUrl });
});

router.get("/payments/payment-response", (req, res) => {
  const paymentResponse = {
    ...req.session.paymentResData,
    clientId: req.session.clientId,
    refreshToken: req.session.refreshToken,
    scheduled: req.session.paymentIsScheduled,
  };

  return res.json(paymentResponse);
});

router.get("/payments/:paymentId", async (req, res, next) => {
  const paymentId = req.params.paymentId;

  const { fapiClient } = await getFapiClient(
    req,
    req.session.clientId,
    req.session.registrationAccessToken,
    USE_EXISTING_CLIENT
  );
  const client = fapiClient;


  const paymentEndpoint = `${getEndpoint(
    req.session.selectedAuthServer,
    "payments-pix",
    "open-banking/payments/v1/pix/payments$"
  )}/${paymentId}`;
  paymentLog("Payment endpoint found %O", paymentEndpoint);

  consentLog("Obtaining Payment Access Token");
  const ccToken = await client.grant({
    grant_type: "client_credentials",
    scope: "payments",
  });

  paymentLog("Getting payment response");
  const response = await client.requestResource(paymentEndpoint, ccToken, {
    method: "GET",
    headers: {
      accept: "application/jwt",
      "x-idempotency-key": nanoid(),
    },
  });
  paymentLog("Payment response recieved %O", response.body.toString());

  consentLog(
    "Retrieve the keyset for the bank sending the payment consent response from the diretory of participants"
  );
  const JWKS = await jose.createRemoteJWKSet(
    new URL(
      `https://keystore.sandbox.directory.openbankingbrasil.org.br/${req.session.selectedOrganisation.OrganisationId}/application.jwks`
    )
  );
  
  const { payload } = await jose.jwtVerify(response.body.toString(), JWKS, {
    issuer: req.session.selectedOrganisation.OrganisationId,
    audience: req.session.config.data.organisation_id,
    clockTolerance: 2,
  });
  
  res
    .status(response.statusCode)
    .json({ ...payload, selectedBank: req.session.selectedBank });
});

router.get("/payments/payment-consent/:consentId", async (req, res) => {
  const consentId = req.params.consentId;

  const { fapiClient } = await getFapiClient(
    req,
    req.session.clientId,
    req.session.registrationAccessToken,
    USE_EXISTING_CLIENT
  );
  const client = fapiClient;

  consentLog(
    "Find the patch consent endpoint for the payments consent from the selected authorisation server from the directory"
  );
  const patchEndpoint = `${getEndpoint(
    req.session.selectedAuthServer,
    "payments-consents",
    "open-banking/payments/v1/consents$"
  )}/${consentId}`;

  consentLog("Obtaining an access token to patch payment");
  const ccToken = await client.grant({
    grant_type: "client_credentials",
    scope: "payments",
  });

  paymentLog("Getting patch payment response ");
  const response = await client.requestResource(patchEndpoint, ccToken, {
    method: "GET",
    headers: {
      accept: "application/jwt",
      "x-idempotency-key": nanoid(),
    },
  });
  paymentLog("Revoked payment response recieved %O", response.body.toString());

  consentLog(
    "Retrieve the keyset for the bank sending the payment consent response from the diretory of participants"
  );
  const JWKS = await jose.createRemoteJWKSet(
    new URL(
      `https://keystore.sandbox.directory.openbankingbrasil.org.br/${req.session.selectedOrganisation.OrganisationId}/application.jwks`
    )
  );

  const { payload } = await jose.jwtVerify(response.body.toString(), JWKS, {
    issuer: req.session.selectedOrganisation.OrganisationId,
    audience: req.session.config.data.organisation_id,
    clockTolerance: 2,
  });

  res.status(response.statusCode).json(payload);
});

router.patch("/payments/revoke-payment", async (req, res) => {
  const consentId = req.session.createdConsent.data.consentId;

  const { fapiClient } = await getFapiClient(
    req,
    req.session.clientId,
    req.session.registrationAccessToken,
    USE_EXISTING_CLIENT
  );
  const client = fapiClient;

  consentLog(
    "Find the patch consent endpoint for the payments consent from the selected authorisation server from the directory"
  );
  const patchEndpoint = `${getEndpoint(
    req.session.selectedAuthServer,
    "payments-consents",
    "open-banking/payments/v1/consents$"
  )}/${consentId}`;

  const payment = {
    status: "REVOKED",
    revocation: {
      loggedUser: {
        document: {
          identification: req.body.document_identification,
          rel: req.body.document_rel,
        },
      },
      revokedBy: req.body.revoked_by,
      reason: {
        code: req.body.code,
        additionalInformation: req.body.additional_info,
      },
    },
  };

  const { signingKey } = getCerts(req.session.certificates);
  const key = getPrivateKey(signingKey);

  const jwt = await new jose.SignJWT({ data: payment })
    .setProtectedHeader({
      alg: "PS256",
      typ: "JWT",
      kid: req.session.privateJwk.kid,
    })
    .setIssuedAt()
    .setIssuer(req.session.config.data.organisation_id)
    .setJti(nanoid())
    .setAudience(patchEndpoint)
    .setExpirationTime("5m")
    .sign(key);
  consentLog("Log patch signing payment consent JWT");
  consentLog(jwt);

  consentLog("Obtaining an access token to patch payment");
  const ccToken = await client.grant({
    grant_type: "client_credentials",
    scope: "payments",
  });

  paymentLog("Getting patch payment response ");
  const response = await client.requestResource(patchEndpoint, ccToken, {
    method: "PATCH",
    body: jwt,
    headers: {
      "content-type": "application/jwt",
      "x-idempotency-key": nanoid(),
    },
  });
  paymentLog("Revoked payment response recieved %O", response.body.toString());

  consentLog(
    "Retrieve the keyset for the bank sending the payment consent response from the diretory of participants"
  );
  const JWKS = await jose.createRemoteJWKSet(
    new URL(
      `https://keystore.sandbox.directory.openbankingbrasil.org.br/${req.session.selectedOrganisation.OrganisationId}/application.jwks`
    )
  );

  const { payload } = await jose.jwtVerify(response.body.toString(), JWKS, {
    issuer: req.session.selectedOrganisation.OrganisationId,
    audience: req.session.config.data.organisation_id,
    clockTolerance: 2,
  });

  res.status(response.statusCode).json(payload);
});

module.exports = router;

