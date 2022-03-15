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

package io.cos.cas.authentication.exceptions;

/**
 * The Class InstitutionLoginAvailabilityException.
 */
public class InstitutionLoginAvailabilityException extends InstitutionLoginFailedException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2175125410715227766L;


    /** Instantiates a new exception (default). */
    public InstitutionLoginAvailabilityException() {
        super();
    }

    /**
     * Instantiates a new exception with a given message.
     *
     * @param message the message
     */
    public InstitutionLoginAvailabilityException(final String message) {
        super(message);
    }
}
