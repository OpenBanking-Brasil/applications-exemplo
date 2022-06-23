package com.raidiam.trustframework.bank.services;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

import java.util.List;

@Client("https://data.sandbox.directory.openbankingbrasil.org.br")
public interface RolesApiClient {

    @Get("/roles")
    List<DirectoryRole> roles();

}
