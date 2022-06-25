const express = require("express");
const { fetchData, getPathWithParams } = require("../utils/helpers.js");

const router = express.Router();

const UAO_ApiFamily = "unarranged-accounts-overdraft";
router.get("/unarranged-accounts-overdraft", async (req, res) => {

  const path = getPathWithParams(req.query);

  const response = await fetchData(req, UAO_ApiFamily, UAO_ApiFamily, path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/unarranged-accounts-overdraft/:contractId", async (req, res) => {
  const contractId = req.params.contractId;
  const response = await fetchData(
    req,
    UAO_ApiFamily,
    "unarranged-accounts-overdraft",
    `/${contractId}`
  );

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/unarranged-accounts-overdraft/:contractId/warranties", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/warranties${queryParams}`;

  const response = await fetchData(req, UAO_ApiFamily, "warranties", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/unarranged-accounts-overdraft/:contractId/scheduled-instalments", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/scheduled-instalments${queryParams}`;

  const response = await fetchData(req, UAO_ApiFamily, "scheduled-instalments", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/unarranged-accounts-overdraft/:contractId/payments", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/payments${queryParams}`;

  const response = await fetchData(req, UAO_ApiFamily, "payments", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

module.exports = router;