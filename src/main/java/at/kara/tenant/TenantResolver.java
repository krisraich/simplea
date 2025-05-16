/*
 * This file is part of the "SimplEA" application.
 *
 * Copyright (c) 2025 by KaRa Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.kara.tenant;

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

@Provider
@RequestScoped
public class TenantResolver {

    public static final String SUB_CLAIM_KEY = "sub";
    public static final String USER_CLAIM_KEY = "given_name";
    public static final int CLAIM_CUTOFF_LENGTH = 32;

    @Context
    SecurityContext securityContext;

    @Produces
    public Tenant getTenantNameFromCaller() {
        OidcJwtCallerPrincipal oidcJwtCallerPrincipal = (OidcJwtCallerPrincipal) securityContext.getUserPrincipal();
        if (oidcJwtCallerPrincipal == null) {
            return null;
        }
        String sub = oidcJwtCallerPrincipal.getClaim(SUB_CLAIM_KEY);
        return new Tenant(
                oidcJwtCallerPrincipal.getClaim(USER_CLAIM_KEY),
                sub.substring(0, CLAIM_CUTOFF_LENGTH)
        );

    }
}
