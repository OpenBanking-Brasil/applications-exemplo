<template>
 <v-main class="patch">
        <v-row>
          <v-col
            cols="12"
            sm="2"
          >
          </v-col>
          <v-col
            cols="12"
            sm="8"
          >
          <SheetAppBar header="Patch Details"/>
            <v-sheet
              min-height="70vh"
              rounded="lg"
            >
            <v-container class="pa-md-12">
            
<div class="pa-2"></div>
  <v-card
                elevation="2"
                outlined
                color=""
                >
                <v-card-title style="color: white; background-color:#004C50">Logged User</v-card-title>
                <v-card-text>
                <v-row class="pa-6">
                <v-col
                  cols="12"
                  sm="4"
                  md="4"
                >
              <b>Document Identification</b>
                <v-text-field class="text-green"
                  placeholder="123456"
                    outlined
                    filled
                    
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="4"
                md="4"
                
              >
              <b>Document Rel</b>
                <v-text-field
                  placeholder="123456"
                    outlined
                    filled
                ></v-text-field>
              </v-col>
              
</v-row>
</v-card-text>
</v-card>
<div class="pa-2"></div>
  <v-card
                elevation="2"
                outlined
                color=""
                >
                <v-card-title style="color: white; background-color:#004C50">Information</v-card-title>
                <v-card-text>
                <v-row class="pa-6">
                <v-col
                  cols="12"
                  sm="4"
                  md="4"
                >
              <b>Revoked By</b>
                <v-text-field class="text-green"
                  placeholder="123456"
                    outlined
                    filled
                    
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="4"
                md="4"
                
              >
              <b>Code</b>
                <v-text-field
                  placeholder="123456"
                    outlined
                    filled
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="4"
                md="4"
                
              >
              <b>Additional Information</b>
                <v-text-field
                  placeholder="123456"
                    outlined
                    filled
                ></v-text-field>
              </v-col>
              
</v-row>
</v-card-text>
</v-card>
        <v-container class="pt-16">
         <v-col align="center">
    <v-btn
      depressed
      color="primary"
      x-large
      @click="$router.push('payment-menu')"
    ><v-icon left> mdi-file </v-icon>
      Confirm Patch
    </v-btn>
         </v-col>
    </v-container>
    </v-container>
            </v-sheet>
          </v-col>

          <v-col
            cols="12"
            sm="2"
          >
            
            
          </v-col>
        </v-row>
</v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from '@/components/GeneralAppComponents/SheetAppBar.vue'

export default {
  name: 'PatchView',
  components: {
    SheetAppBar
  }
}
</script>
