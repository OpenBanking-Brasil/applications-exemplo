const express = require("express");
const { fetchData } = require("../utils/helpers.js");

const router = express.Router();

router.get("/consents/:consentId", async (req, res) => {
  const consentId = req.params.consentId;
  const response = await fetchData(
    req,
    "consents",
    "consents",
    `/${consentId}`
  );

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

router.delete("/consents/:consentId", async (req, res) => {
  const consentId = req.params.consentId;
  const response = await fetchData(
    req,
    "consents",
    "consents",
    `/${consentId}`,
    "DELETE"
  );

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

module.exports = router;
