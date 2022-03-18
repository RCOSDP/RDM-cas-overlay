/*
 * Copyright (c) 2020. Center for Open Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cos.cas.adaptors.postgres.types;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Customized Hibernate data type for Postgres {@literal jsonb}.
 * {@link com.google.gson.JsonArray} is used as the object type / class for Postgres {@literal jsonb}.
 * @author nguyenminhtrung
 * @since 20.0.0
 */
public class PostgresJsonbArrayUserType extends PostgresJsonbUserType {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Class returnedClass() {
        return JsonArray.class;
    }

    @Override
    public Object nullSafeGet(
            final ResultSet rs,
            final String[] names,
            final SessionImplementor session,
            final Object owner
    ) throws HibernateException, SQLException {
        final String jsonString = rs.getString(names[0]);
        if (jsonString == null) {
            return null;
        }
        try {
            final JsonParser jsonParser = new JsonParser();
            return jsonParser.parse(jsonString).getAsJsonArray();
        } catch (final JsonSyntaxException | IllegalStateException e) {
            logger.error("PostgresJsonbArrayUserType.nullSafeGet(): failed to convert Java JSON String to GSON JsonArray:");
            throw new RuntimeException("Failed to convert Java JSON String to GSON JsonArray: " + e.getMessage());
        }
    }
}
