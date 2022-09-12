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

  const dataObj = {
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
    delete dataObj.payment.date;
    req.session.paymentIsScheduled = true;
    dataObj.payment.schedule = {
      single: {
        date: req.body.date,
      },
    };
  } else {
    req.session.paymentIsScheduled = false;
  }

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
    JSON.parse(data),
    "PAYMENTS",
    req.session.selectedOrganisation,
    req
  );

  if (error) {
    const errorPayload = {
      msg: "Unable To Complete Creating Consent",
      payload: error,
    };
    errorPayload.stringify = JSON.stringify(errorPayload, null, 2);
    const paymentConsentInfo = JSON.parse(data);
    paymentConsentInfo.stringify = JSON.stringify(paymentConsentInfo, null, 2);

    req.session.createdConsent = {
      claims: undefined,
      errorPayload,
      paymentConsentInfo,
    };
    return res
      .status(302)
      .redirect("https://tpp.localhost:8080/payment-consent-response");
  }

  req.session.state = state;
  req.session.nonce = nonce;
  req.session.code_verifier = code_verifier;

  if (!authUrl) {
    return res.status(400).send({ message: "Bad Request" });
  }

  consentLog("Send customer to bank to give consent to the payment");
  return res.status(201).json({ authUrl });
});

router.get("/payments/payment-consent-response", (req, res) => {
  const consentReqObj = {
    ...req.session.consentRequestObject,
    tokenSet: req.session.consentRequestObject.ccToken,
  };

  return res.json({
    consentPayload: req.session.createdConsent,
    requestData: req.session.consentRequestData,
    consentReqObj: consentReqObj,
  });
});


