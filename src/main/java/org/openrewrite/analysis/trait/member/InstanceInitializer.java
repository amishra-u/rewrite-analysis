/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.analysis.trait.member;

import fj.data.Validation;
import org.openrewrite.Cursor;
import org.openrewrite.analysis.trait.util.TraitErrors;

/**
 * An instance initializer is a method that contains field initializations
 * and explicit instance initializer blocks.
 */
public class InstanceInitializer extends InitializerMethodBase {
    InstanceInitializer(Cursor cursor) {
        super(cursor);
    }

    @Override
    public String getName() {
        return "<obinit>";
    }

    public static Validation<TraitErrors, InstanceInitializer> viewOf(Cursor cursor) {
        return InitializerMethodBase.genericViewOf(cursor, InstanceInitializer::new, InstanceInitializer.class);
    }
}
