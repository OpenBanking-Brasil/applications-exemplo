<template>
    <div class="layout-wrapper card-wrapper">
    <v-card elevation="2">
      <div class="card-content">
        <slot name="top"></slot>
        <v-card elevation="0" class="pa-0">
          <v-card-title class="px-0 pt-0">
            {{ title }}
          </v-card-title>
          <v-card-text class="px-0">
            <v-row class="pa-0 align-end">
              <slot name="card-content"></slot>
            </v-row>
          </v-card-text>
        </v-card>
        <slot name="content"></slot>
      </div>
      <v-card-actions class="d-flex justify-center">
        <BackButton text />
      </v-card-actions>
    </v-card>
  </div>
</template>

<script>
import BackButton from "@/components/Shared/BackButton.vue";

export default {
  name: "CardWrapper",
  components: {
    BackButton,
  },
  props: {
    title: {
      type: String,
      default: "Information"
    },
  },
};
</script>

<style lang="scss" scoped>
.card-wrapper{
  &.layout-wrapper  {
    &::v-deep .v-sheet {
      padding: 0;

      .card-content {
        padding: 50px 30px;

      }

      .v-card__actions {
        border-top: 1px solid rgba(57, 75, 101, 0.11);
      }
    }
  }
}
</style>
