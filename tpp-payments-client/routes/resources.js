const express = require("express");
const { fetchData, getPathWithParams } = require("../utils/helpers.js");

const router = express.Router();

router.get("/resources", async (req, res) => {
    const path = getPathWithParams(req.query);

    const response = await fetchData(req, "resources", "resources", path);

    return res.status(response.statusCode).json({responseData: response.responseBody, requestData: response.requestData});
  });

  module.exports = router;
  