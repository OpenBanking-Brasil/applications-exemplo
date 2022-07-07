const state = {
  ApiOption: "app",
  clientID: "",
  registrationAccessToken: "",
  scopes: "",
  cadastroOption: "",
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
};

const getters = {
  consents: (state) => state.consents,
  cadastroOption: (state) => state.cadastroOption,
  scopes: (state) => state.scopes,
  clientID: (state) => state.clientID,
  registrationAccessToken: (state) => state.registrationAccessToken,
  ApiOption: (state) => state.ApiOption,
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
  }
};

const mutations = {
  setClientID: (state, clientID) => (state.clientID = clientID),
  setRegistrationAccessToken: (state, registrationAccessToken) =>
    (state.registrationAccessToken = registrationAccessToken),
  setSelectedCadastroOption: (state, cadastroOption) =>
    (state.cadastroOption = cadastroOption),
  setTheScopes: (state, scopes) => (state.scopes = scopes),
  setTheApiOption: (state, ApiOption) => (state.ApiOption = ApiOption)
};

export default {
  state,
  getters,
  actions,
  mutations,
};
