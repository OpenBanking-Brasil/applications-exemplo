const express = require("express");
const { fetchData, getPathWithParams } = require("../utils/helpers.js");

const router = express.Router();

router.get("/customers-personal/identifications", async (req, res) => {
  const queryParams = getPathWithParams(req.query);
  const path = queryParams;
  const response = await fetchData(
    req,
    "customers-personal",
    "identifications",
    path
  );

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

router.get("/customers-personal/financial-relations", async (req, res) => {
  const queryParams = getPathWithParams(req.query);
  const path = queryParams;
  const response = await fetchData(
    req,
    "customers-personal",
    "financial-relations",
    path
  );

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

router.get("/customers-personal/qualifications", async (req, res) => {
  const queryParams = getPathWithParams(req.query);
  const path = queryParams;
  const response = await fetchData(req, "customers-personal", "qualifications", path);

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

module.exports = router;
