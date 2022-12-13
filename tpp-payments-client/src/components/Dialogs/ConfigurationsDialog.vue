<template>
  <v-dialog
    v-model="dialog"
    persistent
    max-width="1000px"
    content-class="dialog-content configurations-dialog"
  >
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        color="#007199"
        height="33"
        width="33"
        depressed
        icon
        v-bind="attrs"
        v-on="on"
      >
        <v-icon> mdi-cog </v-icon>
      </v-btn>
    </template>

    <v-form>
      <v-card>
        <v-tabs v-model="tab" height="46px" color="#007199" grow>
          <v-tab v-for="item in items" :key="item">
            {{ item }}
          </v-tab>
        </v-tabs>

        <v-tabs-items v-model="tab">
          <v-tab-item v-for="item in items" :key="item">
            <template v-if="item === 'Authorization and Message Settings'">
              <v-row class="pa-5">
                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Application Type </span>
                  </div>

                  <v-text-field
                    placeholder="web"
                    outlined
                    dense
                    name="application_type"
                    id="application_type"
                    v-model="theFormData.application_type"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Id Token Signed Response ALG </span>

                    <v-icon
                      small
                      title="JWS (JSON Web Signature) algorithm required for signing the ID Token issued to this client."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder="PS256"
                    outlined
                    dense
                    name="id_token_signed_response_alg"
                    id="id_token_signed_response_alg"
                    v-model="theFormData.id_token_signed_response_alg"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Require Auth Time </span>

                    <v-icon
                      small
                      title="Boolean value specifying whether the auth_time Claim in the ID Token is required."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-select
                    v-model="theFormData.require_auth_time"
                    :items="[true, false]"
                    outlined
                    dense
                    name="require_auth_time"
                    id="require_auth_time"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Subject Type </span>

                    <v-icon
                      small
                      title="subject_type requested for responses to this client."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder="public"
                    outlined
                    dense
                    name="subject_type"
                    id="subject_type"
                    v-model="theFormData.subject_type"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Token Endpoint Auth Method </span>

                    <v-icon
                      small
                      title="Client authentication method to the token endpoint supported by the authorization server."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder="tls_client_auth"
                    outlined
                    dense
                    name="token_endpoint_auth_method"
                    id="token_endpoint_auth_method"
                    v-model="theFormData.token_endpoint_auth_method"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Request Object Signing ALG </span>

                    <v-icon
                      small
                      title="The type of JSON Web Key Set (JWKS) algorithm that must be used for signing request objects."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder="PS256"
                    outlined
                    dense
                    name="request_object_signing_alg"
                    id="request_object_signing_alg"
                    v-model="theFormData.request_object_signing_alg"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Require Signed Request Object </span>

                    <v-icon
                      small
                      title="If set to true, it tells the Authorization Server, that it should reject authorization requests for this client if the request objects are not signed"
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-select
                    v-model="theFormData.require_signed_request_object"
                    :items="[true, false]"
                    outlined
                    dense
                    name="require_signed_request_object"
                    id="require_signed_request_object"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Require Pushed Authorization Requests </span>

                    <v-icon
                      small
                      title="Boolean parameter indicating whether the authorization server accepts authorization request data only via PAR."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-select
                    v-model="theFormData.require_pushed_authorization_requests"
                    :items="[true, false]"
                    outlined
                    dense
                    name="require_pushed_authorization_requests"
                    id="require_pushed_authorization_requests"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> TLS Client Certificate Bound Access Tokens </span>

                    <v-icon
                      small
                      title="Boolean value used to indicate the client's intention to use mutual-TLS client certificate-bound access tokens."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-select
                    v-model="theFormData.tls_client_certificate_bound_access_tokens"
                    :items="[true, false]"
                    outlined
                    dense
                    name="tls_client_certificate_bound_access_tokens"
                    id="tls_client_certificate_bound_access_tokens"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Authorization Signed Response ALG </span>

                    <v-icon
                      small
                      title="JWS algorithm used for signing authorization responses."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder=""
                    outlined
                    dense
                    name="authorization_signed_response_alg"
                    id="authorization_signed_response_alg"
                    v-model="theFormData.authorization_signed_response_alg"
                  />
                </v-col>
              </v-row>
            </template>

            <template v-else-if="item === 'Software Statement Settings'">
              <v-row class="pa-5">
                <v-col cols="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Signing Key ID </span>

                    <v-icon
                      small
                      title="Signing key ID used for signing JWT"
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder=""
                    outlined
                    dense
                    name="signing_kid"
                    id="signing_kid"
                    v-model="theFormData.signing_kid"
                  />
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder">
                    <span> Software Statement ID </span>
                  </div>

                  <v-text-field
                    placeholder=""
                    outlined
                    dense
                    name="software_statement_id"
                    id="software_statement_id"
                    v-model="theFormData.software_statement_id"
                  ></v-text-field>
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder">
                    <span> Directory Client ID </span>
                  </div>

                  <v-text-field
                    placeholder=""
                    outlined
                    dense
                    name="client_id"
                    id="client_id"
                    v-model="theFormData.client_id"
                  ></v-text-field>
                </v-col>

                <v-col cols="6">
                  <div class="app-label-holder">
                    <span> Organisation ID </span>
                  </div>

                  <v-text-field
                    placeholder=""
                    outlined
                    dense
                    name="organisation_id"
                    id="organisation_id"
                    v-model="theFormData.organisation_id"
                  ></v-text-field>
                </v-col>

                <v-col cols="12">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> JWKS URI </span>

                    <v-icon
                      small
                      title="URL for the client's JSON Web Key Set (JWKS) document. If the client signs requests to the server, it contains the signing key(s) the server uses to validate signatures from the Client."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder=""
                    outlined
                    dense
                    name="jwks_uri"
                    id="jwks_uri"
                    v-model="theFormData.jwks_uri"
                  ></v-text-field>
                </v-col>

                <v-col cols="12">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> TLS Client Auth Subject DN </span>

                    <v-icon
                      small
                      title="This value must be set if token_endpoint_auth_method is set to tls_client_auth. The tls_client_auth_subject_dn claim MUST contain the DN of the certificate that the TPP will present to the ASPSP token endpoint."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder=""
                    outlined
                    dense
                    name="tls_client_auth_subject_dn"
                    id="tls_client_auth_subject_dn"
                    v-model="theFormData.tls_client_auth_subject_dn"
                  ></v-text-field>
                </v-col>

                <v-col cols="12">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Upload Certificates - Certificate authority
                    (ca.pem), Signing (signing.pem/signing.key)
                    and Transport (transport.pem/transport.key) </span>

                    <v-icon
                      small
                      title="Upload your own certificates. If empty, will use the default ones from the Mock TPP."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-file-input
                    v-model="localFiles"
                    small-chips
                    show-size
                    multiple
                    clearable
                    outlined
                    dense
                    prepend-icon=""
                    prepend-inner-icon="$file"
                    color="#007199"
                    name="certificates"
                    id="certificates"
                    @click:clear="$emit('clearFilesInput')"
                  >
                    <template
                      v-slot:selection="{ text, index }"
                    >
                      <v-chip
                        small
                        text-color="#ffffff"
                        color="primary"
                        close
                        @click:close="$emit('remove', index)"
                      >
                        {{ text }}
                      </v-chip>
                    </template>
                  </v-file-input>
                </v-col>

                <v-col cols="6" class="pt-0 pb-4">
                  <v-checkbox
                    v-model="theFormData.replace_existing_certs"
                    label="Replace Existing Certificates"
                    color="#007199"
                    on-icon="mdi-circle-slice-8"
                    off-icon="mdi-circle-outline"
                    hide-details
                  ></v-checkbox>
                </v-col>

                <v-col cols="6" class="pt-0 pb-4">
                  <v-checkbox
                    v-model="theFormData.accept_any_certificates"
                    label="Accept Any Certificate Presented"
                    color="#007199"
                    on-icon="mdi-circle-slice-8"
                    off-icon="mdi-circle-outline"
                    hide-details
                  ></v-checkbox>
                </v-col>
              </v-row>
            </template>

            <template v-else-if="item === 'Payments Polling Settings'">
              <v-row class="pa-5">
                <v-col cols="12" sm="6" md="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Loop Pause Time (ms) </span>

                    <v-icon
                      small
                      title="The total time in milliseconds that the application waits before polling again."
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder=""
                    outlined
                    dense
                    type="number"
                    name="loop_pause_time"
                    id="loop_pause_time"
                    v-model="theFormData.loop_pause_time"
                  ></v-text-field>
                </v-col>

                <v-col cols="12" sm="6" md="6">
                  <div class="app-label-holder d-flex justify-space-between">
                    <span> Number of Requests </span>

                    <v-icon
                      small
                      title="The number of times the application polls a payment"
                      color="rgba(57, 75, 101, 0.2)"
                    >mdi-help-circle</v-icon>
                  </div>

                  <v-text-field
                    placeholder=""
                    outlined
                    dense
                    type="number"
                    name="number_of_check_loops"
                    id="number_of_check_loops"
                    v-model="theFormData.number_of_check_loops"
                  ></v-text-field>
                </v-col>
              </v-row>
            </template>
          </v-tab-item>
        </v-tabs-items>

        <v-card-actions>
          <v-row class="layout-wrapper__bottom-btns">
            <v-col cols="6" class="pa-0">
              <v-btn
                depressed
                block 
                text
                height="57"
                @click="dialog = false"
              > Close </v-btn>
            </v-col>

            <v-col cols="6" class="pa-0">
              <v-btn
                depressed
                block 
                text
                height="57"
                type="submit"
                @click.prevent="submitConfig"
              > Save </v-btn>
            </v-col>
          </v-row>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>

<script>

export default {
  name: "ConfigurationsDialog",

  props: {
    theFormData: {
      type: Object,
      required: true,
    },
    value: {
      type: Array,
    },
  },

  data() {
    return {
      dialog: false,
      // files: [],
      tab: null,
      items: [
        "Authorization and Message Settings",
        "Software Statement Settings",
        "Payments Polling Settings",
      ],
    };
  },

  computed: {
    localFiles: {
      get: function () { return [...this.value]; },

      set: function (newValue) {
        this.$emit("input", newValue);
      }
    },
  },

  methods: {
    submitConfig() {
      this.$emit("submitConfigForm");
      this.dialog = false;
    },
  }
};
</script>