<template>
  <v-app-bar app color="secondary" dark>
    <v-btn target="_blank" text>
      <span class="mr-2"><router-link to="/">Home</router-link></span>
      <v-icon>mdi-open-in-new</v-icon>
    </v-btn>
    <v-btn target="_blank" text>
      <span class="mr-2"><router-link to="/banks">Banks</router-link></span>
      <v-icon>mdi-open-in-new</v-icon>
    </v-btn>
    <v-btn target="_blank" text>
      <span class="mr-2"
        ><router-link to="/payment-menu">Payments</router-link></span
      >
      <v-icon>mdi-open-in-new</v-icon>
    </v-btn>

    <v-btn
      href="https://github.com/OpenBanking-Brasil/applications-exemplo"
      target="_blank"
      text
    >
      <span class="mr-2">Mock TPP Github</span>
      <v-icon>mdi-open-in-new</v-icon>
    </v-btn>
    <v-btn
      href="https://gitlab.com/obb1/certification/-/wikis/Overview-of-the-Mock-TPP"
      target="_blank"
      text
    >
      <span class="mr-2">Documentation</span>
      <v-icon>mdi-open-in-new</v-icon>
    </v-btn>
  </v-app-bar>
</template>

<script>
export default {
  name: "NavBar",

  data: () => ({}),
};
</script>
