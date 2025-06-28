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

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Since we have RAM zum schweinefüttern, we can cache every static resource in mem
 */
//@Slf4j
//@PermitAll
//@Path("/{name}")
//@ApplicationScoped
//public class AssetResource {
//
//    public static final java.nio.file.Path PATH_TO_STATIC = Paths.get("src/main/resources/static");
//
//    private final Map<String, byte[]> cache = new HashMap<>();
//
//    @SneakyThrows
//    void onStart(@Observes StartupEvent ev) {
//        Files.walk(PATH_TO_STATIC)
//                .filter(Files::isRegularFile)
//                .forEach(path -> {
//                    try {
//                        cache.put(path.getFileName().toString(), Files.readAllBytes(path));
//                        log.info("Asset found: {}", path.getFileName().toString());
//                    } catch (IOException e) {
//                        log.error("Error reading file '%s' because: %s", path, e.getMessage());
//                    }
//                });
//    }
//
//
//    @GET
//    public Uni<Response> get(@PathParam("name") String name){
//        if(name == null || name.isEmpty()) {
//            name = "index.html";
//        }
//        byte[] resource = cache.get(name);
//
//        if(resource == null) {
//            throw new NotFoundException();
//        }
//
//        return Uni.createFrom().item(Response.ok(resource).build());
//    }
//
//}
