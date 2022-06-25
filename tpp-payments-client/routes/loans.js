const express = require("express");
const { fetchData, getPathWithParams } = require("../utils/helpers.js");

const router = express.Router();

const loansApiFamily = "loans";
router.get("/loans", async (req, res) => {

  const path = getPathWithParams(req.query);

  const response = await fetchData(req, loansApiFamily, loansApiFamily, path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/loans/:contractId", async (req, res) => {
  const contractId = req.params.contractId;
  const response = await fetchData(
    req,
    loansApiFamily,
    "loan",
    `/${contractId}`
  );

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/loans/:contractId/warranties", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/warranties${queryParams}`;

  const response = await fetchData(req, loansApiFamily, "warranties", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/loans/:contractId/scheduled-instalments", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/scheduled-instalments${queryParams}`;

  const response = await fetchData(req, loansApiFamily, "scheduled-instalments", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/loans/:contractId/payments", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/payments${queryParams}`;

  const response = await fetchData(req, loansApiFamily, "payments", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

module.exports = router;