<template>
  <v-card elevation="0" :class="['card-code', 'pa-0', { [color]: !isError }, { 'lightred': isError }]">
    <v-card-title class="px-0 pt-11 pb-5 pl-6">{{ title }} 
      <v-tooltip top v-if="tooltip">
        <template v-slot:activator="{ on, attrs }">
          <v-icon v-bind="attrs" v-on="on" color="white" right>mdi-information</v-icon>
        </template>
        <span>{{ tooltip }}</span>
      </v-tooltip>
    </v-card-title>
    <v-card-text>
      <v-sheet class="pa-4">
        <pre style="overflow: auto">
          {{ code }}
        </pre>
      </v-sheet>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  props: {
    color: {
      type: String,
      default: "lightgreen"
    },
    title: {
      type: String,
      default: "Response"
    },
    code: {
      type: String,
      default: ""
    },
    tooltip: {
      type: String,
      default: ""
    },
    isError: {
      type: Boolean,
      default: false
    }
  },
  name: "CardCode",
};
</script>

<style lang="scss" scoped>
.card-code {
  &.lightgreen {
    background-color: #EDFFFA;
  }

  &.lightblue {
    background-color: #E1F1F8;

    .v-sheet {
      border-color: #B7DDEB;
    }

  }

  &.lightred {
    background-color: #ffeded;

    .v-sheet {
      border-color: #D99F9F;
    }
  }

  .v-sheet {
    border-radius: 10px;
    border: 1px solid #9FD9C7;

    pre {
      font-family: 'Open Sans';
    }
  }
}
</style>
