exports.data = {
  client: {
  application_type: 'web',
  grant_types: [
    'client_credentials',
    'authorization_code',
    'refresh_token',
    'implicit'
  ],
  id_token_signed_response_alg: 'PS256',
  require_auth_time: false,
  response_types: [
    'code id_token',
    'code'
  ],
  subject_type: 'public',
  token_endpoint_auth_method: 'tls_client_auth',
  request_object_signing_alg: 'PS256',
  require_signed_request_object: true,
  require_pushed_authorization_requests: false,
  tls_client_certificate_bound_access_tokens: true,
  client_id: '7nrwPCGZjkx03v4PqErZJ', //Client ID at the directory
  jwks_uri: 'https://keystore.sandbox.directory.openbankingbrasil.org.br/74e929d9-33b6-4d85-8ba7-c146c867a817/7218e1af-195f-42b5-a44b-8c7828470f5a/application.jwks', //JWKS uri for the software statement
  tls_client_auth_subject_dn: 'CN=7218e1af-195f-42b5-a44b-8c7828470f5a,OU=74e929d9-33b6-4d85-8ba7-c146c867a817,O=Open Banking,C=BR', //Change
  authorization_signed_response_alg: 'PS256',
},

  //SET THE KID FOR THE SIGNING KEYID FROM THE JWKS KEYSET
  signing_kid: '8o-O3VSFOPE8TrULXUTHxhxJcdADKIBmsfE0KWYkHik', //Change to the signing key that you want to use
  software_statement_id: '7218e1af-195f-42b5-a44b-8c7828470f5a', //Software Statement Id
  organisation_id: '74e929d9-33b6-4d85-8ba7-c146c867a817' //Software Statement Id
};
