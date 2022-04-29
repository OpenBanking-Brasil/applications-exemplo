const state = {
  selectedOption: "",
  clientID: "",
  registrationAccessToken: "",
  cadastroOption: "",
  consents: [
    {
      id: 1,
      dataCategory: "Cadastro",
      group: "Dados Cadastrais PF",
      permissions: [
        "CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ",
        "RESOURCES_READ",
      ],
      consent: false,
    },
    {
      id: 2,
      dataCategory: "Cadastro",
      group: "Informações complementares PF",
      permissions: ["CUSTOMERS_PERSONAL_ADITTIONALINFO_READ", "RESOURCES_READ"],
      consent: false,
    },
    {
      id: 3,
      dataCategory: "Cadastro",
      group: "Dados Cadastrais PJ",
      permissions: [
        "CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ",
        "RESOURCES_READ",
      ],
      consent: false,
    },
    {
      id: 4,
      dataCategory: "Cadastro",
      group: "Informações complementares PJ",
      permissions: ["CUSTOMERS_BUSINESS_ADITTIONALINFO_READ", "RESOURCES_READ"],
      consent: false,
    },
    {
      id: 5,
      dataCategory: "Contas",
      group: "Saldos",
      permissions: [
        "ACCOUNTS_READ",
        "ACCOUNTS_BALANCES_READ",
        "RESOURCES_READ",
      ],
      consent: false,
    },
    {
      id: 6,
      dataCategory: "Contas",
      group: "Limites",
      permissions: [
        "ACCOUNTS_READ",
        "ACCOUNTS_OVERDRAFT_LIMITS_READ",
        "RESOURCES_READ",
      ],
      consent: false,
    },
    {
      id: 7,
      dataCategory: "Contas",
      group: "Extratos",
      permissions: [
        "ACCOUNTS_READ",
        "ACCOUNTS_TRANSACTIONS_READ",
        "RESOURCES_READ",
      ],
      consent: false,
    },
    {
      id: 8,
      dataCategory: "Cartão de Crédito",
      group: "Limites",
      permissions: [
        "CREDIT_CARDS_ACCOUNTS_READ",
        "CREDIT_CARDS_ACCOUNTS_LIMITS_READ",
        "RESOURCES_READ",
      ],
      consent: false,
    },
    {
      id: 9,
      dataCategory: "Cartão de Crédito",
      group: "Transações",
      permissions: [
        "CREDIT_CARDS_ACCOUNTS_READ",
        "CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ",
        "RESOURCES_READ",
      ],
      consent: false,
    },
    {
      id: 10,
      dataCategory: "Cartão de Crédito",
      group: "Faturas",
      permissions: [
        "CREDIT_CARDS_ACCOUNTS_READ",
        "CREDIT_CARDS_ACCOUNTS_BILLS_READ",
        "CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ",
        "RESOURCES_READ",
      ],
      consent: false,
    },
    {
      id: 11,
      dataCategory: "Operações de Crédito",
      group: "Dados do Contrato",
      permissions: [
        "LOANS_READ",
        "LOANS_WARRANTIES_READ",
        "LOANS_SCHEDULED_INSTALMENTS_READ",
        "LOANS_PAYMENTS_READ",
        "FINANCINGS_READ",
        "FINANCINGS_WARRANTIES_READ",
        "FINANCINGS_SCHEDULED_INSTALMENTS_READ",
        "FINANCINGS_PAYMENTS_READ",
        "UNARRANGED_ACCOUNTS_OVERDRAFT_READ",
        "UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ",
        "UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ",
        "UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ",
        "INVOICE_FINANCINGS_READ",
        "INVOICE_FINANCINGS_WARRANTIES_READ",
        "INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ",
        "INVOICE_FINANCINGS_PAYMENTS_READ",
        "RESOURCES_READ",
      ],
      consent: false,
    },
  ],
};

const getters = {
  selectedOption: (state) => state.selectedOption,
  consents: (state) => state.consents,
  cadastroOption: (state) => state.cadastroOption,
};

const actions = {
  setOption({ commit }, option) {
    commit("setSelectedOption", option);
  },
  setCadastroOption({ commit }, cadastroOption){
    commit("setSelectedCadastroOption", cadastroOption);
  }
};

const mutations = {
  setSelectedOption: (state, selectedOption) =>
    (state.selectedOption = selectedOption),

  setClientID: (state, clientID) => state.clientID = clientID,
  setRegistrationAccessToken: (state, registrationAccessToken) => state.registrationAccessToken = registrationAccessToken,
  setSelectedCadastroOption: (state, cadastroOption) => state.cadastroOption = cadastroOption
};

export default {
  state,
  getters,
  actions,
  mutations,
};
