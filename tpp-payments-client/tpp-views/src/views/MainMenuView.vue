<template>
<v-main class="main">
<v-row>
      <v-col>
      </v-col>
      <v-col
      :cols="7">
            <v-sheet
              min-height="70vh"
              elevation="20"
              rounded="lg"
              class="grey lighten-3"
            >
            <SheetAppBar header="Mock TPP"/>
             <v-container class="pa-md-12">
      <v-row  >
        <v-col
          cols="12"
          sm="6"
          md="3"
        >
        <b>Client ID</b>
          <v-text-field
            placeholder="123456"
              outlined
              filled
          ></v-text-field>
        </v-col>
        <v-col>
        </v-col>
        <v-col
          cols="12"
          sm="6"
          md="3"
        >
        <b>Refresh Token</b>
          <v-text-field
            placeholder="123456"
              outlined
              filled
          ></v-text-field>
        </v-col>
      </v-row>
        <v-row>
        <v-col
          cols="12"
          sm="6"
          md="3"
        > <b>Consent ID</b>
          <v-text-field
             placeholder="123456"
              outlined
              filled
          ></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col
          cols="12"
          sm="6"
          md="3"
        > <b>Payment ID</b>
          <v-text-field
            placeholder="123456"
              outlined
              filled
          ></v-text-field>
        </v-col>
      </v-row>
    </v-container>
    <OptionsButtons/>
            </v-sheet>
      </v-col>
      <v-col>
      </v-col>
      </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src
import OptionsButtons from '@/components/PaymentComponents/OptionsButtons.vue'
import SheetAppBar from '@/components/GeneralAppComponents/SheetAppBar.vue'

export default {
  name: 'MainMenuView',
  components: {
    OptionsButtons,
    SheetAppBar
  }
}
</script>
