<template>
    <v-navigation-drawer
      floating
      permanent
      width="180"
      class="app-nav"
    >
      <template v-slot:prepend>
        <div class="logo-holder">
          <img :src="logo" alt="logo" />
        </div>
      </template>

      <v-list dense>
        <v-list-item
          v-for="item in navData"
          :key="item.text"
          :to="item.to"
          :href="item.href"
          :target="item.target"
          link
          class="app-nav__item"
        >
          <v-list-item-content>
            <v-list-item-title>{{ item.text }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>
</template>

<script>
import logo from "@/assets/MockTPP.svg";

export default {
  name: "NavBar",

  data() {
    return {
      logo,
      items: [
          { title: "Home", icon: "mdi-view-dashboard" },
          { title: "About", icon: "mdi-forum" },
        ],

      navData: [
        {
          to: "/",
          text: " Go Home",
          icon: "mdi-home",
        },
        {
          to: "/banks",
          text: "Banks",
          icon: "mdi-domain",
        },
        {
          to: "/payment-menu",
          text: "Payments",
          icon: "mdi-cash",
        },
        {
          href: "https://github.com/OpenBanking-Brasil/applications-exemplo",
          target: "_blank",
          text: "Mock TPP Github",
          icon: "mdi-github",
        },
        {
          href: "https://gitlab.com/obb1/certification/-/wikis/Overview-of-the-Mock-TPP",
          target: "_blank",
          text: "Documentation",
          icon: "mdi-folder-open",
        },
      ]
    };
  },
};
</script>
