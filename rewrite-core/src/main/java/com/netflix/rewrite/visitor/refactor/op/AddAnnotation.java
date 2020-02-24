/*
 * Copyright 2020 the original authors.
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
package com.netflix.rewrite.visitor.refactor.op;

import com.netflix.rewrite.tree.Formatting;
import com.netflix.rewrite.tree.Tr;
import com.netflix.rewrite.tree.Type;
import com.netflix.rewrite.tree.TypeUtils;
import com.netflix.rewrite.visitor.refactor.AstTransform;
import com.netflix.rewrite.visitor.refactor.ScopedRefactorVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.netflix.rewrite.tree.Formatting.*;
import static com.netflix.rewrite.tree.Formatting.formatFirstPrefix;
import static com.netflix.rewrite.tree.Tr.randomId;

public class AddAnnotation extends ScopedRefactorVisitor {
    private final Type.Class annotationType;

    public AddAnnotation(UUID scope, String annotationTypeName) {
        super(scope);
        this.annotationType = Type.Class.build(annotationTypeName);
    }

    @Override
    public List<AstTransform> visitClassDecl(Tr.ClassDecl classDecl) {
        return maybeTransform(classDecl,
                isScope(classDecl),
                super::visitClassDecl,
                (cd, cursor) -> {
                    Tr.ClassDecl fixedCd = cd;

                    maybeAddImport(annotationType.getFullyQualifiedName());

                    if (fixedCd.getAnnotations().stream().noneMatch(ann -> TypeUtils.isOfClassType(ann.getType(), annotationType.getFullyQualifiedName()))) {
                        List<Tr.Annotation> fixedAnnotations = new ArrayList<>(fixedCd.getAnnotations());

                        Formatting requiredArgsFormatting = cd.getModifiers().isEmpty() ?
                                (cd.getTypeParams() == null ?
                                        cd.getKind().getFormatting() :
                                        cd.getTypeParams().getFormatting()) :
                                format(firstPrefix(cd.getModifiers()));

                        fixedAnnotations.add(new Tr.Annotation(randomId(),
                                Tr.Ident.build(randomId(), annotationType.getClassName(), annotationType, EMPTY),
                                null,
                                requiredArgsFormatting)
                        );

                        fixedCd = fixedCd.withAnnotations(fixedAnnotations);
                        if (cd.getAnnotations().isEmpty()) {
                            String prefix = formatter().findIndent(0, cd).getPrefix();

                            // special case, where a top-level class is often un-indented completely
                            String cdPrefix = cd.getFormatting().getPrefix();
                            if (cursor.getParentOrThrow().getTree() instanceof Tr.CompilationUnit &&
                                    cdPrefix.substring(cdPrefix.lastIndexOf('\n')).chars().noneMatch(c -> c == ' ' || c == '\t')) {
                                prefix = "\n";
                            }

                            if (!fixedCd.getModifiers().isEmpty()) {
                                fixedCd = fixedCd.withModifiers(formatFirstPrefix(fixedCd.getModifiers(), prefix));
                            } else if (fixedCd.getTypeParams() != null) {
                                fixedCd = fixedCd.withTypeParams(fixedCd.getTypeParams().withPrefix(prefix));
                            } else {
                                fixedCd = fixedCd.withKind(fixedCd.getKind().withPrefix(prefix));
                            }
                        }
                    }

                    return fixedCd;
                });
    }
}
