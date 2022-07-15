const express = require("express");
const { fetchData, getPathWithParams } = require("../utils/helpers.js");

const router = express.Router();

router.get("/customers-business/identifications", async (req, res) => {
  const queryParams = getPathWithParams(req.query);
  const path = queryParams;
  const response = await fetchData(
    req,
    "customers-business",
    "identifications",
    path,
  );

  return res
    .status(response.statusCode)
    .json({
      responseData: response.responseBody,
      requestData: response.requestData,
    });
});
router.get("/customers-business/financial-relations", async (req, res) => {
  const queryParams = getPathWithParams(req.query);
  const path = queryParams;
  const response = await fetchData(
    req,
    "customers-business",
    "financial-relations",
    path
  );

  return res
    .status(response.statusCode)
    .json({
      responseData: response.responseBody,
      requestData: response.requestData,
    });
});

router.get("/customers-business/qualifications", async (req, res) => {
  const queryParams = getPathWithParams(req.query);
  const path = queryParams;
  const response = await fetchData(req, "customers-business", "qualifications");

  return res
    .status(response.statusCode)
    .json({
      responseData: response.responseBody,
      requestData: response.requestData,
      path
    });
});

module.exports = router;
