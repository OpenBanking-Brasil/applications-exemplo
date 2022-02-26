<template>
  <v-main class="home">
    <v-row>
      <v-col>
      </v-col>
      <v-col
      :cols="7">

            <v-sheet
              min-height="70vh"
              elevation="20"
              rounded="lg"
            >
            <v-app-bar class="blue darken-4 rounded-lg rounded-b-0">
              <v-spacer/>
              <div class="white--text transition-swing text-h3"
            >Home
            </div>
              <v-spacer/>
            </v-app-bar>

            </v-sheet>
      </v-col>
      <v-col>
      </v-col>
      </v-row>
  </v-main>

</template>

<script>
// @ is an alias to /src


export default {
  name: 'HomeView',
  components: {
    
  }
}
</script>
