<template>
  <v-dialog
    ref="dialog"
    v-model="modal"
    :return-value.sync="theDate"
    content-class="dialog-content"
    persistent
    width="290px"
  >
    <template v-slot:activator="{ on, attrs }">
      <v-text-field
        dense
        outlined
        readonly
        v-model="theDate"
        :label="label"
        name="date"
        append-icon="mdi-calendar"
        v-bind="attrs"
        v-on="on"
      ></v-text-field>
    </template>

    <v-date-picker v-model="theDate" color="#007199" scrollable>
      <v-btn text color="#007199" @click="modal = false"> Cancel </v-btn>

      <v-divider vertical />

      <v-btn text color="#007199" @click="changeDate"> OK </v-btn>
    </v-date-picker>
  </v-dialog>
</template>

<script>
export default {
  name: "DatePicker",
  
  props: {
    value: {
      type: String,
      default: ""
    },
    label: {
      type: String,
      default: ""
    }
  },

  data() {
    return {
      modal: false,
      theDate: null,
    };
  },

  created() {
    this.theDate = JSON.parse(JSON.stringify(this.value));
  },

  methods: {
    changeDate(){
      this.$refs.dialog.save(this.theDate);
      this.$emit("input", this.theDate);
    }
  }
};
</script>