router.post("/payments/make-payment", async (req, res) => {
  const { fapiClient } = await getFapiClient(
    req,
    req.session.clientId,
    req.session.registrationAccessToken,
    USE_EXISTING_CLIENT
  );
  const client = fapiClient;

  let consentPayload = req.body.consentPayload;
  consentPayload.stringify = JSON.stringify(
    req.body.consentPayload,
    null,
    2
  );
  paymentLog(
    "Find payment endpoint for the selected bank from the directory of participants"
  );
  const paymentEndpoint = getEndpoint(
    req.session.selectedAuthServer,
    "payments-pix",
    "open-banking/payments/v1/pix/payments$"
  );
  paymentLog("Payment endpoint found %O", paymentEndpoint);
  let date = new Date();
  const offset = date.getTimezoneOffset();
  date = new Date(date.getTime() - offset * 60 * 1000);
  let year    = new Intl.DateTimeFormat('en-GB', { year: 'numeric' }).format(date);
  let month   = new Intl.DateTimeFormat('en-GB', { month: '2-digit' }).format(date);
  let day     = new Intl.DateTimeFormat('en-GB', { day: '2-digit' }).format(date);
  let hour    = new Intl.DateTimeFormat('en-GB', { hour: '2-digit' }).format(date);
  let minute  = new Intl.DateTimeFormat('en-GB', { minute: '2-digit' }).format(date);
  let dateString = `${year}${month}${day}${hour}${minute}`;

  const payment = {
    creditorAccount: {
      number: req.body.payment_details_creditAccount_number,
      accountType: req.body.payment_details_creditAccount_accountType,
      ispb: req.body.payment_details_creditAccount_ispb,
      issuer: req.body.payment_details_creditAccount_issuer,
    },
    localInstrument: req.body.payment_details_localInstrument,
    proxy: req.body.payment_details_proxy,
    remittanceInformation: "Making a payment",
    cnpjInitiator: "59285411000113",
    payment: {
      amount: req.body.payment_amount,
      currency: "BRL",
    },
    endToEndId : "E99999004" + dateString + "123456789ab",
  };
  paymentLog("Create payment object %O", payment);
  paymentLog("Signing payment");
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
    .setAudience(paymentEndpoint)
    .setExpirationTime("5m")
    .sign(key);
  paymentLog("Signed payment JWT %O", jwt);
  paymentLog("Create payment resource using the signed payment JWT ");
  // const ccToken = await client.grant({
  //   grant_type: "client_credentials",
  //   scope: "payments",
  // });
  let paymentResponse = await client.requestResource(
    `${paymentEndpoint}`,
    req.session.tokenSet.access_token,
    {
      body: jwt,
      method: "POST",
      headers: {
        "content-type": "application/jwt",
        "x-idempotency-key": nanoid(),
      },
    }
  );
  paymentLog(
    "Payment resource created successfully %O",
    paymentResponse.body.toString()
  );
  paymentLog("Validate payment response as it is a JWT");
  paymentLog(
    "Retrieve the keyset for the bank (this has already been done and could be cached)"
  );
  //Validate the jwt came from teh correct bank and was meant to be sent to me.
  const JWKS = await jose.createRemoteJWKSet(
    new URL(
      `https://keystore.sandbox.directory.openbankingbrasil.org.br/${req.session.selectedOrganisation.OrganisationId}/application.jwks`
    )
  );
  let { payload } = await jose.jwtVerify(
    paymentResponse.body.toString(),
    JWKS,
    {
      issuer: req.session.selectedOrganisation.OrganisationId,
      audience: req.session.config.data.organisation_id,
      clockTolerance: 2,
    }
  );
  paymentLog("Payment response extracted and validated");
  if (payload.errors) {
    paymentLog(payload);
    const errorPayload = { msg: "Payment errored", payload: payload };
    errorPayload.stringify = JSON.stringify(errorPayload, null, 2);
    const paymentInfo = payment;
    paymentInfo.stringify = JSON.stringify(paymentInfo, null, 2);
    req.session.paymentResData = {
      errorPayload,
      paymentInfo,
      consentPayload,
    };
    return res.status(302).json({message:"Error"});
  }
  let x = 0;
  const loopPauseTime = req.session.useCustomConfig
    ? req.session.customEnvVars.loop_pause_time
    : process.env.LOOP_PAUSE_TIME;
  const numberOfCheckLoops = req.session.useCustomConfig
    ? req.session.customEnvVars.number_of_check_loops
    : process.env.NUMBER_OF_CHECK_LOOPS;
  while (!["ACSP", "ACCC", "RJCT", "SASC"].includes(payload.data.status)) {
    paymentLog(
      "Payment still not in a valid end state. Status: %O. Will check again to see if it has gone through.",
      payload.data.status
    );
    paymentLog(payload);
    paymentLog(
      "Use the self link on the payment to retrieve the latest record status. %O",
      payload.links.self
    );
    paymentResponse = await client.requestResource(
      payload.links.self,
      req.session.tokenSet.access_token,
      {
        headers: {
          accept: "application/jwt",
          "x-idempotency-key": nanoid(),
        },
      }
    );
    paymentLog("Validate and extract the payment response from the bank");
    ({ payload } = await jose.jwtVerify(
      paymentResponse.body.toString(),
      JWKS,
      {
        issuer: req.session.selectedOrganisation.OrganisationId,
        audience: req.session.config.data.organisation_id,
        clockTolerance: 2,
      }
    ));
    x = x + 1;
    await sleep(loopPauseTime);
    if (x > numberOfCheckLoops) {
      paymentLog(
        "Payment has not reached final state after 5 iterations, failing"
      );
      payload = { msg: "Unable To Complete Payment", payload: payload };
      try {
        payload.stringify = JSON.stringify(payload, null, 2);
      } catch (error) {
        console.log("Something went wrong ");
        console.log(error);
      }
      consentPayload = {
        msg: "Unable To Complete Payment",
        payload: req.body.consentPayload,
      };
      consentPayload.stringify = JSON.stringify(
        req.body.consentPayload,
        null,
        2
      );
      req.session.paymentResData = {
        payload,
        consentPayload,
      };
      return res.status(302).json({message:"Error"});
    }
  }
  paymentLog("Payment has reached a final state of", payload.data.status);
  paymentLog(payload);
  try {
    payload.stringify = JSON.stringify(payload, null, 2);
  } catch (e) {
    console.log("Something went wrong ");
    console.log(e);
  }
  consentPayload = req.body.consentPayload;
  consentPayload.stringify = JSON.stringify(
    req.body.consentPayload,
    null,
    2
  );
  req.session.paymentResData = {
    payload,
    consentPayload,
  };
  paymentLog("Payment execution complete");
  return res.status(201).json({message:"Payment execution complete"});
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

function sleep(ms) {
  paymentLog("Sleeping");
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

module.exports = router;

