import Vue from "vue";
import Vuetify from "vuetify/lib/framework";

Vue.use(Vuetify);

const vuetify = new Vuetify({
    theme: {
        themes: {
            light: {
                primary: "#007199",
                secondary: "#d7dbe0",
                lightblue:"#E1F1F8",
                lightgreen:"#EDFFFA",
                lightred:"#ffeded"
            },
        },
    },
});
export default vuetify;
