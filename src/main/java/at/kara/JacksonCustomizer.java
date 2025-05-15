/*
 * This file is part of the "Bauernkiste" application.
 *
 * Copyright (c) 2024 by KaRa Software - All rights reserved.
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package at.kara;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;


@Singleton
public class JacksonCustomizer implements ObjectMapperCustomizer {

    public static final JavaTimeModule DEFAULT_JAVA_TIME_MODULE = new JavaTimeModule();

    public static final Jdk8Module DEFAULT_JAVA_8_MODULE = new Jdk8Module();


    public void customize(ObjectMapper mapper) {

        /*
         * JavaTimeModule for Instance and ZonedDateTime
         * Note: JavaTimeModule does not perfectly deserialize ZonedDateTime
         * also java.util.Date ist not supported anymore?
         */
        mapper.registerModule(DEFAULT_JAVA_TIME_MODULE);

        /*
         * We want Jdk8Module for Optionals
         */
        mapper.registerModule(DEFAULT_JAVA_8_MODULE);

        /*
         * The mapper should not fail on unknown properties
         */
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /*
         * The mapper should include all values expect null.
         * This is useful, because it allows to explicit set a value to empty with Optional.empty().
         * Eg:
         *
         * Model with
         * Optional<Double> setMeToZero;
         * Optional<Double> ignoreMe;
         *
         * a JSON with { "setMeToZero": null } will produce an empty Optional,
         * while the left out field ignoreMe is still null
         *
         */
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    }

}

