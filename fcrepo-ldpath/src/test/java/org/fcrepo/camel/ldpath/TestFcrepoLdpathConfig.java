/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fcrepo.camel.ldpath;

import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.model.functions.ConcatenateFunction;
import org.apache.marmotta.ldpath.model.functions.FirstFunction;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @author dbernstein
 */
@Configuration
public class TestFcrepoLdpathConfig extends FcrepoLdPathConfig {
    protected Set<SelectorFunction> createSelectorFunctions() {
        return Set.of(new ConcatenateFunction(), new FirstFunction());
    }
}