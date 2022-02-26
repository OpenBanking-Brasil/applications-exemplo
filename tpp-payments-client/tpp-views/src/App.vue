<template>
  <v-app>
    <NavBar/>
    <v-main class="grey lighten-3">
      <v-row class="pa-md-4">
      </v-row>
    <router-view></router-view>
    </v-main>
    <v-footer app>
    <FooterBar/>
  </v-footer>
  </v-app>
</template>
<script>

import NavBar from './components/GeneralAppComponents/NavBar';
import FooterBar from './components/GeneralAppComponents/FooterBar';

export default {
  name: 'App',
  components: {

    NavBar,
    FooterBar,

  },
  data: () => ({
    //
  }),
};
</script>