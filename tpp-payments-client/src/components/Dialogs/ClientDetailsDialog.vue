<template>
  <v-dialog
    transition="dialog-bottom-transition"
    max-width="850"
    v-model="dialog"
    content-class="dialog-content"
  >
    <template v-slot:default="dialog">
      <v-card>
        <v-toolbar class="status-toolbar" height="46" dark>
          <span>Client Details</span>

          <v-btn
            icon
            height="36"
            width="36"
            class="mr-0"
            @click="dialog.value = false"
          >
            <v-icon small>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="container">
          <div>
            <v-row>
              <v-col>
                <v-alert dense text type="success" class="mt-5">
                  {{ messageText }}
                </v-alert>

                <v-card class="mt-5">
                  <v-list dense>
                    <v-list-item>
                      <v-list-item-content
                        >Client ID:</v-list-item-content
                      >
                      
                      <v-list-item-content class="align-end">
                        {{ clientID }}
                      </v-list-item-content>
                    </v-list-item>

                    <v-list-item>
                      <v-list-item-content
                        >Registration Access
                        Token:</v-list-item-content
                      >

                      <v-list-item-content class="align-end">
                        {{ registrationAccessToken }}
                      </v-list-item-content>
                    </v-list-item>

                    <v-list-item>
                      <v-list-item-content
                        >Granted Scopes:</v-list-item-content
                      >

                      <v-list-item-content class="align-end">
                        {{ scopes }}
                      </v-list-item-content>
                    </v-list-item>
                  </v-list>
                </v-card>
              </v-col>
            </v-row>
          </div>
        </v-card-text>

        <v-card-actions>
          <v-row class="layout-wrapper__bottom-btns">
            <v-col class="pa-0">
              <v-btn
                depressed
                text
                block
                height="57"
                class="rounded-0"
                @click="dialog.value = false"
              >OK</v-btn>
            </v-col>
          </v-row>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  name: "ClientDetailsDialog",

  data() {
    return {
      dialog: true,
    };
  },

  props: {
    messageText: {
      type: String,
      required: true,
    },
  },

  computed: {
    ...mapGetters(["scopes", "clientID", "registrationAccessToken"]),
  },
};
</script>