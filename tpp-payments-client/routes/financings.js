const express = require("express");
const { fetchData, getPathWithParams } = require("../utils/helpers.js");

const router = express.Router();

const financingsApiFamily = "financings";
router.get("/financings", async (req, res) => {

  const path = getPathWithParams(req.query);

  const response = await fetchData(req, financingsApiFamily, financingsApiFamily, path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/financings/:contractId", async (req, res) => {
  const contractId = req.params.contractId;
  const response = await fetchData(
    req,
    financingsApiFamily,
    "financing",
    `/${contractId}`
  );

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/financings/:contractId/warranties", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/warranties${queryParams}`;

  const response = await fetchData(req, financingsApiFamily, "warranties", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/financings/:contractId/scheduled-instalments", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/scheduled-instalments${queryParams}`;

  const response = await fetchData(req, financingsApiFamily, "scheduled-instalments", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

router.get("/financings/:contractId/payments", async (req, res) => {
  const contractId = req.params.contractId;
  const queryParams = getPathWithParams(req.query);
  const path = `/${contractId}/payments${queryParams}`;

  const response = await fetchData(req, financingsApiFamily, "payments", path);

  return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
});

module.exports = router;
