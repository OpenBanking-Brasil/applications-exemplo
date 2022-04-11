import Vuex from "vuex";
import Vue from "vue";
import mockTPP from "./modules/mock-tpp";

//Load Vuex
Vue.use(Vuex);

//Create Store
const store = new Vuex.Store({
    modules: {
        mockTPP
    }
  })
  
export default store