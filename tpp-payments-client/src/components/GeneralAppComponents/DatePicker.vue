<template>
      <v-dialog
        ref="dialog"
        v-model="modal"
        :return-value.sync="theDate"
        persistent
        width="290px"
      >
        <template v-slot:activator="{ on, attrs }">
          <v-text-field
            outlined
            dense
            v-model="theDate"
            :label="dateLabel"
            name="date"
            prepend-icon="mdi-calendar"
            v-bind="attrs"
            v-on="on"
          ></v-text-field>
        </template>
        <v-date-picker v-model="theDate" scrollable>
          <v-spacer></v-spacer>
          <v-btn text color="primary" @click="modal = false"> Cancel </v-btn>
          <v-btn text color="primary" @click="changeDate">
            OK
          </v-btn>
        </v-date-picker>
      </v-dialog>
</template>

<script>
export default {
  props: {
    date: {
      type: String,
    },
    dateLabel: {
        type: String
    }
  },
  name: "DatePicker",
  data() {
    return {
      modal: false,
      theDate: JSON.parse(this.date),
    };
  },

  methods: {
      changeDate(){
        this.$refs.dialog.save(this.theDate);
        const flag = this.dateLabel.split(" ").join("");
        this.$emit("change-date", this.theDate, flag);
      }
  }
};
</script>

<style></style>
