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

package at.kara.common;

import at.kara.tenant.Tenant;
import at.kara.user.User;
import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

@Authenticated
@RequiredArgsConstructor
public abstract class BaseResource {

    public static final String DEFAULT_TEMPLATE_NAME = "/index.html";

    @Inject
    Engine templateEngine;

    @Inject
    Provider<User> userProvider;

    @Inject
    Provider<Tenant> tenantProvider;

    private final String siteName;

    protected TemplateInstance getTemplate() {
        return templateEngine
                .getTemplate(siteName + DEFAULT_TEMPLATE_NAME)
                .data("user",  getCurrentUser())
                .data("tenant",  getCurrentTenant());
    }

    protected User getCurrentUser() {
        return userProvider.get();
    }

    protected Tenant getCurrentTenant() {
        return tenantProvider.get();
    }


    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        return getTemplate();
    }

}
