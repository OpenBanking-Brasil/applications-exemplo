<template>
  <v-app-bar
    class="app-bar"
  >
    <div class="d-flex align-center">
      <BackButton />
      <v-breadcrumbs :items="breadCrumbs">
        <template v-slot:item="{ item }">
          <v-breadcrumbs-item
            :to="item.to"
            :disabled="item.disabled"
            exact
          >
            {{ item.text }}
          </v-breadcrumbs-item>
        </template>
      </v-breadcrumbs>
    </div>

    <div>
      <configurations-dialog
        v-if="showConfigDialog"
        :theFormData="theFormData"
        v-model="files"
        @clearFilesInput="clearFilesInput"
        @remove="remove"
        @submitConfigForm="submitConfigForm"
      />

      <v-btn
        href="https://gitlab.com/obb1/certification/-/wikis/Overview-of-the-Mock-TPP"
        target="_blank"
        height="33"
        width="33"
        icon
        class="exit-btn ml-4"
      >
        <v-icon size="17">mdi-logout</v-icon>
      </v-btn>
    </div>
  </v-app-bar>
</template>

<script>
import { isEqual } from "lodash";
import { mapActions } from "vuex";
import axios from "@/util/axios.js";
import { FormData } from "formdata-node";

import ConfigurationsDialog from "@/components/Dialogs/ConfigurationsDialog";
import BackButton from "@/components/Shared/BackButton.vue";

export default {
  name: "AppBar",

  components: {
    ConfigurationsDialog,
    BackButton,
  },

  data() {
    return {
      files: [],
      currFiles: [],
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
        accept_any_certificates: false,
      },
    };
  },

  computed: {
    breadCrumbs() {
      if (typeof this.$route.meta.breadCrumb === "function") {
        return this.$route.meta.breadCrumb.call(this, this.$route);
      }
      return this.$route.meta.breadCrumb;
    },
    backNav() {
      return this.$route.meta.backNav;
    },
    showConfigDialog() {
      return this.$route.path === "/";
    },
  },

  watch: {
    files(newV, oldV) {
      if (isEqual(newV, oldV)) {
        return;
      }
    },
  },

  methods: {
    ...mapActions(["setError", "setInfo"]),

    remove(index) {
      this.files.splice(index, 1);
    },

    clearFilesInput() {
      this.currFiles = [];
      this.files = [];
    },

    async submitConfigForm() {
      let formData = new FormData();
      for (let file of this.files) {
        formData.append("certificates", file);
      }

      for (let recordName in this.theFormData) {
        formData.append(recordName, this.theFormData[recordName]);
      }

      axios.defaults.withCredentials = true;

      let response;
      try {
        response = await axios.post("/change-config", formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        });

        if (response.status === 201) {
          this.setInfo(response.data.message);
        }
        this.clearFilesInput();
      } catch (error) {
        this.setError(error.data.message);
        this.clearFilesInput();
      }
    },
  }
};
</script>
