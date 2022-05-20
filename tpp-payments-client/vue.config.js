const { defineConfig } = require("@vue/cli-service");
// module.exports = defineConfig({
//   transpileDependencies: ["vuetify"],
// });

module.exports = {
  devServer: {
    host: "tpp.localhost",
    port: 8080,
    https: true,
  },
  transpileDependencies: ["vuetify"],
};

// module.exports = {
//   configureWebpack: {
//     devServer: {
//       proxy: {
//         "/api": {
//           target: "https://tpp.localhost:443",
//         },
//       },
//     },
//   },
// };
