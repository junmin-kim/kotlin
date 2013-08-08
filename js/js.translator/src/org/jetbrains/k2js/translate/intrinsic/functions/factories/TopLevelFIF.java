/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.k2js.translate.intrinsic.functions.factories;

import com.google.dart.compiler.backend.js.ast.JsExpression;
import com.google.dart.compiler.backend.js.ast.JsInvocation;
import com.google.dart.compiler.backend.js.ast.JsNameRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.descriptors.FunctionDescriptor;
import org.jetbrains.jet.lang.descriptors.PropertyDescriptor;
import org.jetbrains.jet.lang.psi.JetExpression;
import org.jetbrains.jet.lang.psi.JetQualifiedExpression;
import org.jetbrains.jet.lang.psi.JetReferenceExpression;
import org.jetbrains.jet.lang.resolve.calls.model.ResolvedCall;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverValue;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;
import org.jetbrains.jet.lang.types.lang.PrimitiveType;
import org.jetbrains.k2js.translate.context.TranslationContext;
import org.jetbrains.k2js.translate.intrinsic.functions.basic.FunctionIntrinsic;
import org.jetbrains.k2js.translate.intrinsic.functions.patterns.DescriptorPredicate;
import org.jetbrains.k2js.translate.intrinsic.functions.patterns.NamePredicate;
import org.jetbrains.k2js.translate.reference.CallTranslator;
import org.jetbrains.k2js.translate.utils.AnnotationsUtils;
import org.jetbrains.k2js.translate.utils.BindingUtils;
import org.jetbrains.k2js.translate.utils.JsDescriptorUtils;

import java.util.List;

import static org.jetbrains.k2js.translate.intrinsic.functions.basic.FunctionIntrinsic.CallParametersAwareFunctionIntrinsic;
import static org.jetbrains.k2js.translate.intrinsic.functions.patterns.PatternBuilder.pattern;
import static org.jetbrains.k2js.translate.utils.TranslationUtils.generateInvocationArguments;

public final class TopLevelFIF extends CompositeFIF {
    @NotNull
    public static final KotlinFunctionIntrinsic EQUALS = new KotlinFunctionIntrinsic("equals");
    @NotNull
    private static final FunctionIntrinsic RETURN_RECEIVER_INTRINSIC = new FunctionIntrinsic() {
        @NotNull
        @Override
        public JsExpression apply(
                @Nullable JsExpression receiver,
                @NotNull List<JsExpression> arguments,
                @NotNull TranslationContext context
        ) {
            assert receiver != null;
            return receiver;
        }
    };

    private static final FunctionIntrinsic NATIVE_MAP_GET = new NativeMapGetSet() {
        @NotNull
        @Override
        protected String operation() {
            return "get";
        }

        @Nullable
        @Override
        protected ExpressionReceiver getExpressionReceiver(@NotNull ResolvedCall<?> resolvedCall) {
            ReceiverValue result = resolvedCall.getThisObject();
            return result instanceof ExpressionReceiver ? (ExpressionReceiver) result : null;
        }

        @Override
        protected JsExpression asArrayAccess(
                @NotNull JsExpression receiver,
                @NotNull List<JsExpression> arguments,
                @NotNull TranslationContext context
        ) {
            return ArrayFIF.GET_INTRINSIC.apply(receiver, arguments, context);
        }
    };

    private static final FunctionIntrinsic NATIVE_MAP_SET = new NativeMapGetSet() {
        @NotNull
        @Override
        protected String operation() {
            return "put";
        }

        @Nullable
        @Override
        protected ExpressionReceiver getExpressionReceiver(@NotNull ResolvedCall<?> resolvedCall) {
            ReceiverValue result = resolvedCall.getReceiverArgument();
            return result instanceof ExpressionReceiver ? (ExpressionReceiver) result : null;
        }

        @Override
        protected JsExpression asArrayAccess(
                @NotNull JsExpression receiver,
                @NotNull List<JsExpression> arguments,
                @NotNull TranslationContext context
        ) {
            return ArrayFIF.SET_INTRINSIC.apply(receiver, arguments, context);
        }
    };

    @NotNull
    public static final FunctionIntrinsicFactory INSTANCE = new TopLevelFIF();

