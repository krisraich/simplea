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

import at.kara.common.BaseResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/benutzer")
@ApplicationScoped
public class UserResource extends BaseResource {

    public UserResource() {
        super("benutzer");
    }

    @POST
    @Path("/year")
    @Consumes("text/plain")
    public Response setYear(int newYear) {
        User currentUser = getCurrentUser();
        currentUser.setCurrentYear(newYear);
        currentUser.update();
        return Response.ok().build();
    }
}
