/*
 * Copyright (c) 2016. Center for Open Science
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
package io.cos.cas.adaptors.postgres.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.cos.cas.adaptors.postgres.types.PostgresJsonbArrayUserType;
import io.cos.cas.adaptors.postgres.types.PostgresJsonbUserType;

/**
 * The Open Science Framework User.
 *
 * @author Michael Haselton
 * @author Longze Chen
 * @since 19.3.0
 */
@Entity
@Table(name = "osf_osfuser")
@TypeDefs({@TypeDef(name = "PostgresJsonb", typeClass = PostgresJsonbUserType.class),
    @TypeDef(name = "PostgresJsonbArray", typeClass = PostgresJsonbArrayUserType.class)})
public final class OpenScienceFrameworkUser {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "eppn", nullable = true)
    private String eppn;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "external_identity")
    @Type(type = "PostgresJsonb")
    private JsonObject externalIdentity;

    @Column(name = "verification_key")
    private String verificationKey;

    @OneToOne
    @JoinColumn(name = "merged_by_id")
    private OpenScienceFrameworkUser mergedBy;

    @Column(name = "given_name", nullable = false)
    private String givenName;

    @Column(name = "family_name", nullable = false)
    private String familyName;

    @Column(name = "is_registered", nullable = false)
    private Boolean registered;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_confirmed")
    private Date dateConfirmed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_disabled")
    private Date dateDisabled;

    @Column(name = "jobs")
    @Type(type = "PostgresJsonbArray")
    private JsonArray jobs;

    @Column(name = "given_name_ja", nullable = false)
    private String givenNameJa;

    @Column(name = "family_name_ja", nullable = false)
    private String familyNameJa;

    /** Default Constructor. */
    public OpenScienceFrameworkUser() {}

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEppn() {
        return eppn;
    }

    public String getPassword() {
        return password;
    }

    public JsonObject getExternalIdentity() {
        return externalIdentity;
    }

    public String getVerificationKey() {
        return verificationKey;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public Boolean isRegistered() {
        return registered;
    }

    public Boolean isMerged() {
        return mergedBy != null;
    }

    public Boolean isConfirmed() {
        return dateConfirmed != null;
    }

    public Boolean isDisabled() {
        return dateDisabled != null;
    }

    public Boolean isActive() {
        return isRegistered() && !isMerged() && !isDisabled() && isConfirmed();
    }

    public JsonArray getJobs() {
        return jobs;
    }

    public String getGivenNameJa() {
        return givenNameJa;
    }

    public String getFamilyNameJa() {
        return familyNameJa;
    }

    @Override
    public String toString() {
        return String.format("OpenScienceFrameworkUser [id=%s, username=%s]", id, username);
    }
}
