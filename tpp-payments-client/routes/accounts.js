const express = require("express");
const { fetchData, getPathWithParams } = require("../utils/helpers.js");

const router = express.Router();

const accountsApiFamily = "accounts";
router.get("/accounts", async (req, res) => {

  const path = getPathWithParams(req.query);

  const response = await fetchData(req, accountsApiFamily, accountsApiFamily, path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/accounts/:accountId", async (req, res) => {
    const accountId = req.params.accountId;
    const response = await fetchData(
      req,
      accountsApiFamily,
      "account",
      `/${accountId}`
    );

    return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/accounts/:accountId/overdraft-limits", async (req, res) => {
    const accountId = req.params.accountId;
    const path = `/${accountId}/overdraft-limits`;

    const response = await fetchData(
      req,
      accountsApiFamily,
      "overdraft limits",
      path
    );

    return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/accounts/:accountId/balances", async (req, res) => {
    const accountId = req.params.accountId;
    const path = `/${accountId}/balances`;

    const response = await fetchData(req, accountsApiFamily, "balances", path);

    return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
  });

router.get("/accounts/:accountId/transactions", async (req, res) => {
    const accountId = req.params.accountId;
    const queryParams = getPathWithParams(req.query);
    const path = `/${accountId}/transactions${queryParams}`;

    const response = await fetchData(
      req,
      accountsApiFamily,
      "transactions",
      path
    );

    return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
  });

module.exports = router;
