<template>
  <v-main class="home">
    <v-row>
      <v-col> </v-col>
      <v-col :cols="7">
        <v-sheet min-height="vh" elevation="20" rounded="lg">
          <v-app-bar class="blue darken-4 rounded-lg rounded-b-0">
            <v-spacer />
            <div class="white--text transition-swing text-h3">Home</div>
            <v-spacer />
          </v-app-bar>
          <v-row class="pt-12"> </v-row>
          <v-row class="pt-12">
            <v-col cols="12" sm="6" md="6">
              <v-card class="mx-auto" max-width="344">
                <v-list-item three-line>
                  <v-list-item-content>
                    <div class="text-overline mb-4">Phase 2</div>
                    <v-list-item-title class="text-h5 mb-1">
                      Customer Data
                    </v-list-item-title>
                    <v-list-item-subtitle
                      >This is still a work-in-progress</v-list-item-subtitle
                    >
                  </v-list-item-content>

                  <v-list-item-avatar tile size="40"
                    ><v-icon large color="grey darken-2">
                      mdi-swap-horizontal
                    </v-icon>
                  </v-list-item-avatar>
                </v-list-item>
                <v-card-actions>
                  <v-btn
                    outlined
                    rounded
                    text
                    @click="optionChoice('customer-data')"
                  >
                    Go
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-col>

            <v-col cols="12" sm="6" md="6">
              <v-card class="mx-auto" max-width="344">
                <v-list-item three-line>
                  <v-list-item-content>
                    <div class="text-overline mb-4">Phase 3</div>
                    <v-list-item-title class="text-h5 mb-1">
                      Payments
                    </v-list-item-title>
                    <v-list-item-subtitle
                      >Use the Mock TPP for Phase 3
                      Payments</v-list-item-subtitle
                    >
                  </v-list-item-content>

                  <v-list-item-avatar tile size="40"
                    ><v-icon large color="green darken-2">
                      mdi-cash-fast
                    </v-icon>
                  </v-list-item-avatar>
                </v-list-item>

                <v-card-actions>
                  <v-btn
                    outlined
                    rounded
                    text
                    @click="optionChoice('payments')"
                  >
                    Go
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-col>
          </v-row>
          <v-row class="pt-12">
            <v-col cols="12" sm="1" md="1"></v-col>
            <v-col cols="12" sm="11" md="11">
              <v-dialog v-model="dialog" persistent max-width="1000px">
                <template v-slot:activator="{ on, attrs }">
                  <v-btn
                    color="primary"
                    dark
                    v-bind="attrs"
                    v-on="on"
                    class="mb-5"
                  >
                    <v-icon left> mdi-cog </v-icon>
                    Configurations
                  </v-btn>
                </template>
                <v-form>
                  <v-card>
                    <v-card-title>
                      <span class="text-h5"> Configuration Settings</span>
                    </v-card-title>

                    <v-card-text>
                      <v-container>
                        <v-row>
                          <v-col cols="12" sm="5" md="5">
                            <v-divider />
                          </v-col>
                          <v-col
                            cols="12"
                            sm="2"
                            md="2"
                            class="text-center mt-n3"
                          >
                            <h2>Client Details</h2>
                          </v-col>
                          <v-col cols="12" sm="5" md="5">
                            <v-divider />
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Application Type </b>
                            <v-text-field
                              placeholder="web"
                              outlined
                              dense
                              name="application_type"
                              id="application_type"
                              v-model="theFormData.application_type"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Id Token Signed Response ALG </b>
                            <v-icon
                              small
                              title="JWS (JSON Web Signature) algorithm required for signing the ID Token issued to this client."
                            >
                              mdi-information
                            </v-icon>
                            <v-text-field
                              placeholder="PS256"
                              outlined
                              dense
                              name="id_token_signed_response_alg"
                              id="id_token_signed_response_alg"
                              v-model="theFormData.id_token_signed_response_alg"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Require Auth Time </b>
                            <v-icon
                              small
                              title="Boolean value specifying whether the auth_time Claim in the ID Token is required."
                            >
                              mdi-information
                            </v-icon>
                            <v-select
                              v-model="theFormData.require_auth_time"
                              :items="[true, false]"
                              outlined
                              dense
                              name="require_auth_time"
                              id="require_auth_time"
                            ></v-select>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Subject Type </b>
                            <v-icon
                              small
                              title="subject_type requested for responses to this client."
                            >
                              mdi-information
                            </v-icon>
                            <v-text-field
                              placeholder="public"
                              outlined
                              dense
                              name="subject_type"
                              id="subject_type"
                              v-model="theFormData.subject_type"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Token Endpoint Auth Method </b>
                            <v-icon
                              small
                              title="Client authentication method to the token endpoint supported by the authorization server."
                            >
                              mdi-information
                            </v-icon>
                            <v-text-field
                              placeholder="tls_client_auth"
                              outlined
                              dense
                              name="token_endpoint_auth_method"
                              id="token_endpoint_auth_method"
                              v-model="theFormData.token_endpoint_auth_method"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Request Object Signing ALG </b>
                            <v-icon
                              small
                              title="The type of JSON Web Key Set (JWKS) algorithm that must be used for signing request objects."
                            >
                              mdi-information
                            </v-icon>
                            <v-text-field
                              placeholder="PS256"
                              outlined
                              dense
                              name="request_object_signing_alg"
                              id="request_object_signing_alg"
                              v-model="theFormData.request_object_signing_alg"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Require Signed Request Object </b>
                            <v-icon
                              small
                              title="If set to true, it tells the Authorization Server, that it should reject authorization requests for this client if the request objects are not signed"
                            >
                              mdi-information
                            </v-icon>
                            <v-select
                              v-model="
                                theFormData.require_signed_request_object
                              "
                              :items="[true, false]"
                              outlined
                              dense
                              name="require_signed_request_object"
                              id="require_signed_request_object"
                            ></v-select>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Require Pushed Authorization Requests </b>
                            <v-icon
                              small
                              title="Boolean parameter indicating whether the authorization server accepts authorization request data only via PAR."
                            >
                              mdi-information
                            </v-icon>
                            <v-select
                              v-model="
                                theFormData.require_pushed_authorization_requests
                              "
                              :items="[true, false]"
                              outlined
                              dense
                              name="require_pushed_authorization_requests"
                              id="require_pushed_authorization_requests"
                            ></v-select>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> TLS Client Certificate Bound Access Tokens </b>
                            <v-icon
                              small
                              title="Boolean value used to indicate the client's intention to use mutual-TLS client certificate-bound access tokens."
                            >
                              mdi-information
                            </v-icon>
                            <v-select
                              v-model="
                                theFormData.tls_client_certificate_bound_access_tokens
                              "
                              :items="[true, false]"
                              outlined
                              dense
                              name="tls_client_certificate_bound_access_tokens"
                              id="tls_client_certificate_bound_access_tokens"
                            ></v-select>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Client ID </b>
                            <v-text-field
                              placeholder=""
                              outlined
                              dense
                              name="client_id"
                              id="client_id"
                              v-model="theFormData.client_id"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="8" md="8">
                            <b> JWKS URI </b>
                            <v-icon
                              small
                              title="URL for the client's JSON Web Key Set (JWKS) document. If the client signs requests to the server, it contains the signing key(s) the server uses to validate signatures from the Client."
                            >
                              mdi-information
                            </v-icon>
                            <v-text-field
                              placeholder=""
                              outlined
                              dense
                              name="jwks_uri"
                              id="jwks_uri"
                              v-model="theFormData.jwks_uri"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="8" md="8">
                            <b> TLS Client Auth Subject DN </b>
                            <v-icon
                              small
                              title="This value must be set if token_endpoint_auth_method is set to tls_client_auth. The tls_client_auth_subject_dn claim MUST contain the DN of the certificate that the TPP will present to the ASPSP token endpoint."
                            >
                              mdi-information
                            </v-icon>
                            <v-text-field
                              placeholder=""
                              outlined
                              dense
                              name="tls_client_auth_subject_dn"
                              id="tls_client_auth_subject_dn"
                              v-model="theFormData.tls_client_auth_subject_dn"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Authorization Signed Response ALG </b>
                            <v-icon
                              small
                              title="JWS algorithm used for signing authorization responses."
                            >
                              mdi-information
                            </v-icon>
                            <v-text-field
                              placeholder=""
                              outlined
                              dense
                              name="authorization_signed_response_alg"
                              id="authorization_signed_response_alg"
                              v-model="
                                theFormData.authorization_signed_response_alg
                              "
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="5" md="5">
                            <v-divider />
                          </v-col>
                          <v-col
                            cols="12"
                            sm="2"
                            md="2"
                            class="text-center mt-n3"
                          >
                            <h2>App Details</h2>
                          </v-col>
                          <v-col cols="12" sm="5" md="5">
                            <v-divider />
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Signing Key ID </b>
                            <v-icon
                              small
                              title="Signing key ID used for signing JWT"
                            >
                              mdi-information
                            </v-icon>
                            <v-text-field
                              placeholder=""
                              outlined
                              dense
                              name="signing_kid"
                              id="signing_kid"
                              v-model="theFormData.signing_kid"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Software Statement ID </b>
                            <v-text-field
                              placeholder=""
                              outlined
                              dense
                              name="software_statement_id"
                              id="software_statement_id"
                              v-model="theFormData.software_statement_id"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Organisation ID </b>
                            <v-text-field
                              placeholder=""
                              outlined
                              dense
                              name="organisation_id"
                              id="organisation_id"
                              v-model="theFormData.organisation_id"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="4" md="4">
                            <b> Loop Pause Time (ms) </b>
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

                          <v-col cols="12" sm="4" md="4">
                            <b> Number of Check Loops </b>
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

                          <v-col cols="12" sm="4" md="4">
                            <b> Preferred Token Auth Mechanism </b>
                            <v-text-field
                              placeholder=""
                              outlined
                              dense
                              name="preferred_token_auth_mech"
                              id="preferred_token_auth_mech"
                              v-model="theFormData.preferred_token_auth_mech"
                            ></v-text-field>
                          </v-col>

                          <v-col cols="12" sm="8" md="8">
                            <b>
                              Upload Certificates - Certificate authority
                              (ca.pem), Signing (signing.pem/signing.key) and
                              Transport (transport.pem/transport.key)
                            </b>
                            <v-icon
                              small
                              title="Upload your own certificates. If empty, will use the default ones from the Mock TPP."
                            >
                              mdi-information
                            </v-icon>
                            <v-file-input
                              v-model="files"
                              small-chips
                              show-size
                              multiple
                              clearable
                              outlined
                              dense
                              name="certificates"
                              id="certificates"
                              @change="onFileChange"
                              @click:clear="clearFilesInput"
                            >
                              <template v-slot:selection="{ text, index }">
                                <v-chip
                                  small
                                  text-color="#ffffff"
                                  color="primary"
                                  close
                                  @click:close="remove(index)"
                                >
                                  {{ text }}
                                </v-chip>
                              </template>
                            </v-file-input>
                          </v-col>



                        <v-col cols="12" sm="4" md="4">
                          <v-checkbox
                            class="mt-12"
                            v-model="theFormData.replace_existing_certs"
                            label="Replace Existing Certificates"
                            color="primary"
                            hide-details
                          ></v-checkbox>
                        </v-col>
                        <v-col cols="12" sm="4" md="4">
                          <v-checkbox
                            v-model="theFormData.accept_any_certificates"
                            label="Accept Any Certificate Presented"
                            color="primary"
                            hide-details
                          ></v-checkbox>
                        </v-col>


                        </v-row>
                      </v-container>
                    </v-card-text>
                    <v-card-actions>
                      <v-spacer></v-spacer>
                      <v-btn color="blue darken-1" text @click="dialog = false">
                        Close
                      </v-btn>
                      <v-btn
                        color="blue darken-1"
                        text
                        @click.prevent="submitConfigForm"
                        type="submit"
                      >
                        Save
                      </v-btn>
                    </v-card-actions>
                  </v-card>
                </v-form>
              </v-dialog>
            </v-col>
          </v-row>
        </v-sheet>
      </v-col>
      <v-col> </v-col>
    </v-row>
    <v-snackbar
      v-model="snackbar"
      :multi-line="multiLine"
      :color="statusColour"
    >
      {{ messageText }}

      <template v-slot:action="{ attrs }">
        <v-btn color="white" text v-bind="attrs" @click="snackbar = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </v-main>
