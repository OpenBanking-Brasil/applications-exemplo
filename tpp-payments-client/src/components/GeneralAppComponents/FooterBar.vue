<template>
  <v-footer padless inset>
    <v-col class="d-flex justify-end pr-0">
      <a :href="hrefFooter" target="_blank">{{ hrefText }}</a>
    </v-col>
  </v-footer>
</template>

<script>
export default {
  name: "FooterBar",

  data() {
    return {
      hrefFooter: "https://www.raidiam.com",
      hrefText: "Powered by Raidiam",
    };
  },
};
</script>