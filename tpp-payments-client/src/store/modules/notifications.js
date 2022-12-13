const state = {
  error: "",
  info: "",
  loading: false,
};

const mutations = {
  SET_ERROR(state, error) {
    state.error = error;
  },
  SET_INFO(state, info) {
    state.info = info;
  },
  SET_LOADING(state, loading) {
    state.loading = loading;
  },
};

const getters = {
  error(state) {
    return state.error;
  },
  info(state) {
    return state.info;
  },
  loading(state) {
    return state.loading;
  },
};

const actions = {
  setError({ commit }, error ) {
    commit("SET_LOADING", false);
    commit("SET_ERROR", error);
  },
  setInfo({ commit }, info) {
    commit("SET_INFO", info);
  },
  setLoading({ commit }, loading) {
    commit("SET_LOADING", loading);
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};