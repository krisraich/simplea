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

package at.kara;

import at.kara.tenant.Tenant;
import io.quarkus.mongodb.panache.common.MongoDatabaseResolver;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * DB name must not exceed 63 chars.
 */
@RequestScoped
public class DatabaseResolver implements MongoDatabaseResolver {

    @ConfigProperty(name = "quarkus.mongodb.database")
    String databasePrefix;

    @Inject
    Tenant tenant;

    @Override
    public String resolve() {
        if(tenant == null) {
            throw new SecurityException("Tenant not set");
        }
        return databasePrefix + "-" + tenant.getTenantId();
    }

}
