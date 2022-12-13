<template>
  <v-dialog
    transition="dialog-bottom-transition"
    content-class="dialog-content"
    max-width="530"
  >
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        v-bind="attrs"
        v-on="on"
        depressed
        block
        text
        height="57"
        @click="$emit('getPayment')"
      >
        <v-icon left>mdi-check</v-icon>
        <span>Check Status</span>
      </v-btn>
    </template>

    <template v-slot:default="dialog">
      <v-card elevation="0">
        <v-toolbar height="46" dark>
          <span>Payment Status</span>

          <v-btn
            icon
            height="36"
            width="36"
            class="mr-0"
            @click="dialog.value = false"
          >
            <v-icon small>mdi-close</v-icon>
          </v-btn>
        </v-toolbar >
        
        <v-card-text class="container">
          <v-row>
            <v-col>
              <v-card>
                <v-card-title class="mt-4 px-0">
                  {{ bankName }}
                </v-card-title>

                <v-list dense>
                  <template v-for="item in statusDetails">
                    <v-list-item
                      v-if="item.name === 'Scheduled Date:' ? paymentIsScheduled : true"
                      class="px-0"
                    >
                      <v-list-item-content>
                        {{ item.name }}
                      </v-list-item-content>

                      <v-list-item-content class="align-end">
                        {{ item.value }}
                      </v-list-item-content>
                    </v-list-item>
                  </template>
                </v-list>
              </v-card>
            </v-col>
          </v-row>
        </v-card-text>

        <v-card-actions>
          <v-progress-linear
            v-if="loading"
            top
            absolute
            indeterminate
            color="primary"
            class=""
          />

          <v-row class="layout-wrapper__bottom-btns">
            <v-col class="pa-0">
              <v-btn depressed block text height="57" @click="dialog.value = false">Close</v-btn>
            </v-col>
          </v-row>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>

export default {
  name: "PaymentStatusDialog",

  props: {
    loading: { type: Boolean },
    scheduledDate: { type: String },
    creationDateTime: { type: String },
    currency: { type: String },
    status: { type: String },
    paymentAmount: { type: Number },
    bankName: { type: String },
    paymentIsScheduled: { type: Boolean },
  },

  data() {
    return {
      dialog: false,
      statusDetails: [
        {
          name: "Amount:",
          value: this.paymentAmount,
        },
        {
          name: "Status:",
          value: this.status, 
        },
        {
          name: "Currency:",
          value: this.currency,
        },
        {
          name: "Creation Date and Time:",
          value: this.creationDateTime,
        },
        {
          name: "Scheduled Date:",
          value: this.scheduledDate,
        },
        
      ]
    };
  },
};
</script>