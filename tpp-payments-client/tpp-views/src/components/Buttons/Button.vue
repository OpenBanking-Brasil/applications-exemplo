<template>
    
<div class="pa-md-12" align="center">
    <v-btn
      depressed
      :color="colour"
      x-large
      @click="clickAction"
    >
    <v-icon left v-if="hasIcon"> {{ icon }} </v-icon>
     {{ text }}
    </v-btn>
    </div>

</template>


<script>
  export default {
    props : {
      colour: {
        type: String
      },
      text: {
        type: String
      },
      hasIcon: {
        type: Boolean
      },
      icon: {
        type: String
      },
      func: {
        type: Function
      },
    },
    name: 'ButtonComponent',
   

    data: () => ({
    }),
    methods: {
      clickAction(){
        this.func();
        //   this.$router.push(this.route);

      }
    }
  }
</script>
