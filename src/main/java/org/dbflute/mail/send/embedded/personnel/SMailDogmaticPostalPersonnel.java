/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.mail.send.embedded.personnel;

import java.util.ArrayList;
import java.util.List;

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.SMailReceptionist;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.embedded.postie.SMailHonestPostie;
import org.dbflute.mail.send.embedded.proofreader.SMailBatchProofreader;
import org.dbflute.mail.send.embedded.proofreader.SMailPmCommentProofreader;
import org.dbflute.mail.send.embedded.receptionist.SMailConventionReceptionist;
import org.dbflute.mail.send.supplement.async.SMailAsyncStrategy;
import org.dbflute.mail.send.supplement.filter.SMailAddressFilter;
import org.dbflute.mail.send.supplement.filter.SMailSubjectFilter;
import org.dbflute.mail.send.supplement.logging.SMailLoggingStrategy;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class SMailDogmaticPostalPersonnel implements SMailPostalPersonnel {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String CLASSPATH_BASEDIR = "mail";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailReceptionist receptionist;
    protected final SMailTextProofreader proofreader;

    // -----------------------------------------------------
    //                                           Deep Option
    //                                           -----------
    protected final OptionalThing<SMailAddressFilter> addressFilter;
    protected final OptionalThing<SMailSubjectFilter> subjectFilter;
    protected final OptionalThing<SMailAsyncStrategy> asyncStrategy;
    protected final OptionalThing<SMailLoggingStrategy> loggingStrategy;

    // -----------------------------------------------------
    //                                       for Development
    //                                       ---------------
    protected boolean training;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDogmaticPostalPersonnel() {
        receptionist = createOutsideBodyReceptionist();
        proofreader = createProofreader();
        addressFilter = createAddressFilter();
        subjectFilter = createSubjectFilter();
        asyncStrategy = createAsyncStrategy();
        loggingStrategy = createLoggingStrategy();
    }

    public void workingDispose() {
        receptionist.workingDispose();
        proofreader.workingDispose();
    }

    // -----------------------------------------------------
    //                                          Receptionist
    //                                          ------------
    protected SMailReceptionist createOutsideBodyReceptionist() { // you can change it e.g. from database
        return newMailConventionReceptionist().asClasspathBase(CLASSPATH_BASEDIR);
    }

    protected SMailConventionReceptionist newMailConventionReceptionist() {
        return new SMailConventionReceptionist();
    }

    // -----------------------------------------------------
    //                                           Proofreader
    //                                           -----------
    protected SMailTextProofreader createProofreader() {
        final List<SMailTextProofreader> readerList = new ArrayList<SMailTextProofreader>(4);
        setupProofreader(readerList);
        return new SMailBatchProofreader(readerList);
    }

    protected void setupProofreader(List<SMailTextProofreader> readerList) { // you can add yours
        readerList.add(createTemplateProofreader());
    }

    protected SMailTextProofreader createTemplateProofreader() { // you can change it e.g. Velocity
        return newMailPmCommentProofreader();
    }

    protected SMailPmCommentProofreader newMailPmCommentProofreader() {
        return new SMailPmCommentProofreader();
    }

    // -----------------------------------------------------
    //                                           Deep Option
    //                                           -----------
    protected OptionalThing<SMailAddressFilter> createAddressFilter() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailSubjectFilter> createSubjectFilter() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailAsyncStrategy> createAsyncStrategy() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailLoggingStrategy> createLoggingStrategy() {
        return OptionalThing.empty();
    }

    // -----------------------------------------------------
    //                                       for Development
    //                                       ---------------
    public SMailDogmaticPostalPersonnel asTraining() {
        training = true;
        return this;
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    // -----------------------------------------------------
    //                                          Receptionist
    //                                          ------------
    @Override
    public SMailReceptionist selectReceptionist(Postcard postcard) {
        return receptionist;
    }

    // -----------------------------------------------------
    //                                           Proofreader
    //                                           -----------
    @Override
    public SMailTextProofreader selectProofreader(Postcard postcard) {
        return proofreader;
    }

    // -----------------------------------------------------
    //                                                Postie
    //                                                ------
    @Override
    public SMailPostie selectPostie(Postcard postcard, SMailPostalMotorbike motorbike) {
        final SMailHonestPostie postie = newSMailHonestPostie(motorbike);
        addressFilter.ifPresent(filter -> {
            postie.withAddressFilter(filter);
        });
        asyncStrategy.ifPresent(strategy -> {
            postie.withAsyncStrategy(strategy);
        });
        loggingStrategy.ifPresent(strategy -> {
            postie.withLoggingStrategy(strategy);
        });
        return training ? postie.asTraining() : postie;
    }

    protected SMailHonestPostie newSMailHonestPostie(SMailPostalMotorbike motorbike) {
        return new SMailHonestPostie(motorbike);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(DfTypeUtil.toClassTitle(this));
        sb.append(":{").append(receptionist);
        sb.append(", ").append(proofreader).append((training ? ", *training" : ""));
        sb.append("}@").append(Integer.toHexString(hashCode()));
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isTraining() {
        return training;
    }
}
