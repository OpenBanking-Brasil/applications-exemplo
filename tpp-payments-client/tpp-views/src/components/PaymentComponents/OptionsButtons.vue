<template>
<v-container>
      <v-row  >
         <v-col align="center"
          
        >
    <v-btn
      depressed
      color="white--text green lighten-1"
      x-large
      @click="$router.push('payment-detail')"
    ><v-icon left> mdi-file </v-icon>
      Create Payment
    </v-btn>
         </v-col>
         <v-col align="center"
          
        >
    <v-btn
      depressed
      color="primary"
      x-large
      @click="$router.push('payment-menu')"
    ><v-icon left> mdi-refresh </v-icon>
      Check Status
    </v-btn>
         </v-col>
         <v-col align="center"
         
        >
    <v-btn
      color="white--text light-blue darken-4"
      x-large
      @click="$router.push('patch-detail')">
    <v-icon left> mdi-cancel </v-icon>
      Revoke Payment
    </v-btn>
         </v-col>
      </v-row>
    </v-container>
</template>

<script>
  export default {
    name: 'OptionsButtons',

    data: () => ({
    }),
  }
</script>