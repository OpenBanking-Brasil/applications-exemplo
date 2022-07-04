const express = require("express");
const { fetchData } = require("../utils/helpers.js");

const router = express.Router();

router.get("/customers-business/identifications", async (req, res) => {
  const response = await fetchData(
    req,
    "customers-business",
    "identifications"
  );

  return res
    .status(response.statusCode)
    .json({
      responseData: response.responseBody,
      requestData: response.requestData,
    });
});
router.get("/customers-business/financial-relations", async (req, res) => {
  const response = await fetchData(
    req,
    "customers-business",
    "financial-relations"
  );

  return res
    .status(response.statusCode)
    .json({
      responseData: response.responseBody,
      requestData: response.requestData,
    });
});

router.get("/customers-business/qualifications", async (req, res) => {
  const response = await fetchData(req, "customers-business", "qualifications");

  return res
    .status(response.statusCode)
    .json({
      responseData: response.responseBody,
      requestData: response.requestData,
    });
});

module.exports = router;
