"use strict";
require("dotenv").config();
let paymentLog = require("debug")("tpp:payment"),
  setupLog = require("debug")("tpp:setup"),
  consentLog = require("debug")("tpp:consent"),
  commsLog = require("debug")("tpp:communications");
const config = require("./config");

(async () => {
  const { custom } = require("openid-client");
  const fs = require("fs");
  const express = require("express");
  const cookieParser = require("cookie-parser");
  const app = express();
  const path = require("path");
  const https = require("https");
  const { default: axios } = require("axios");
  const certsPath = path.join(__dirname, "./certs/");
  const jose = require("jose");
  const { nanoid } = require("nanoid");
  const cors = require("cors");
  const session = require("express-session");
  const MongoDBStore = require("connect-mongodb-session")(session);
  const uploadFile = require("express-fileupload");

  const { getEndpoint, getPrivateKey } = require("./utils/helpers.js");
  const { getCerts, getFapiClient } = require("./utils/fapiClient.js");
  const { connectToDatabase, getDatabase } = require("./database/connectToDb");

  const accountRoutes = require("./routes/accounts.js");
  const resourcesRoutes = require("./routes/resources.js");
  const creditCardAccountsRoutes = require("./routes/credit-card-accounts.js");
  const customersBusinessRoutes = require("./routes/customers-business.js");
  const customersPersonalRoutes = require("./routes/customers-personal.js");
  const lonasRoutes = require("./routes/loans.js");
  const financingsRoutes = require("./routes/financings.js");
  const invoiceFinancingsRoutes = require("./routes/invoice-financings.js");
  const unarrangedAccountsOverdraftRoutes = require("./routes/unarranged-accounts-overdraft.js");
  const paymentsRoutes = require("./routes/payments.js");
  const consentRoutes = require("./routes/consent.js");
  const consentsRoutes = require("./routes/consents.js");

  app.use(cors({ credentials: true, origin: "https://tpp.localhost:8080" }));

  //A lot of oauth 2 bodies are form url encoded
  app.use(express.urlencoded({ extended: true }));

  const USE_EXISTING_CLIENT = "USE_EXISTING_CLIENT";

  // configure MongoDB store for sessions
  const store = new MongoDBStore({
    uri: "mongodb://localhost:27017/MockTPP",
    collection: "sessions",
  });

  // when connectd to the mongoDB, start the server
  connectToDatabase(startServer);

  let db;
  function startServer(connected) {
    if (connected) {
      db = getDatabase();
      https
        .createServer(
          {
            // ...
            key: fs.readFileSync(certsPath + "transport.key"),
            cert: fs.readFileSync(certsPath + "transport.pem"),
            // ...
          },
          app
        )
        .listen(443);

      console.log("Node.js web server at port 443 is running..");
    } else {
      throw new Error("Failed to connect to the mongoDB database");
    }
  }

  //secret should be a lnog string value in production
  app.use(
    session({
      secret: "boo",
      resave: false,
      saveUninitialized: false,
      cookie: { secure: true, sameSite: "none", httpOnly: false },
      store: store,
    })
  );

  // parse various different custom JSON types as JSON
  app.use(express.json({ type: "application/json" }));

  function getHttpsAgent(certificates = {}) {
    const { certAuthority, transportKey, transportPem } =
      getCerts(certificates);
    //We need to setup an mtls certificate httpsAgent
    //NOTE: Do NOT leave rejectUnauthorized as 'false' as this disables certificate chain verification
    const httpsAgent = new https.Agent({
      ca: certAuthority,
      key: transportKey,
      cert: transportPem,
      rejectUnauthorized: false,
    });

    return httpsAgent;
  }

  async function initConfig(req, res, flag = "") {
    const { certAuthority, transportKey, transportPem, signingKey } = getCerts(
      req.session.certificates
    );

    //We need to confirm our private key into a jwks for local signing
    const key = getPrivateKey(signingKey);
    req.session.privateJwk = await jose.exportJWK(key);
    req.session.privateJwk.kid = req.session.customConfig
      ? req.session.customConfig.data.signing_kid
      : config.data.signing_kid;
    setupLog("Create private jwk key %O", req.session.privateJwk);
    req.session.keyset = {
      keys: [req.session.privateJwk],
    };

    //Set some logging options so that we log evrey request and response in an easy to use pattern
    req.session.loggingOptions = {
      hooks: {
        beforeRequest: [
          (options) => {
            commsLog(
              "--> %s %s",
              options.method.toUpperCase(),
              options.url.href
            );
            commsLog("--> HEADERS %o", options.headers);
            if (options.body) {
              commsLog("--> BODY %s", options.body);
            }
            if (options.form) {
              commsLog("--> FORM %s", options.form);
            }
          },
        ],
        afterResponse: [
          (response) => {
            commsLog(
              "<-- %i FROM %s %s",
              response.statusCode,
              response.request.options.method.toUpperCase(),
              response.request.options.url.href
            );
            commsLog("<-- HEADERS %o", response.headers);
            if (response.body) {
              commsLog("<-- BODY %s", response.body);
            }
            return response;
          },
        ],
      },
      timeout: 20000,
      https: {
        certificateAuthority: certAuthority,
        certificate: transportPem,
        key: transportKey,
        rejectUnauthorized: false,
      },
    };
    custom.setHttpOptionsDefaults(req.session.loggingOptions);

    if (flag === "CUSTOM_CERTS") {
      return;
    }

    res.send("success");
  }

  app.get("/", initConfig);

  function sleep(ms) {
    paymentLog("Sleeping");
    return new Promise((resolve) => {
      setTimeout(resolve, ms);
    });
  }

  //Express setup routes
  app.use(cookieParser());
  app.use(express.static(path.join(__dirname, "public")));

  app.use("/banks/:option", async (req, res) => {
    
    let apiFamilyType;
    let ApiVersion;
    if(req.params.option === "payments"){
      apiFamilyType = "payments-consents";
    } else if (req.params.option === "customer-data-v1"){
      apiFamilyType = "customers-personal";
    } else {
      ApiVersion = "v2";
    }

    //Retrieve the information from the open banking brazil directory of participants on launch
    const instance = axios.create({
      httpsAgent: getHttpsAgent(req.session.certificates),
    });
    setupLog("Retrieving Banks from Directory of Participants");
    const axiosResponse = await instance.get(
      "https://data.sandbox.directory.openbankingbrasil.org.br/participants"
    );

    const TotalBanks = axiosResponse.data;
    req.session.availableBanks = TotalBanks.filter((e) => {
      return e.AuthorisationServers.some((as) => {
        return as.ApiResources.some((resource) => {
          if(apiFamilyType){
            return resource.ApiFamilyType === apiFamilyType;
          } else if (ApiVersion) {
            return resource.ApiDiscoveryEndpoints.some((endpointObj) => {
              return endpointObj.ApiEndpoint.includes(ApiVersion);
            });
          }
        });
      });
    });

    consentLog(
      "Providing a list of banks to the customer for them to choose from the UI"
    );
    res.json(req.session.availableBanks);
  });

  //replace the default certs if the user wants to
  function saveFile(file) {
    const filename = file.name;
    file.mv("./certs/" + filename, function (error) {
      if (error) {
        return false;
      } else {
        return true;
      }
    });
  }

  app.use(uploadFile());
  app.post("/change-config", async (req, res, next) => {
    if (req.files && JSON.parse(req.body.replace_existing_certs)) {
      if (req.files && !Array.isArray(req.files.certificates)) {
        saveFile(req.files.certificates);
      } else {
        req.files.certificates.forEach((cert) => {
          saveFile(cert);
        });
      }
    }

    if (req.body.accept_any_certificates) {
      process.env.NODE_TLS_REJECT_UNAUTHORIZED = 0;
    }

    req.session.useCustomConfig = true;

    const certsObj = {};

    if (req.files) {
      if (req.files && !Array.isArray(req.files.certificates)) {
        certsObj[req.files.certificates.name] =
          req.files.certificates.data.toString();
      } else {
        req.files.certificates.forEach((cert) => {
          certsObj[cert.name] = cert.data.toString();
        });
      }
    }

    req.session.certificates = certsObj;

    req.session.customConfig = {
      data: {
        client: {
          application_type: req.body.application_type,
          grant_types: [
            "client_credentials",
            "authorization_code",
            "refresh_token",
            "implicit",
          ],
          id_token_signed_response_alg: req.body.id_token_signed_response_alg,
          require_auth_time: JSON.parse(req.body.require_auth_time),
          response_types: ["code id_token", "code"],
          subject_type: req.body.subject_type,
          token_endpoint_auth_method: req.body.token_endpoint_auth_method,
          request_object_signing_alg: req.body.request_object_signing_alg,
          require_signed_request_object: JSON.parse(
            req.body.require_signed_request_object
          ),
          require_pushed_authorization_requests: JSON.parse(
            req.body.require_pushed_authorization_requests
          ),
          tls_client_certificate_bound_access_tokens: JSON.parse(
            req.body.tls_client_certificate_bound_access_tokens
          ),
          client_id: req.body.client_id,
          jwks_uri: req.body.jwks_uri,
          tls_client_auth_subject_dn: req.body.tls_client_auth_subject_dn,
          authorization_signed_response_alg:
            req.body.authorization_signed_response_alg,
        },
        signing_kid: req.body.signing_kid,
        software_statement_id: req.body.software_statement_id,
        organisation_id: req.body.organisation_id,
      },
    };

    req.session.customEnvVars = {
      loop_pause_time: req.body.loop_pause_time,
      number_of_check_loops: req.body.number_of_check_loops,
      preferred_token_auth_mech: req.body.preferred_token_auth_mech,
    };

    await initConfig(req, res, "CUSTOM_CERTS");

    return res.status(201).json({ message: "Form Submitted Successfully" });
  });

  app.use(paymentsRoutes);

  app.use(express.urlencoded());

  //This is used for response mode form_post, query and form_post are the most common
  app.post("/cb", async (req, res) => {
    const { fapiClient, localIssuer } = await getFapiClient(
      req,
      req.session.clientId,
      req.session.registrationAccessToken,
      USE_EXISTING_CLIENT
    );
    const client = fapiClient;
    const issuer = localIssuer;

    consentLog("Received redirect from the bank");
    const callbackParams = client.callbackParams(req);
    consentLog("Trying to obtain an access token using the authorization code");
    const tokenSet = await client.callback(
      "https://tpp.localhost/cb",
      callbackParams,
      {
        code_verifier: req.session.code_verifier,
        state: req.session.state,
        nonce: req.session.nonce,
        response_type: "code",
      },
      {
        clientAssertionPayload: {
          aud: issuer.mtls_endpoint_aliases.token_endpoint,
        },
      }
    );
    consentLog("Access token obtained. %O", tokenSet);

    req.session.refreshToken = tokenSet.refresh_token;
    req.session.accessToken = tokenSet.access_token;
    req.session.tokenSet = tokenSet;

    let apiFamilyType;
    let apiEndpointRegex;
    let scope;
    let requestOptions;
    consentLog(
      "Find the consent endpoint to check the status of the consent resource"
    );
    if (req.session.flag === "PAYMENTS") {
      apiFamilyType = "payments-consents";
      apiEndpointRegex = "open-banking/payments/v1/consents$";
      scope = "payments";
      requestOptions = {
        headers: {
          accept: "application/jwt",
          "x-idempotency-key": nanoid(),
        },
      };
    } else {
      apiFamilyType = "consents";
      apiEndpointRegex = "open-banking/consents/v1/consents$";
      scope = "consents";
      requestOptions = {
        headers: {
          method: "GET",
          accept: "application/json",
        },
      };
    }

    //Cut down version of the consent process
    const consentEndpoint = getEndpoint(
      req.session.selectedAuthServer,
      apiFamilyType,
      apiEndpointRegex
    );
    consentLog("Consent endpoint found %O", consentEndpoint);
    consentLog("Obtaining an access token to create the consent record");
    const ccToken = await client.grant({
      grant_type: "client_credentials",
      scope,
    });

    const JWKS = await jose.createRemoteJWKSet(
      new URL(
        `https://keystore.sandbox.directory.openbankingbrasil.org.br/${req.session.selectedOrganisation.OrganisationId}/application.jwks`
      )
    );

    let y = 0;
    while (!["AUTHORISED"].includes(req.session.createdConsent.data.status)) {
      //Create the consent
      consentLog("Get the consent record");
      const consentId = req.session.createdConsent.data.consentId;
      req.session.createdConsent = await client.requestResource(
        `${consentEndpoint}/${consentId}`,
        ccToken,
        requestOptions
      );

      req.session.consentRequestData = {
        endpoint: `${consentEndpoint}/${consentId}`,
        tokenSet: ccToken,
        requestOptions,
      };

      let payload;
      if (req.session.flag === "PAYMENTS") {
        //Errors processing a JWT are sent as a
        consentLog(
          "Validate the Consent Response JWT to confirm it was signed correctly by the bank"
        );

        //Validate the jwt
        let result = await jose.jwtVerify(
          req.session.createdConsent.body.toString(),
          JWKS,
          {
            issuer: req.session.selectedOrganisation.OrganisationId,
            audience: req.session.config.data.organisation_id,
            clockTolerance: 2,
          }
        );
        payload = result.payload;
        //Update the consent
        req.session.createdConsent = payload;
        consentLog(
          "Consent response payload validated and extracted successfully"
        );
        consentLog(req.session.createdConsent);
      } else {
        req.session.createdConsent = JSON.parse(
          req.session.createdConsent.body.toString()
        );
      }

      const loopPauseTime = req.session.useCustomConfig
        ? req.session.customEnvVars.loop_pause_time
        : process.env.LOOP_PAUSE_TIME;
      await sleep(loopPauseTime);

      const numberOfCheckLoops = req.session.useCustomConfig
        ? req.session.customEnvVars.number_of_check_loops
        : process.env.NUMBER_OF_CHECK_LOOPS;
      y = y + 1;
      if (y > numberOfCheckLoops) {
        consentLog(
          "Consent has not reached authorised state after 5 iterations, failing"
        );
        let errorPayload = {
          msg: "Unable To Complete Authorisation - State Not Authorised",
          payload: payload,
        };

        try {
          payload.stringify = JSON.stringify(payload, null, 2);
        } catch (error) {
          console.log("Something went wrong");
          console.log(error);
        }

        req.session.paymentResData = {
          claims: tokenSet.claims(),
          errorPayload,
        };
        return res
          .status(302)
          .redirect("https://tpp.localhost:8080/payment-response");
      }
    }

    consentLog("Consent process finished");
    if (req.session.flag === "PAYMENTS") {
      let consentPayload = req.session.createdConsent;
      consentPayload.stringify = JSON.stringify(
        req.session.createdConsent,
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

      const payment = {
        creditorAccount:
          req.session.createdConsent.data.payment.details.creditorAccount,
        localInstrument:
          req.session.createdConsent.data.payment.details.localInstrument,
        proxy: req.session.createdConsent.data.payment.details.proxy,
        remittanceInformation: "Making a payment",
        cnpjInitiator: "59285411000113",
        payment: {
          amount: req.session.createdConsent.data.payment.amount,
          currency: req.session.createdConsent.data.payment.currency,
        },
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
      let paymentResponse = await client.requestResource(
        `${paymentEndpoint}`,
        tokenSet,
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
        const errorPayload = { msg: "Payment errored", payload: payload };
        errorPayload.stringify = JSON.stringify(errorPayload, null, 2);
        const paymentInfo = payment;
        paymentInfo.stringify = JSON.stringify(paymentInfo, null, 2);

        req.session.paymentResData = {
          claims: tokenSet.claims(),
          errorPayload,
          paymentInfo,
          consentPayload,
        };
        return res
          .status(302)
          .redirect("https://tpp.localhost:8080/payment-response");
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
          tokenSet,
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
            payload: req.session.createdConsent,
          };
          consentPayload.stringify = JSON.stringify(
            req.session.createdConsent,
            null,
            2
          );

          req.session.paymentResData = {
            claims: tokenSet.claims(),
            payload,
            consentPayload,
          };

          return res
            .status(302)
            .redirect("https://tpp.localhost:8080/payment-response");
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

      consentPayload = req.session.createdConsent;
      consentPayload.stringify = JSON.stringify(
        req.session.createdConsent,
        null,
        2
      );

      req.session.paymentResData = {
        claims: tokenSet.claims(),
        payload,
        consentPayload,
      };

      paymentLog("Payment execution complete");
      return res
        .status(302)
        .redirect("https://tpp.localhost:8080/payment-response");
    }

    paymentLog("Consent execution complete");
    return res
      .status(302)
      .redirect("https://tpp.localhost:8080/consent-response-menu");
  });

  app.get("/clients", async (req, res) => {
    const clients = [];
    await db
      .collection("clients")
      .find()
      .forEach((client) => {
        clients.push(client);
      });

    res.status(200).json(clients);
  });

  app.post("/dcr", async (req, res) => {
    const selectedBank = req.body.bank;
    const selectedDcrOption = req.body.selectedDcrOption;
    const theClientId = req.body.clientId;
    const registrationAccessToken = req.body.registrationAccessToken;

    if (selectedBank) {
      req.session.selectedBank = selectedBank;
    }
    let client;
    let issuer;
    if (req.session.selectedBank) {
      //Setup the client
      consentLog(
        "Customer has select bank issuer to use %O",
        req.session.selectedBank
      );

      try {
        const { fapiClient, localIssuer } = await getFapiClient(
          req,
          theClientId,
          registrationAccessToken,
          selectedDcrOption
        );
        consentLog("Client is ready to talk to the chosen bank");
        client = fapiClient;
        issuer = localIssuer;
      } catch (error) {
        res.status(500).json({ error });
      }
    } else {
      throw Error("No bank was selected");
    }

    if (!client || !client.client_id) {
      console.log(" Something went catastrophically wrong with DCR ");
      console.log(" Please check configuration and certificates! ");
      return;
    }

    req.session.clientId = client.client_id;
    req.session.registrationAccessToken = client.registration_access_token;

    return res.send({
      clientId: client.client_id,
      registrationAccessToken: client.registration_access_token,
      message: "Client is setup",
      scope: client.scope,
    });
  });

  app.use(consentRoutes);

  app.use(accountRoutes);

  app.use(resourcesRoutes);

  app.use(creditCardAccountsRoutes);

  app.use(customersBusinessRoutes);

  app.use(customersPersonalRoutes);

  app.use(lonasRoutes);

  app.use(financingsRoutes);

  app.use(invoiceFinancingsRoutes);

  app.use(unarrangedAccountsOverdraftRoutes);

  app.use(consentsRoutes);
})();
