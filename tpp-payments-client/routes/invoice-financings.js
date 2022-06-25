const express = require("express");
const { fetchData, getPathWithParams } = require("../utils/helpers.js");

const router = express.Router();

const invoiceFinancingsApiFamily = "invoice-financings";
router.get("/invoice-financings", async (req, res) => {

  const path = getPathWithParams(req.query);

  const response = await fetchData(req, invoiceFinancingsApiFamily, invoiceFinancingsApiFamily, path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/invoice-financings/:contractId", async (req, res) => {
  const contractId = req.params.contractId;
  const response = await fetchData(
    req,
    invoiceFinancingsApiFamily,
    "invoice-financing",
    `/${contractId}`
  );

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/invoice-financings/:contractId/warranties", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/warranties${queryParams}`;

  const response = await fetchData(req, invoiceFinancingsApiFamily, "warranties", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/invoice-financings/:contractId/scheduled-instalments", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/scheduled-instalments${queryParams}`;

  const response = await fetchData(req, invoiceFinancingsApiFamily, "scheduled-instalments", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/invoice-financings/:contractId/payments", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/payments${queryParams}`;

  const response = await fetchData(req, invoiceFinancingsApiFamily, "payments", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

module.exports = router;