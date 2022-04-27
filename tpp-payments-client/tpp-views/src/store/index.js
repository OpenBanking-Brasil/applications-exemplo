import Vuex from "vuex";
import Vue from "vue";
import mockTPP from "./modules/mock-tpp";
import createPersistedState from "vuex-persistedstate";

//Load Vuex
Vue.use(Vuex);

//Create Store
const store = new Vuex.Store({
    modules: {
        mockTPP
    },
    plugins: [createPersistedState()]
  })
  
export default store