</template>

<script>
import { mapActions } from "vuex";
import axios from "../util/axios.js";
import { FormData } from "formdata-node";

export default {
  name: "HomeView",
  components: {},

  data: () => {
    return {
      dialog: false,
      files: [],
      currFiles: [],

      multiLine: true,
      snackbar: false,
      messageText: "",
      statusColour: "red accent-2",

      theFormData: {
        application_type: "web",
        id_token_signed_response_alg: "PS256",
        require_auth_time: false,
        subject_type: "public",
        token_endpoint_auth_method: "tls_client_auth",
        request_object_signing_alg: "PS256",
        require_signed_request_object: true,
        require_pushed_authorization_requests: false,
        tls_client_certificate_bound_access_tokens: true,
        client_id: "7nrwPCGZjkx03v4PqErZJ",
        jwks_uri:
          "https://keystore.sandbox.directory.openbankingbrasil.org.br/74e929d9-33b6-4d85-8ba7-c146c867a817/7218e1af-195f-42b5-a44b-8c7828470f5a/application.jwks",
        tls_client_auth_subject_dn:
          "CN=7218e1af-195f-42b5-a44b-8c7828470f5a,OU=74e929d9-33b6-4d85-8ba7-c146c867a817,O=Open Banking,C=BR",
        authorization_signed_response_alg: "PS256",
        signing_kid: "8o-O3VSFOPE8TrULXUTHxhxJcdADKIBmsfE0KWYkHik",
        software_statement_id: "7218e1af-195f-42b5-a44b-8c7828470f5a",
        organisation_id: "74e929d9-33b6-4d85-8ba7-c146c867a817",
        loop_pause_time: 2000,
        number_of_check_loops: 5,
        preferred_token_auth_mech: "private_key_jwt",
        replace_existing_certs: false,
        accept_any_certificates: false
      },
    };
  },

  methods: {
    ...mapActions(["setOption"]),

    remove(index) {
      this.files.splice(index, 1);
    },

    clearFilesInput() {
      this.currFiles = [];
      this.files = [];
    },

    onFileChange() {
      for (let file of this.files) {
        let fileAlreadyExist = false;
        for (let i = 0; i < this.currFiles.length; i++) {
          if (file.name === this.currFiles[i].name) {
            fileAlreadyExist = true;
            this.currFiles[i] = file;
            break;
          }
        }
        if (!fileAlreadyExist) {
          this.currFiles.push(file);
        }
      }

      this.files = structuredClone(this.currFiles);
    },

    submitConfigForm() {
      this.dialog = false;

      let formData = new FormData();
      for (let file of this.files) {
        formData.append("certificates", file);
      }

      for (let recordName in this.theFormData) {
        formData.append(recordName, this.theFormData[recordName]);
      }

      axios.defaults.withCredentials = true;
      axios
        .post("/change-config", formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        })
        .then((res) => {
          if (res.status === 201) {
            this.messageText = res.data.message;
            this.statusColour = "success";
            this.snackbar = true;
          }
          this.clearFilesInput();
        })
        .catch((error) => {
          console.log("cannot post", error);
          this.messageText = error.response.data;
          this.statusColour = "red accent-2";
          this.snackbar = true;
          this.clearFilesInput();
        });
    },

    optionChoice(option) {
      this.setOption(option);
      this.$router.push({
        name: "banks",
        query: { option: option },
      });
    },
  },

  created(){
    axios
      .get("/", { withCredentials: true })
      .then((response) => {
        console.log(response.status);
      });
  }
};
</script>
