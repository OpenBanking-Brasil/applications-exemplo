<template>
  <v-app :class="$vuetify.breakpoint.lgAndUp ? 'app-lg' : ''">
    <div class="app-wrapper">
      <nav-bar />

      <div class="app-content-layer d-flex flex-column">
        <app-bar />

        <v-main class="app-main">
          <router-view></router-view>
        </v-main>
        
        <FooterBar class="app-footer" />
      </div>

      <SnackBar />
      <LoadingOverlay />
    </div>
  </v-app>
</template>

<script>
import AppBar from "@/components/GeneralAppComponents/AppBar.vue";
import NavBar from "@/components/GeneralAppComponents/NavBar.vue";
import FooterBar from "@/components/GeneralAppComponents/FooterBar.vue";
import SnackBar from "@/components/GeneralAppComponents/SnackBar.vue";
import LoadingOverlay from "@/components/GeneralAppComponents/LoadingOverlay.vue";

export default {
  name: "App",
  components: {
    AppBar,
    NavBar,
    FooterBar,
    SnackBar,
    LoadingOverlay,
  },
};
</script>

<style lang="scss">
 @import "@/styles/index";
</style>
