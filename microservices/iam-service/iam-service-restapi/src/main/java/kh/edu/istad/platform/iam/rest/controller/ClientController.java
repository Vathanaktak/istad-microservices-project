package kh.edu.istad.platform.iam.rest.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.application.command.CreateClientCommand;
import kh.edu.istad.platform.iam.application.command.UpdateClientCommand;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.iam.rest.dto.ClientDtos.*;
import kh.edu.istad.platform.iam.rest.dto.Common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ListClientsUseCase listClients;
    private final CreateClientUseCase createClient;
    private final UpdateClientUseCase updateClient;
    private final DeleteClientUseCase deleteClient;
    private final RegenerateClientSecretUseCase regenerateSecret;

    @GetMapping
    @PreAuthorize("hasAuthority('client:read') or hasRole('ADMIN')")
    public PageResponse<ClientResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        var res = listClients.execute(q, page, size);
        var mapped = res.map(c -> new ClientResponse(
            c.id(), c.clientId(), c.clientName(), c.type(), c.grantTypes(), c.authenticationMethods(),
            c.redirectUris(), c.postLogoutRedirectUris(), c.scopes(), c.requireProofKey(),
            c.requireAuthorizationConsent(), c.accessTokenMinutes(), c.refreshTokenHours()));
        return PageResponse.of(mapped);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('client:create') or hasRole('ADMIN')")
    public ResponseEntity<ClientCreatedResponse> create(@Valid @RequestBody CreateClientRequest r) {
        var res = createClient.execute(new CreateClientCommand(
            r.clientId(), r.clientName(), r.type(), r.grantTypes(), r.redirectUris(),
            r.postLogoutRedirectUris(), r.scopes(), r.requireProofKey(),
            r.requireAuthorizationConsent(), r.accessTokenMinutes(), r.refreshTokenHours()));
        var c = res.client();
        var clientResp = new ClientResponse(
            c.id(), c.clientId(), c.clientName(), c.type(), c.grantTypes(), c.authenticationMethods(),
            c.redirectUris(), c.postLogoutRedirectUris(), c.scopes(), c.requireProofKey(),
            c.requireAuthorizationConsent(), c.accessTokenMinutes(), c.refreshTokenHours());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ClientCreatedResponse(clientResp, res.secret()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('client:update') or hasRole('ADMIN')")
    public ClientResponse update(@PathVariable String id, @Valid @RequestBody UpdateClientRequest r) {
        var c = updateClient.execute(id, new UpdateClientCommand(
            r.clientName(), r.type(), r.grantTypes(), r.redirectUris(),
            r.postLogoutRedirectUris(), r.scopes(), r.requireProofKey(),
            r.requireAuthorizationConsent(), r.accessTokenMinutes(), r.refreshTokenHours()));
        return new ClientResponse(
            c.id(), c.clientId(), c.clientName(), c.type(), c.grantTypes(), c.authenticationMethods(),
            c.redirectUris(), c.postLogoutRedirectUris(), c.scopes(), c.requireProofKey(),
            c.requireAuthorizationConsent(), c.accessTokenMinutes(), c.refreshTokenHours());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('client:delete') or hasRole('ADMIN')")
    public void delete(@PathVariable String id) {
        deleteClient.execute(id);
    }

    @PostMapping("/{id}/secret")
    @PreAuthorize("hasAuthority('client:update') or hasRole('ADMIN')")
    public SecretResponse regenerateSecret(@PathVariable String id) {
        String secret = regenerateSecret.execute(id);
        return new SecretResponse(secret);
    }
}
