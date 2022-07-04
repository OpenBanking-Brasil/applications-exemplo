const express = require("express");
const { fetchData } = require("../utils/helpers.js");

const router = express.Router();

router.get("/customers-personal/identifications", async (req, res) => {
  const response = await fetchData(
    req,
    "customers-personal",
    "identifications"
  );

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

router.get("/customers-personal/financial-relations", async (req, res) => {
  const response = await fetchData(
    req,
    "customers-personal",
    "financial-relations"
  );

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

router.get("/customers-personal/qualifications", async (req, res) => {
  const response = await fetchData(req, "customers-personal", "qualifications");

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

module.exports = router;
