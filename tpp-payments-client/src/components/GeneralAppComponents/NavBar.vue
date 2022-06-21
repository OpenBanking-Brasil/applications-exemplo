<template>
  <v-app-bar app color="secondary" dark>

    <router-link to="/" style="text-decoration: none">
      <v-btn text>
        <span class="mr-2">Home</span>
        <v-icon>mdi-home</v-icon>
      </v-btn>
    </router-link>

    <router-link to="/banks" style="text-decoration: none">
      <v-btn text>
        <span class="mr-2">Banks</span>
        <v-icon>mdi-domain</v-icon>
      </v-btn>
    </router-link>

    <router-link to="/payment-menu" style="text-decoration: none">
      <v-btn text>
        <span class="mr-2">Payments</span>
        <v-icon>mdi-cash</v-icon>
      </v-btn>
    </router-link>

    <v-divider inset vertical class="mr-5 ml-5"></v-divider>

    <v-btn
      href="https://github.com/OpenBanking-Brasil/applications-exemplo"
      target="_blank"
      text
    >
      <span class="mr-2">Mock TPP Github</span>
      <v-icon>mdi-github</v-icon>
    </v-btn>
    <v-btn
      href="https://gitlab.com/obb1/certification/-/wikis/Overview-of-the-Mock-TPP"
      target="_blank"
      text
    >
      <span class="mr-2">Documentation</span>
      <v-icon>mdi-folder-open</v-icon>
    </v-btn>
  </v-app-bar>
</template>

<script>
export default {
  name: "NavBar",

  data: () => ({}),
};
</script>
