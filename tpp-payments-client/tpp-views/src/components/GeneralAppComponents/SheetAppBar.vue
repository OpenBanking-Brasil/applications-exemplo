<template>
    
<v-app-bar class="blue-grey darken-4 rounded-lg rounded-b-0">
              <v-spacer/>
              <div class="white--text transition-swing text-h3"
            >{{header}}
            </div>
              <v-spacer/>
            </v-app-bar>
    
</template>


<script>
  export default {
    name: 'SheetAppBar',
   props: ['header'],

  }
</script>
