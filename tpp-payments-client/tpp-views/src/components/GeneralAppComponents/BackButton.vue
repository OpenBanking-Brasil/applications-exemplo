<template>

<v-container>
          <v-sheet
              min-height="8vh"
              max-width="8vh"
              rounded="lg"
               fill-height
            ><v-row justify="center"><v-col  align="center" >
              <v-icon x-large 
              @click="$router.push('payment-menu')"
              >mdi-arrow-left-circle</v-icon></v-col></v-row>
          </v-sheet>
            </v-container>
    
</template>

<script>
  export default {
    name: 'BackButton',

    data: () => ({
    }),
  }
</script>