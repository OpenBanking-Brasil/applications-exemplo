const express = require("express");
const { fetchData, getPathWithParams } = require("../utils/helpers.js");

const router = express.Router();

const creditCardAccountAPIFamily = "credit-cards-accounts";
router.get("/credit-cards-accounts", async (req, res) => {
  const path = getPathWithParams(req.query);
  const response = await fetchData(
    req,
    creditCardAccountAPIFamily,
    "credit cards accounts",
    path
  );

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

router.get("/credit-cards-accounts/:creditCardAccountId", async (req, res) => {
  const creditCardAccountId = req.params.creditCardAccountId;
  const response = await fetchData(
    req,
    creditCardAccountAPIFamily,
    "credit card account",
    `/${creditCardAccountId}`
  );

  return res.status(response.statusCode).json({
    responseData: response.responseBody,
    requestData: response.requestData,
  });
});

router.get(
  "/credit-cards-accounts/:creditCardAccountId/limits",
  async (req, res) => {
    const creditCardAccountId = req.params.creditCardAccountId;
    const path = `/${creditCardAccountId}/limits`;
    const response = await fetchData(
      req,
      creditCardAccountAPIFamily,
      "credit cards accounts limits",
      path
    );

    return res.status(response.statusCode).json({
      responseData: response.responseBody,
      requestData: response.requestData,
    });
  }
);

router.get(
  "/credit-cards-accounts/:creditCardAccountId/transactions",
  async (req, res) => {
    const creditCardAccountId = req.params.creditCardAccountId;
    const queryParams = getPathWithParams(req.query);
    const path = `/${creditCardAccountId}/transactions${queryParams}`;
    const response = await fetchData(
      req,
      creditCardAccountAPIFamily,
      "credit cards accounts transactions",
      path
    );

    return res.status(response.statusCode).json({
      responseData: response.responseBody,
      requestData: response.requestData,
    });
  }
);

router.get(
  "/credit-cards-accounts/:creditCardAccountId/bills",
  async (req, res) => {
    const creditCardAccountId = req.params.creditCardAccountId;
    const queryParams = getPathWithParams(req.query);
    const path = `/${creditCardAccountId}/bills${queryParams}`;
    const response = await fetchData(
      req,
      creditCardAccountAPIFamily,
      "credit cards accounts bills",
      path
    );

    return res
      .status(response.statusCode)
      .json({
        responseData: response.responseBody,
        requestData: response.requestData,
      });
  }
);

router.get(
  "/credit-cards-accounts/:creditCardAccountId/bills/:billId/transactions",
  async (req, res) => {
    const creditCardAccountId = req.params.creditCardAccountId;
    const billId = req.params.billId;
    const queryParams = getPathWithParams(req.query);
    const path = `/${creditCardAccountId}/bills/${billId}/transactions${queryParams}`;
    const response = await fetchData(
      req,
      creditCardAccountAPIFamily,
      "credit cards accounts transactions",
      path
    );

    return res
      .status(response.statusCode)
      .json({
        responseData: response.responseBody,
        requestData: response.requestData,
      });
  }
);

module.exports = router;
