<template>
    
<div class="pa-md-12" align="center">
    <v-btn
      depressed
      color="primary"
      x-large
      @click="$router.push('payment-menu')"
    >
      Select
    </v-btn>
    </div>


    
</template>


<script>
  export default {
    name: 'SelectButton',
   

    data: () => ({
    }),
  }
</script>
