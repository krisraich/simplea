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

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
@MongoEntity(collection="benutzer")
public class User extends PanacheMongoEntityBase {

    @BsonId
    public String id;

    private Map<String, Integer> buchungNummerCounter = new HashMap<>();

    public int getAndIncrementNextBuchungsNummer(LocalDate buchungsDatum) {
        String key = "" + buchungsDatum.getYear();
        buchungNummerCounter.putIfAbsent(key, 1);
        Integer i = buchungNummerCounter.get(key);
        buchungNummerCounter.put(key, i + 1);
        update();
        return i;
    }

}
