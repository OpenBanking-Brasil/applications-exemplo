//import axios from "../../util/axios";

const state = {
    selectedOption: ""
};

const getters = {
    selectedOption: (state) => state.selectedOption
};

const actions = {
    setOption({ commit }, option){
        commit("setSelectedOption", option);
    }

};

const mutations = {
    setSelectedOption: (state, selectedOption) => (state.selectedOption = selectedOption)
};

export default {
    state,
    getters,
    actions,
    mutations
}