    private TopLevelFIF() {
        add(pattern("jet", "toString").receiverExists(), new KotlinFunctionIntrinsic("toString"));
        add(pattern("jet", "equals").receiverExists(), EQUALS);
        add(pattern(NamePredicate.PRIMITIVE_NUMBERS, "equals"), EQUALS);
        add(pattern("String|Boolean|Char|Number.equals"), EQUALS);
        add(pattern("jet", "arrayOfNulls"), new KotlinFunctionIntrinsic("nullArray"));
        add(pattern("jet", "iterator").receiverExists(), RETURN_RECEIVER_INTRINSIC);
        add(new DescriptorPredicate() {
                @Override
                public boolean apply(@NotNull FunctionDescriptor descriptor) {
                    if (!descriptor.getName().asString().equals("invoke")) {
                        return false;
                    }
                    int parameterCount = descriptor.getValueParameters().size();
                    DeclarationDescriptor fun = descriptor.getContainingDeclaration();
                    return fun == (descriptor.getReceiverParameter() == null
                                   ? KotlinBuiltIns.getInstance().getFunction(parameterCount)
                                   : KotlinBuiltIns.getInstance().getExtensionFunction(parameterCount));
                }
            }, new CallParametersAwareFunctionIntrinsic() {
                @NotNull
                @Override
                public JsExpression apply(
                        @NotNull CallTranslator callTranslator,
                        @NotNull List<JsExpression> arguments,
                        @NotNull TranslationContext context
                ) {
                    JsExpression thisExpression = callTranslator.getCallParameters().getThisObject();
                    if (thisExpression == null) {
                        return new JsInvocation(callTranslator.getCallParameters().getFunctionReference(), arguments);
                    }
                    else if (callTranslator.getResolvedCall().getReceiverArgument().exists()) {
                        return callTranslator.extensionFunctionCall(false);
                    }
                    else {
                        return new JsInvocation(new JsNameRef("call", callTranslator.getCallParameters().getFunctionReference()),
                                                generateInvocationArguments(thisExpression, arguments));
                    }
                }
            }
        );

        String[] javaUtil = {"java", "util"};
        add(pattern(javaUtil, "set").receiverExists(), NATIVE_MAP_SET);
        add(pattern("jet", "Map", "get"), NATIVE_MAP_GET);
        add(pattern(javaUtil, "HashMap", "get"), NATIVE_MAP_GET);

        add(pattern(javaUtil, "HashMap", "<init>"), new MapSelectImplementationIntrinsic(false));
        add(pattern(javaUtil, "HashSet", "<init>"), new MapSelectImplementationIntrinsic(true));
    }

    private abstract static class NativeMapGetSet extends CallParametersAwareFunctionIntrinsic {
        @NotNull
        protected abstract String operation();

        @Nullable
        protected abstract ExpressionReceiver getExpressionReceiver(@NotNull ResolvedCall<?> resolvedCall);

        protected abstract JsExpression asArrayAccess(
                @NotNull JsExpression receiver,
                @NotNull List<JsExpression> arguments,
                @NotNull TranslationContext context
        );

        @NotNull
        @Override
        public JsExpression apply(@NotNull CallTranslator callTranslator, @NotNull List<JsExpression> arguments, @NotNull TranslationContext context) {
            ExpressionReceiver expressionReceiver = getExpressionReceiver(callTranslator.getResolvedCall());
            JsExpression thisOrReceiver = callTranslator.getCallParameters().getThisOrReceiverOrNull();
            assert thisOrReceiver != null;
            if (expressionReceiver != null) {
                JetExpression expression = expressionReceiver.getExpression();
                JetReferenceExpression referenceExpression = null;
                if (expression instanceof JetReferenceExpression) {
                    referenceExpression = (JetReferenceExpression) expression;
                }
                else if (expression instanceof JetQualifiedExpression) {
                    JetExpression candidate = ((JetQualifiedExpression) expression).getReceiverExpression();
                    if (candidate instanceof JetReferenceExpression) {
                        referenceExpression = (JetReferenceExpression) candidate;
                    }
                }

                if (referenceExpression != null) {
                    DeclarationDescriptor candidate = BindingUtils.getDescriptorForReferenceExpression(context.bindingContext(),
                                                                                                       referenceExpression);
                    if (candidate instanceof PropertyDescriptor && AnnotationsUtils.isNativeObject(candidate)) {
                        return asArrayAccess(thisOrReceiver, arguments, context);
                    }
                }
            }

            return new JsInvocation(new JsNameRef(operation(), thisOrReceiver), arguments);
        }
    }

    private static class MapSelectImplementationIntrinsic extends CallParametersAwareFunctionIntrinsic {
        private final boolean isSet;

        private MapSelectImplementationIntrinsic(boolean isSet) {
            this.isSet = isSet;
        }

        @NotNull
        @Override
        public JsExpression apply(
                @NotNull CallTranslator callTranslator,
                @NotNull List<JsExpression> arguments,
                @NotNull TranslationContext context
        ) {
            JetType keyType = callTranslator.getResolvedCall().getTypeArguments().values().iterator().next();
            Name keyTypeName = JsDescriptorUtils.getNameIfStandardType(keyType);
            String collectionClassName;
            if (keyTypeName != null &&
                (NamePredicate.PRIMITIVE_NUMBERS.apply(keyTypeName) ||
                 keyTypeName.asString().equals("String") ||
                 PrimitiveType.BOOLEAN.getTypeName().equals(keyTypeName))) {
                collectionClassName = isSet ? "PrimitiveHashSet" : "PrimitiveHashMap";
            }
            else {
                collectionClassName = isSet ? "ComplexHashSet" : "ComplexHashMap";
            }

            return callTranslator.createConstructorCallExpression(context.namer().kotlin(collectionClassName));
        }
    }
}
