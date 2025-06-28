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

package at.kara.user;

import at.kara.tenant.Tenant;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.Provider;

@Provider
@RequestScoped
public class UserProvider {

    @Inject
    Tenant tenant;

    @Produces
    public User getUser() {
        return (User) User.findByIdOptional(tenant.getTenantId()).orElseGet(() -> {
            User newUser = new User().setId(tenant.getTenantId());
            newUser.persist();
            return newUser;
        });
    }
}
