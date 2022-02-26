<template>
  <v-main class="payment-details">

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
          <SheetAppBar header="Payment Details"/>
            <v-sheet
              min-height="70vh"
              rounded="lg"
            >
            <v-container class="pa-md-12">
              <v-card
                elevation="2"
                outlined
                color=""
                >
                <v-card-title style="color: white; background-color:#004D40">Debtor</v-card-title>
                <v-card-text>
                <v-row class="pa-6">
                <v-col
                  cols="12"
                  sm="4"
                  md="4"
                >
              <b>Account Number</b>
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
              <b>Account Type</b>
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
              <b>ISPB</b>
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
              <b>Issuer</b>
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
                <v-card-title style="color: white; background-color:#004C50">Creditor</v-card-title>
                <v-card-text>
                <v-row class="pa-6">
                <v-col
                  cols="12"
                  sm="4"
                  md="4"
                >
              <b>Name</b>
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
              <b>CPF/CNPJ</b>
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
              <b>Person Type</b>
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
                <v-card-title style="color: white; background-color:#004C50">Payment</v-card-title>
                <v-card-text class="pa-6">
                <v-row class="pl-6">
                <v-col
                  cols="12"
                  sm="4"
                  md="4"
                >
              <b>Amount</b>
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
              <b>Payment Scheduled</b>
                <v-select v-model="selected"
                  :items="['Yes','No']"
                  label=""
                  outlined
        ></v-select>
              </v-col>
              <v-col v-if="selected == 'Yes'"
                cols="12"
                sm="2"
                md="2"
                
              >
              <b>Payment Date</b>
                <v-text-field
                  placeholder="123456"
                    outlined
                    filled
                ></v-text-field>
              </v-col>
                </v-row>
                <v-row class="pl-6">
              <v-col
                cols="12"
                sm="6"
                md="6"
                
              >
              <b>Type</b>
                <v-text-field
                  placeholder="123456"
                    outlined
                    filled
                ></v-text-field>
              </v-col>
                </v-row>
                <v-row class="pl-6">
              <v-col
                cols="12"
                sm="4"
                md="4"
                
              >
              <b>Proxy</b>
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
              <b>Local Instrument</b>
                <v-text-field
                  placeholder="123456"
                    outlined
                    filled
                ></v-text-field>
              </v-col>
              </v-row>
                <v-row class="pl-6">
              <v-col
                cols="12"
                sm="4"
                md="4"
                
              >
              <b>Credit Account Number</b>
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
              <b>Credit Account Type</b>
                <v-text-field
                  placeholder="123456"
                    outlined
                    filled
                ></v-text-field>
              </v-col>
              </v-row>
                <v-row class="pl-6">
              <v-col
                cols="12"
                sm="4"
                md="4"
                
              >
              <b>Credit Account ISPB</b>
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
              <b>Credit Account Issuer</b>
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
      Create Payment
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
  name: 'PaymentDetail',
  components: {
    SheetAppBar
  },
  data: () => ({
      selected: undefined,
    }),
}
</script>
