const state = {
  ApiOption: "app",
  clientID: "",
  registrationAccessToken: "",
  scopes: "",
  cadastroOption: "",
  consentId: "",
  consent: "",
  consents: [
    {
      id: 1,
      dataCategory: "Cadastro",
      group: "Dados Cadastrais PF",
      permissions: [
        {
          permission: "CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        }
      ],
      consent: false,
    },
    {
      id: 2,
      dataCategory: "Cadastro",
      group: "Informações complementares PF",
      permissions: [
        {
          permission: "CUSTOMERS_PERSONAL_ADITTIONALINFO_READ",
          consent: false
        }, 
        {
          permission: "RESOURCES_READ",
          consent: false
        }, 
      ],
      consent: false,
    },
    {
      id: 3,
      dataCategory: "Cadastro",
      group: "Dados Cadastrais PJ",
      permissions: [
        {
          permission: "CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        }
      ],
      consent: false,
    },
    {
      id: 4,
      dataCategory: "Cadastro",
      group: "Informações complementares PJ",
      permissions: [
        {
          permission: "CUSTOMERS_BUSINESS_ADITTIONALINFO_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        }
      ],
      consent: false,
    },
    {
      id: 5,
      dataCategory: "Contas",
      group: "Saldos",
      permissions: [
        {
          permission: "ACCOUNTS_READ",
          consent: false,
        },
        {
          permission: "ACCOUNTS_BALANCES_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        },
      ],
      consent: false,
    },
    {
      id: 6,
      dataCategory: "Contas",
      group: "Limites",
      permissions: [
        {
          permission: "ACCOUNTS_READ",
          consent: false,
        },
        {
          permission: "ACCOUNTS_OVERDRAFT_LIMITS_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        },
      ],
      consent: false,
    },
    {
      id: 7,
      dataCategory: "Contas",
      group: "Extratos",
      permissions: [
        {
          permission: "ACCOUNTS_READ",
          consent: false,
        },
        {
          permission: "ACCOUNTS_TRANSACTIONS_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        },
      ],
      consent: false,
    },
    {
      id: 8,
      dataCategory: "Cartão de Crédito",
      group: "Limites",
      permissions: [
        {
          permission: "CREDIT_CARDS_ACCOUNTS_READ",
          consent: false,
        },
        {
          permission: "CREDIT_CARDS_ACCOUNTS_LIMITS_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        },
      ],
      consent: false,
    },
    {
      id: 9,
      dataCategory: "Cartão de Crédito",
      group: "Transações",
      permissions: [
        {
          permission: "CREDIT_CARDS_ACCOUNTS_READ",
          consent: false,
        },
        {
          permission: "CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        },
      ],
      consent: false,
    },
    {
      id: 10,
      dataCategory: "Cartão de Crédito",
      group: "Faturas",
      permissions: [
        {
          permission: "CREDIT_CARDS_ACCOUNTS_READ",
          consent: false,
        },
        {
          permission: "CREDIT_CARDS_ACCOUNTS_BILLS_READ",
          consent: false,
        },
        {
          permission: "CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        },
      ],
      consent: false,
    },
    {
      id: 11,
      dataCategory: "Operações de Crédito",
      group: "Dados do Contrato",
      permissions: [
        {
          permission: "LOANS_READ",
          consent: false,
        },
        {
          permission: "LOANS_WARRANTIES_READ",
          consent: false,
        },
        {
          permission: "LOANS_SCHEDULED_INSTALMENTS_READ",
          consent: false,
        },
        {
          permission: "LOANS_PAYMENTS_READ",
          consent: false,
        },
        {
          permission: "FINANCINGS_READ",
          consent: false,
        },
        {
          permission: "FINANCINGS_WARRANTIES_READ",
          consent: false,
        },
        {
          permission: "FINANCINGS_SCHEDULED_INSTALMENTS_READ",
          consent: false,
        },
        {
          permission: "FINANCINGS_PAYMENTS_READ",
          consent: false,
        },
        {
          permission: "UNARRANGED_ACCOUNTS_OVERDRAFT_READ",
          consent: false,
        },
        {
          permission: "UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ",
          consent: false,
        },
        {
          permission: "UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ",
          consent: false,
        },
        {
          permission: "UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ",
          consent: false,
        },
        {
          permission: "INVOICE_FINANCINGS_READ",
          consent: false,
        },
        {
          permission: "INVOICE_FINANCINGS_WARRANTIES_READ",
          consent: false,
        },
        {
          permission: "INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ",
          consent: false,
        },
        {
          permission: "INVOICE_FINANCINGS_PAYMENTS_READ",
          consent: false,
        },
        {
          permission: "RESOURCES_READ",
          consent: false,
        },

      ],
      consent: false,
    },
  ],
  newConsent: true,
  consentsList: [],
  selectedConsent: null,
  paymentConsentsList: [],
  paymentSelectedConsent: null,
};

const getters = {
  consents: (state) => state.consents,
  cadastroOption: (state) => state.cadastroOption,
  scopes: (state) => state.scopes,
  clientID: (state) => state.clientID,
  registrationAccessToken: (state) => state.registrationAccessToken,
  ApiOption: (state) => state.ApiOption,
  consentId: (state) => state.consentId,
  consent: (state) => state.consent,
  newConsent: (state) => state.newConsent,
  consentsList: (state) => state.consentsList,
  selectedConsent: (state) => state.selectedConsent,
  paymentConsentsList: (state) => state.paymentConsentsList,
  paymentSelectedConsent: (state) => state.paymentSelectedConsent,
};

const actions = {
  setCadastroOption({ commit }, cadastroOption) {
    commit("setSelectedCadastroOption", cadastroOption);
  },
  setScopes({ commit }, scopes) {
    commit("setTheScopes", scopes);
  },
  setApiOption({ commit }, ApiOption) {
    commit("setTheApiOption", ApiOption);
  },
  setConsentId({ commit }, consentId) {
    commit("setTheConsentId", consentId);
  },
  setConsent({ commit }, consent) {
    commit("setTheConsent", consent);
  },
  setNewConsent({ commit }, isNew) {
    commit("setTheNewConsent", isNew);
  },
  addToConsentsList({ commit }, consent) {
    commit("addToTheConsentsList", consent);
  },
  removeFromConsentsList({ commit }, consentId) {
    commit("removeFromTheConsentsList", consentId);
  },
  updateConsentInConsentsList({ commit }, consent) {
    commit("updateTheConsentInConsentsList", consent);
  },
  setSelectedConsent({ commit }, selectedConsent) {
    commit("setTheSelectedConsent", selectedConsent);
  },
  setSelectedConsentFromId({ commit }, selectedConsentId) {
    commit("setTheSelectedConsentFromId", selectedConsentId);
  },
  setPaymentConsentsList({ commit }, paymentConsentsList) {
    commit("setThePaymentConsentsList", paymentConsentsList);
  },
  setPaymentSelectedConsent({ commit }, paymentSelectedConsent) {
    commit("setThePaymentSelectedConsent", paymentSelectedConsent);
  },
};

const mutations = {
  setClientID: (state, clientID) => (state.clientID = clientID),
  setRegistrationAccessToken: (state, registrationAccessToken) =>
    (state.registrationAccessToken = registrationAccessToken),
  setSelectedCadastroOption: (state, cadastroOption) =>
    (state.cadastroOption = cadastroOption),
  setTheScopes: (state, scopes) => (state.scopes = scopes),
  setTheApiOption: (state, ApiOption) => (state.ApiOption = ApiOption),
  setTheConsentId: (state, consentId) => (state.consentId = consentId),
  setTheNewConsent: (state, isNew) => (state.newConsent = isNew),
  setTheConsent: (state, consent) => (state.consent = consent),
  addToTheConsentsList: (state, consent) => {
    const index = state.consentsList.findIndex(item => item.consent.data.consentId === consent.consent.data.consentId);
    if (index == -1) {
      state.consentsList.push(consent);
    }
  },
  removeFromTheConsentsList: (state, consentId) => {
    const index = state.consentsList.findIndex(item => item.consent.data.consentId === consentId);
    if (index > -1) {
      state.consentsList.splice(index, 1);
    }
  },
  updateTheConsentInConsentsList: (state, consent) => {
    const index = state.consentsList.findIndex(item => item.consent.data.consentId === consent.data.consentId);
    if (index > -1) {
      state.consentsList[index].consent = consent;
    }
  },
  setTheSelectedConsent: (state, selectedConsent) => (state.selectedConsent = selectedConsent),
  setTheSelectedConsentFromId: (state, selectedConsentId) => {
    state.selectedConsent = state.consentsList.find(item => item.consent.data.consentId === selectedConsentId);
    console.log(state.selectedConsent);
  },
  setThePaymentConsentsList: (state, paymentConsentsList) => (state.paymentConsentsList = paymentConsentsList),
  setThePaymentSelectedConsent: (state, paymentSelectedConsent) => (state.paymentSelectedConsent = paymentSelectedConsent),
};

export default {
  state,
  getters,
  actions,
  mutations,
};
